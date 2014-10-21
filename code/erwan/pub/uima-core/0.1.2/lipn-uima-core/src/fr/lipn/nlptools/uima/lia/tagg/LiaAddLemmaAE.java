package fr.lipn.nlptools.uima.lia.tagg;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.lia.common.LiaPositionedTextTokenIterator;
import fr.lipn.nlptools.uima.lia.common.LiaTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.Lemma;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.align.TokenIteratorInputReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * TODO doc
 * 
 * @author moreau
 *
 */
public class LiaAddLemmaAE extends LipnLiaExternalProgramGenericAnnotator {
	
	static final String LIA_ADD_LEMMA_EXEC_NAME = "bin/lia_rajoute_lemme_ecg";
	final String AE_NAME = getDefaultComponentId();

	boolean useLiaNeBigLexData = false;
	File liaNeDir = null;
	String[][] sentenceFilter; 
	String[][] tokenFilter;
	String[][] posFilter;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initCommonParameters(context, null, LIA_ADD_LEMMA_EXEC_NAME, AE_NAME);

		sentenceFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("SentenceFilter"),"SentenceFilter");
		tokenFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("TokenFilter"),"TokenFilter");
		posFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("PartOfSpeechFilter"),"PartOfSpeechFilter");

		if (((Boolean) context.getConfigParameterValue("UseLiaNeBigLex")).booleanValue()) {
			useLiaNeBigLexData = true;
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
		
		FSMatchConstraint sentenceConstraint = createAnnotationFilterConstraint(aJCas, sentenceFilter);
		FSMatchConstraint tokenConstraint = createAnnotationFilterConstraint(aJCas, tokenFilter);
		FSMatchConstraint posConstraint = createAnnotationFilterConstraint(aJCas, posFilter);

		ExternalProgram p = initProgram(aJCas, getProgramDir(), liaNeDir);
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXGRAPH_RESOURCE_ID));
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXTAG_RESOURCE_ID));
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXI_RESOURCE_ID));
		WriterFeeder writerFeeder = new TokenByLineWriterFeeder( new LiaTokenPOSLemmaIterator(aJCas, true, false, true, sentenceConstraint, tokenConstraint, posConstraint, null), LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR); 
		runProgram(aJCas, writerFeeder, new LiaAddLemmaAnnotatedReader(aJCas));

	}

	
	protected class LiaAddLemmaAnnotatedReader implements ReaderConsumer {

		JCas aJCas;
		
		public LiaAddLemmaAnnotatedReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			FSMatchConstraint sentenceConstraint = createAnnotationFilterConstraint(aJCas, sentenceFilter);
			FSMatchConstraint tokenConstraint = createAnnotationFilterConstraint(aJCas, tokenFilter);
			try {
				SimpleTokenByLineReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), new LipnLiaExternalProgramGenericAnnotator.LiaInputFilter());
				aligner.align(new TokenIteratorInputReader(new LiaPositionedTextTokenIterator(aJCas, sentenceConstraint, tokenConstraint),
						                                   (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null)),
						      new LiaAddLemmaConsumer(aJCas));
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}
		}
		
	}
	
	protected class LiaAddLemmaConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<GenericAnnotation> annotsToAdd;

		
		public LiaAddLemmaConsumer(JCas aJCas) {
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
			getContext().getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");

		}

		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if ((annotation != null) && (!annotation.equals(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_OPEN_TAG)) && (!annotation.equals(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_CLOSE_TAG))) {
				int separatorPos = annotation.lastIndexOf(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR) + LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR.length();
				String lemmaValue = annotation.substring(separatorPos);
				synchronized (aJCas) {
					Lemma l = new Lemma(aJCas);
					setGenericAttributes(l, (int) startPos, (int) endPos);
					l.setValue(lemmaValue);
					getContext().getLogger().log(Level.FINEST,"Lemma found for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+lemmaValue+"'");
					annotsToAdd.add(l);
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
			return false;
		}

	}

}
