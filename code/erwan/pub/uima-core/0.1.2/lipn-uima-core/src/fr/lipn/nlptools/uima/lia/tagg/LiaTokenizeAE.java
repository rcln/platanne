package fr.lipn.nlptools.uima.lia.tagg;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.LiaCleaned;
import fr.lipn.nlptools.uima.types.Token;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.AnnotatedTextReader;
import fr.lipn.nlptools.utils.align.FilterTokenAndAnnot;
import fr.lipn.nlptools.utils.align.SimpleInputReader;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.ReplaceCharWriterFeeder;
import fr.lipn.nlptools.utils.externprog.StringWriterFeeder;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * TODO doc
 * @author moreau
 *
 */
public class LiaTokenizeAE extends LipnLiaExternalProgramGenericAnnotator {

	public final String AE_NAME = getDefaultComponentId();
	static final String LIA_TOKENIZE_EXEC_NAME = "bin/lia_tokenize";
	public static final char LIA_APOS = '\''; 
	
	boolean applyLiaTransAPos = false;
	boolean useLiaNeBigLexData = false;
	File liaNeDir = null;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initCommonParameters(context, null, LIA_TOKENIZE_EXEC_NAME, AE_NAME);
		
		if (((Boolean) context.getConfigParameterValue("UseLiaNeBigLex")).booleanValue()) {
			applyLiaTransAPos = true;
			useLiaNeBigLexData = true;
		}
		if (useLiaNeBigLexData) {
			if (context.getConfigParameterValue("LiaNEDir") != null) {
				liaNeDir = new File((String) context.getConfigParameterValue("LiaNEDir"));
				if (!liaNeDir.exists()) {
					throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{(String) context.getConfigParameterValue("LiaNEDir")});
				}		
			} else {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{null, "LiaNEDir", "must be set if UseLiaNeBigLexTokenization is enabled"});
			}

		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {		

		ExternalProgram p = initProgram(aJCas, getProgramDir(), liaNeDir);
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXITAB_RESOURCE_ID));
		WriterFeeder feeder;
		if (applyLiaTransAPos) {
			feeder = new ReplaceCharWriterFeeder(aJCas.getDocumentText(), LIA_APOS, LIA_APOS+" ");
		} else {
			feeder = new StringWriterFeeder(aJCas.getDocumentText());
		}
		runProgram(aJCas, feeder, new LiaTokenizeAnnotatedReader(aJCas));

	}

	
	protected class LiaTokenizeAnnotatedReader implements ReaderConsumer {

		JCas aJCas;
		
		public LiaTokenizeAnnotatedReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			// Important : this LIA tokenizer does not add any annotation (tag) in the text, it only adds whitespaces
			FilterTokenAndAnnot filter = null;
			if (applyLiaTransAPos) {
				filter = new LiaTransAPosOutputTokenFilter(""+LIA_APOS+LIA_MULTIWORDS_CHAR, ""+LIA_APOS);
			}
			try {
				AnnotatedTextReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), null,  filter, LIA_TOKENIZED_TEXT_SEPARATOR_REGEX);
				aligner.align(new SimpleInputReader(new java.io.StringReader(aJCas.getDocumentText()),
						                            null,
						                            (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null),
						                            LipnLiaExternalProgramGenericAnnotator.LIA_MULTIWORDS_CHAR), 
						      new LiaTokenizeConsumer(aJCas));
			} catch (AlignmentException ale) {
				throw new ExternalProgramException("An alignment error occured.", ale);
			}
		}
		
	}


	
	protected class LiaTokenizeConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<GenericAnnotation> annotsToAdd;
		int nbT, nbC = 0;
		
		public LiaTokenizeConsumer(JCas aJCas) {
			this.aJCas = aJCas;
		}
				
		public void initConsumer() throws AlignmentException {
			annotsToAdd = new ArrayList<GenericAnnotation>();
		}
		
		public void closeConsumer() throws AlignmentException {
			synchronized (aJCas) {
				for (GenericAnnotation a : annotsToAdd) {
					a.addToIndexes();
				}
			}
			getContext().getLogger().log(Level.INFO,""+nbT+" Token and "+nbC+" Cleaned annotations have been written.");
		}


		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			//System.out.println("Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if (annotation != null) { //should never happen 
				throw new AlignmentException("Aligner consumer for "+LIA_TOKENIZE_EXEC_NAME+" receives unexpected data as annotation: "+annotation);
			} else { // simple tokens
				if (inputToken != null) {
					synchronized (aJCas) {
						Token t = new Token(aJCas);
						setGenericAttributes(t, (int) startPos, (int) endPos);
						getContext().getLogger().log(Level.FINEST,"new Token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+annotation+"'");
						annotsToAdd.add(t);
						nbT++;
						if (!annotatedToken.equals(inputToken)) {  // if LIA clean tool modifies the token, keep the modified version (special annotation for this purpose)
							LiaCleaned n = new LiaCleaned(aJCas);
							setGenericAttributes(n, (int) startPos, (int) endPos);
							n.setValue(annotatedToken);
							getContext().getLogger().log(Level.FINEST,"new LiaCleaned for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+annotatedToken+"'");
							annotsToAdd.add(n);
							nbC++;
						}
					}
				}				
			}
		}

		public boolean requiresPosition() {
			return true;
		}

		public boolean requiresSeparator() {
			return false;
		}

		public boolean requiresUnannotatedTokens() {
			return true;
		}

	}
	
	protected class LiaTransAPosOutputTokenFilter implements FilterTokenAndAnnot {
		
		String searchedSequence;
		String replaceWith;

		public LiaTransAPosOutputTokenFilter(String searchedSequence, String replaceWith) {
			this.searchedSequence = searchedSequence;
			this.replaceWith = replaceWith;
		}
		
		public String filteredToken(String token, String annot) {
			return token.replaceAll(searchedSequence, replaceWith);
		}
		
		public String filteredAnnot(String token, String annot) {
			return annot;
		}
		
		public boolean sendTokenAndAnnot() {
			return true;
		}

	}

}
