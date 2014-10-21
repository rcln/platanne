package fr.lipn.nlptools.uima.lia.ne;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.NamedEntity;
import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.FilterTokenAndAnnot;
import fr.lipn.nlptools.utils.align.SimpleTaggedTextReader;
import fr.lipn.nlptools.utils.align.TagPositionConsumer;
import fr.lipn.nlptools.utils.align.VoidInputReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.StringWriterFeeder;

/**
 * 
 * This AE is a wrapper for the LIA NE main program, which is actually a script calling different subprograms (including CRF++).
 * It tags named entities using the LIA NE CRF model, see LIA NE documentation. Only for French<br/>
 * 
 * IMPORTANT:
 * The LIA NE script calls several subprograms, in particular the LIA TAGG <code>nomb2alpha</code> program. This program converts any
 * number/digit into a sequences of (letters) words (e.g. "1978" becomes "mille neuf cent soixante dix huit"). This is the reason why the 
 * annotated output text is different from the original one. Therefore the NE tagging CAN NOT BE represented in the input text contained in
 * the CAS, this is why this AE CREATES A NEW VIEW containing the result. This is a very special behaviour but there is no general way
 * to correctly annotate the input text, because it is not possible to match the input and output texts (for example if the original text is
 * "En 2010 23 personnes sont mortes de rire", then in the output there is no way to know whre is the frontier between "2010" and "23" in 
 * "deux mille dix ving trois").
 * As a second consequence, the text written in the new view is tokenized arbitrarily with one token by line (because again it is not possible
 * to find the exact original form of whitespace separators). 
 * 
 * @author erwan
 *
 */
public class LiaNERawTextAE extends LipnLiaExternalProgramGenericAnnotator {

	public static final String LIA_NE_RAW_TEXT_COMPONENT_ID = LiaNERawTextAE.class.getName();
	public static final String LIA_NE_ENCODING = LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENCODING;
	public static final String LIA_NE_LANG="french";
	public static final String ALLOWED_LANGUAGE="fr";
	public static final double LIA_NE_CONFIDENCE = LipnLiaExternalProgramGenericAnnotator.DEFAULT_LIA_CONFIDENCE;
	public static final String EXTERNAL_PROGRAM_NAME = "LIA_NE";
	
	static final String LIA_NE_EXEC_NAME = "script/lia_ne_tagg_txt";
	
	public static final String LIA_NE_LB_EMPTY_SENTENCES_TEXT = "--LB--";
	public static final String TOKENS_SEPARATOR_IN_NEW_VIEW = LINE_SEPARATOR;

	File liaTaggDir;
	File crfPPDir;
	String outputViewName;
	boolean removeLBEmptySentences;
	boolean keepSentencesTags;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		super.initCommonParameters(context, LIA_NE_DIR_PARAM_NAME, LIA_NE_EXEC_NAME, EXTERNAL_PROGRAM_NAME);
		
		liaTaggDir = new File((String) context.getConfigParameterValue(LIA_TAGG_DIR_PARAM_NAME));			
		outputViewName = (String) context.getConfigParameterValue("OutputViewName");			
		crfPPDir = new File((String) context.getConfigParameterValue("CRFPlusPlusDir"));
		
		removeLBEmptySentences = ((Boolean) context.getConfigParameterValue("RemoveEmptySentencesAfterDocumentEnd"));
		keepSentencesTags = ((Boolean) context.getConfigParameterValue("KeepSentencesAnnotations"));
		if (!liaTaggDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{liaTaggDir.getAbsolutePath()});
		}		
		if (!crfPPDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{crfPPDir.getAbsolutePath()});
		}		

	}
	
	
	public void process(JCas aJCas)  throws AnalysisEngineProcessException  {

		String language = ensureLanguage(null, aJCas, null);
		if (!language.equals(ALLOWED_LANGUAGE)) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{LIA_NE_RAW_TEXT_COMPONENT_ID, language, ALLOWED_LANGUAGE});
		}
		
		ExternalProgram p = initProgram(aJCas, liaTaggDir, getProgramDir());
		try {
			String currentPathEnvVar = p.environment().get("PATH");
			p.environment().put("PATH", currentPathEnvVar+":"+crfPPDir.getCanonicalPath()+"/bin");
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}

		// CREATE NEW VIEW
		JCas newView;
		try {
			newView = aJCas.createView(outputViewName);
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "cas_exception", new Object[]{"error trying to create view '"+outputViewName+"'"}, e);
		} catch (CASRuntimeException cre) {
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "view_already_exists", new Object[]{"error trying to create view '"+outputViewName+"'"}, cre);
		}
		
		// main call
		runProgram(aJCas, new StringWriterFeeder(aJCas.getDocumentText()), new LiaNeRawTextAnnotatedReader(newView, aJCas.getDocumentText().length()));
	}
	

	
	protected class LiaNeRawTextAnnotatedReader implements ReaderConsumer {

		JCas aJCas;
		int originalViewSize;
		
		public LiaNeRawTextAnnotatedReader(JCas aJCas, int originalViewSize) {
			this.aJCas = aJCas;
			this.originalViewSize = originalViewSize;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			try {
				SimpleTaggedTextReader aligner = new SimpleTaggedTextReader(reader, new LiaNERawFilter());
				aligner.align(new VoidInputReader(aligner),
						      new TagPositionConsumer(new LiaNeRawTextAnnotReceiver(aJCas), true));			
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}
		}

		protected class LiaNeRawTextAnnotReceiver implements TagPositionConsumer.AnnotationReceiver {
			
			JCas aJCas;
			ArrayList<GenericAnnotation> annotsToAdd;
			StringBuilder newViewContent;
			/*
			 * Note: the position received from the TagPositionConsumer object is based on the 
			 * text transmitted to the AnnotatedTextReader, which is different from the one stored in the
			 * new view (and from the one in the original view also of course) because the "separators" (text
			 * between tokens, usually whitespaces) are not transmitted.
			 * Therefore we must set the right position depending on the AnnotatedTextReader position (which is
			 * provided), that's why a map is maintained to find the corresponding position in all cases.
			 *   
			 */
			HashMap<Integer, Integer> mapStartIndex;
			HashMap<Integer, Integer> mapEndIndex;
			int currentViewPos;
			int currentAnnotatedTextPos;
			
			public LiaNeRawTextAnnotReceiver(JCas aJCas) {
				this.aJCas = aJCas;
			}
			
			public void initPositionConsumer() throws AlignmentException {
				currentViewPos = 0;
				newViewContent = new StringBuilder(originalViewSize);
				mapStartIndex = new HashMap<Integer, Integer>(); 
				mapEndIndex = new HashMap<Integer, Integer>(); 
				annotsToAdd = new ArrayList<GenericAnnotation>();
			}

			
			public void addUnannotatedToken(String inputToken, String annotatedToken, long start, long end) throws AlignmentException {
				mapStartIndex.put((int) start, currentViewPos);
				newViewContent.append(annotatedToken);
				currentViewPos += annotatedToken.length();
				mapEndIndex.put((int) end, currentViewPos);
				newViewContent.append(TOKENS_SEPARATOR_IN_NEW_VIEW);
				currentViewPos += TOKENS_SEPARATOR_IN_NEW_VIEW.length();
				currentAnnotatedTextPos = (int) end;
				getLogger().log(Level.FINEST,"Token read: '"+annotatedToken+"' ("+start+"-"+end+" -> "+mapStartIndex.get((int) start)+"-"+mapEndIndex.get((int) end)+").");
			}
			
			
			protected int findNewViewTokenIndex(int oldIndex, HashMap<Integer, Integer> mapping, int direction) throws AlignmentException {
				int index = oldIndex;
				Integer newIndex = mapping.get(index);
				while ((newIndex == null) && ((direction!=-1) || (index>=0)) && ((direction!=1) || (index<=currentAnnotatedTextPos))) {
					index += direction;
					newIndex = mapping.get(index);
				}
				if ((index < 0) || (index > currentAnnotatedTextPos)) {
// Finally no warning: the position is wrong but it will be checked later (case of the extra sentences after the end)
//					getContext().getLogger().log(Level.WARNING, "No matching position found in new view for index "+oldIndex+" (direction is "+direction+")");
					newIndex = (direction==-1)?0:currentViewPos;
				}
				return newIndex;
			}
			
			/**
			 * Called by the position consumer (final step of the re-alignment process)
			 */
			public void addAnnotation(String content, long start, long end) throws AlignmentException {
//				getContext().getLogger().log(Level.FINEST,"receiving: '"+content+"' ("+start+"-"+end+").");
				int newViewStart = findNewViewTokenIndex((int) start, mapStartIndex, +1);
				int newViewEnd = findNewViewTokenIndex((int) end, mapEndIndex, -1);
				if (content.equals("s")) {
					if (keepSentencesTags) {
						if (newViewStart < newViewEnd) { // otherwise this is an extra sentence to remove
							synchronized (aJCas) {
								Sentence s = new Sentence(aJCas);
								setGenericAttributes(s, newViewStart, newViewEnd);
								annotsToAdd.add(s);
							}
							getLogger().log(Level.FINEST,"Sentence found ("+newViewStart+"-"+newViewEnd+").");
						}
					}
				} else {
					synchronized (aJCas) {
						NamedEntity ne = new NamedEntity(aJCas);
						setGenericAttributes(ne, newViewStart, newViewEnd);
						ne.setValue(content);
						annotsToAdd.add(ne);
					}
					getLogger().log(Level.FINEST,"Entity found: '"+content+"' ("+newViewStart+"-"+newViewEnd+").");
				}
			}
			
			public void closePositionConsumer() throws AlignmentException {
				synchronized (aJCas) {
					aJCas.setDocumentText(newViewContent.toString());
					for (GenericAnnotation a : annotsToAdd) {
						a.addToIndexes();
					}
				}
				getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");

			}

		}

	}

	
	
	protected class LiaNERawFilter implements FilterTokenAndAnnot {
		
		boolean dontSend = false;
		
		public String filteredToken(String token, String annot) {
			if ((removeLBEmptySentences) && (token != null) && (token.equals(LIA_NE_LB_EMPTY_SENTENCES_TEXT))) {
				if (annot == null) {
					dontSend = true;
				}
				return null;
			} else {
				return token;
			}
		}
		
		public String filteredAnnot(String token, String annot) {
			if ((!keepSentencesTags) && (annot != null) && (annot.equals(LIA_TAGG_SENTENCE_OPEN_TAG) || annot.equals(LIA_TAGG_SENTENCE_CLOSE_TAG))) {
				if (token == null) {
					dontSend = true;
				}
				return null;
			} else {
				return annot;
			}
		}
		
		public boolean sendTokenAndAnnot() {
			if (dontSend) {
				dontSend = false;
				return false;
			} else {
				return true;
			}
		}

	}

}
