package fr.lipn.nlptools.uima.lia.tagg;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.lia.common.LiaPositionedTextTokenIterator;
import fr.lipn.nlptools.uima.lia.common.LiaTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.AnnotatedTextReader;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.align.TokenIteratorInputReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder;

/**
 * TODO doc
 * 
 * @author moreau
 *
 */
public class LiaSentenceAE extends LipnLiaExternalProgramGenericAnnotator {

	public final String AE_NAME = getDefaultComponentId();
	static final String LIA_SENTENCE_EXEC_NAME = "bin/lia_sentence";
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initCommonParameters(context, null, LIA_SENTENCE_EXEC_NAME, AE_NAME);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		ExternalProgram p = initProgram(aJCas);
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LISTE_CHIF_RESOURCE_ID));
		WriterFeeder writerFeeder = new TokenByLineWriterFeeder( new LiaTokenPOSLemmaIterator(aJCas, false, false, false),LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR); 
		runProgram(aJCas, writerFeeder, new LiaSentenceAnnotatedReader(aJCas));
	}

	
	protected class LiaSentenceAnnotatedReader implements ExternalProgram.ReaderConsumer {

		JCas aJCas;
		
		public LiaSentenceAnnotatedReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			try {
				AnnotatedTextReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), null, new LipnLiaExternalProgramGenericAnnotator.LiaInputFilter(), LIA_TOKENIZED_TEXT_SEPARATOR_REGEX);
				aligner.align(new TokenIteratorInputReader(new LiaPositionedTextTokenIterator(aJCas),
						                                   (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null)),
						                                   new LiaSentenceConsumer(aJCas));
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}
		}
		
	}

	
	
	protected class LiaSentenceConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<GenericAnnotation> annotsToAdd;
		long startSentence = -1;
		
		public LiaSentenceConsumer(JCas aJCas) {
			this.aJCas = aJCas;
		}

		public void initConsumer() throws AlignmentException {
			annotsToAdd = new ArrayList<GenericAnnotation>();
		}

		public void closeConsumer() throws AlignmentException {
			if (startSentence != -1) {
				throw new AlignmentException("Last sentence starting at position "+startSentence+" is not closed.");
			}
			synchronized (aJCas) {
				for (GenericAnnotation a : annotsToAdd) {
					a.addToIndexes();
				}
			}
			getContext().getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");
		}

		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if (annotation != null) {
				if (annotation.equals(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_OPEN_TAG)) {
					if (startSentence != -1) {
						throw new AlignmentException("Aligner consumer for "+getDefaultComponentId()+": Starting a new sentence at position "+startPos+", but previous sentence is not closed.");
					}
					startSentence = startPos;
				} else if (annotation.equals(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_CLOSE_TAG)) {
					if (startSentence != -1) {
						synchronized (aJCas) {
							Sentence s = new Sentence(aJCas);
							setGenericAttributes(s, (int) startSentence, (int) endPos);
							annotsToAdd.add(s);
						}
						startSentence = -1;
					} else {
						throw new AlignmentException("Aligner consumer for "+getDefaultComponentId()+": Ending a sentence with no starting point at position "+endPos+".");
					}
				} else {// should never happen
					throw new AlignmentException("Aligner consumer for "+getDefaultComponentId()+" receives unexpected data as annotation: "+annotation);
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
