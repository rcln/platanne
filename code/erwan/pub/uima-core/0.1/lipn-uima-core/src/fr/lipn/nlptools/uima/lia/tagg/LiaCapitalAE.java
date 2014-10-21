package fr.lipn.nlptools.uima.lia.tagg;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.FSIteratorFactory;
import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.lia.common.LiaPositionedTextTokenIterator;
import fr.lipn.nlptools.uima.lia.common.LiaTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.LiaCleaned;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
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
public class LiaCapitalAE extends LipnLiaExternalProgramGenericAnnotator {

	public final String AE_NAME = getDefaultComponentId();
	static final String LIA_CAPITAL_EXEC_NAME = "bin/lia_nett_capital";

	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		super.initCommonParameters(context, null, LIA_CAPITAL_EXEC_NAME, AE_NAME);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		ExternalProgram p = initProgram(aJCas);
		p.getCommand().add(getResourceName(LipnLiaExternalProgramGenericAnnotator.LEXITAB_RESOURCE_ID));
		WriterFeeder writerFeeder = new TokenByLineWriterFeeder( new LiaTokenPOSLemmaIterator(aJCas, false, false, true),LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR); 
		runProgram(aJCas, writerFeeder, new LiaCapitalAnnotatedReader(aJCas));

	}

	
	protected class LiaCapitalAnnotatedReader implements ExternalProgram.ReaderConsumer {

		JCas aJCas;
		
		public LiaCapitalAnnotatedReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			try {
				SimpleTokenByLineReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), new LipnLiaExternalProgramGenericAnnotator.LiaInputFilter());
				aligner.align(new TokenIteratorInputReader(new LiaPositionedTextTokenIterator(aJCas),
						                                   (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null)),
						      new LiaCapitalConsumer(aJCas));
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}
		}
		
	}
	
	protected class LiaCapitalConsumer implements AlignerConsumer {
		
		JCas aJCas;
		FSIterator cleanedIterator;
		LiaCleaned currCleaned;
		int currCleanedStart;
		int currCleanedEnd;
		ArrayList<GenericAnnotation> annotsToAdd;
		int nbModified = 0;
		int nbAdded = 0;
		
		public LiaCapitalConsumer(JCas aJCas) {
			this.aJCas = aJCas;
		}
				
		public void initConsumer() throws AlignmentException {
			cleanedIterator = FSIteratorFactory.createFSIterator(aJCas, LiaCleaned.type, true);
			annotsToAdd = new ArrayList<GenericAnnotation>();
			if (cleanedIterator.hasNext()) {
				currCleaned = (LiaCleaned) cleanedIterator.next();
				synchronized (aJCas) {
					currCleanedStart = currCleaned.getBegin();
					currCleanedEnd = currCleaned.getEnd();
							}
			} else {
				currCleaned = null;
			}
		}
		
		public void closeConsumer() throws AlignmentException {
			synchronized (aJCas) {
				for (GenericAnnotation a : annotsToAdd) {
					a.addToIndexes();
				}
			}
			getContext().getLogger().log(Level.INFO,""+nbModified+" annotations have been modified, "+nbAdded+" annotations have been added.");
		}

		// annotations are simply ignored
		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
						
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if ((annotatedToken != null) && !annotatedToken.equals(inputToken)) { // otherwise nothing to do
				while ((currCleaned != null) && (currCleanedStart < startPos)) { // find next possible existing "LiaCleaned"
					if (cleanedIterator.hasNext()) {
						currCleaned = (LiaCleaned) cleanedIterator.next();
						synchronized (aJCas) {
							currCleanedStart = currCleaned.getBegin();
							currCleanedEnd = currCleaned.getEnd();
						}
					} else {
						currCleaned = null;
					}					
				}
				if ((currCleaned != null) && (currCleanedStart == startPos) && (currCleanedEnd == endPos)) {
					getContext().getLogger().log(Level.FINEST,"Modifying existing LiaCleaned for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+annotatedToken+"'");
					synchronized (aJCas) { 
						currCleaned.removeFromIndexes();
						currCleaned.setValue(annotatedToken);
						currCleaned.addToIndexes();
					}
					nbModified++;
				} else {
					synchronized (aJCas) {
						LiaCleaned n = new LiaCleaned(aJCas);
						setGenericAttributes(n,(int)  startPos, (int) endPos);
						n.setValue(annotatedToken);
						getContext().getLogger().log(Level.FINEST,"new LiaCleaned for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+annotatedToken+"'");
						annotsToAdd.add(n);
					}
					nbAdded++;
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


	
	

}
