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
import fr.lipn.nlptools.uima.types.PartOfSpeech;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.align.TokenIteratorInputReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * This AE is a wrapper for the LIA POS tagger. French and English.
 * Should be called after tokenizing (words and sentences) and cleaning with {@link LiaCapitalAE}.
 * It is worth noticing that LIA uses its own set of POS labels.
 * 
 * NB: LIA TAGG '-guess' option is set by default for French.
 * 
 * @author moreau
 *
 */
public class LiaTaggAE extends LipnLiaExternalProgramGenericAnnotator {
	
	public final String AE_NAME = getDefaultComponentId();
	static final String LIA_TAGG_EXEC_NAME = "bin/lia_quicktagg";

	boolean useLiaNeBigLexData = false;
	File liaNeDir = null;
	String[][] sentenceFilter; 
	String[][] tokenFilter;

	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initCommonParameters(context, null, LIA_TAGG_EXEC_NAME, AE_NAME);

		setGuessOption(context);
		
		if (((Boolean) context.getConfigParameterValue("UseLiaNeBigLex")).booleanValue()) {
			useLiaNeBigLexData = true;
		}
		sentenceFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("SentenceFilter"),"SentenceFilter");
		tokenFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("TokenFilter"),"TokenFilter");

		if (useLiaNeBigLexData) {
			if (context.getConfigParameterValue("LiaNEDir") != null) {
				liaNeDir = new File((String) context.getConfigParameterValue("LiaNEDir"));
				if (!liaNeDir.exists()) {
					throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{liaNeDir.getName()});
				}		
			} else {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{null, "LiaNEDir", "must be set if UseLiaNeBigLexTokenization is enabled"});
			}

		}

	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		ExternalProgram p = initProgram(aJCas, getProgramDir(), liaNeDir);
		p.getCommand().add("-lextag");
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXTAG_RESOURCE_ID));
		p.getCommand().add("-morpho");
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.MORPHO_RESOURCE_ID));
		p.getCommand().add("-lexgraf");
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXGRAPH_RESOURCE_ID));
		p.getCommand().add("-pmc");
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXI_RESOURCE_ID));
		p.getCommand().add("-ml");
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.TRIGRAM_RESOURCE_ID));
		
		FSMatchConstraint sentenceConstraint = createAnnotationFilterConstraint(aJCas, sentenceFilter);
		FSMatchConstraint tokenConstraint = createAnnotationFilterConstraint(aJCas, tokenFilter);
		WriterFeeder writerFeeder = new TokenByLineWriterFeeder( new LiaTokenPOSLemmaIterator(aJCas, false, false, true, sentenceConstraint, tokenConstraint, null, null), LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR); 
		runProgram(aJCas, writerFeeder, new LiaTaggAnnotatedReader(aJCas));

	}

	
	protected class LiaTaggAnnotatedReader implements ReaderConsumer {

		JCas aJCas;
		
		public LiaTaggAnnotatedReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			FSMatchConstraint sentenceConstraint = createAnnotationFilterConstraint(aJCas, sentenceFilter);
			FSMatchConstraint tokenConstraint = createAnnotationFilterConstraint(aJCas, tokenFilter);
			try {
				SimpleTokenByLineReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), new LipnLiaExternalProgramGenericAnnotator.LiaInputFilter());
				aligner.align(new TokenIteratorInputReader(new LiaPositionedTextTokenIterator(aJCas, sentenceConstraint, tokenConstraint),
						                                   (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null)),
						      new LiaTaggConsumer(aJCas));
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}
		}
		
	}
	
	protected class LiaTaggConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<GenericAnnotation> annotsToAdd;
		
		public LiaTaggConsumer(JCas aJCas) {
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
				synchronized (aJCas) {
					PartOfSpeech pos = new PartOfSpeech(aJCas);
					setGenericAttributes(pos, (int) startPos, (int) endPos);
					pos.setValue(annotation);
					getContext().getLogger().log(Level.FINEST,"new PartOfSpeech for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+annotation+"'");
					annotsToAdd.add(pos);
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