package fr.lipn.nlptools.uima.lia.ne;

import java.io.Reader;
import java.util.ArrayList;

import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.NamedEntity;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.SimpleInputReader;
import fr.lipn.nlptools.utils.align.SimpleTaggedTextReader;
import fr.lipn.nlptools.utils.align.TagPositionConsumer;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;

public class LiaNeAnnotatedReader implements ExternalProgram.ReaderConsumer {

	static final char[] ignoredChars = { '.', '_' };

	JCas aJCas;
	LipnLiaExternalProgramGenericAnnotator caller;
	
	public LiaNeAnnotatedReader(JCas aJCas, LipnLiaExternalProgramGenericAnnotator caller) {
		this.aJCas = aJCas;
		this.caller = caller;
	}
	
	public void consumeReader(Reader reader) throws ExternalProgramException {
		try {
			SimpleTaggedTextReader aligner = new SimpleTaggedTextReader(reader, ignoredChars);
			aligner.align(new SimpleInputReader(new java.io.StringReader(aJCas.getDocumentText()), 
					                            ignoredChars,
					                            (caller.isCodingErrorActionReplace()?new char[]{caller.getCodingErrorReplacementValue()}:null)),
					      new TagPositionConsumer(new LiaNeAnnotReceiver(aJCas)));			
		} catch (AlignmentException ae) {
			throw new ExternalProgramException("An alignment error occured.", ae);
		}
	}

	protected class LiaNeAnnotReceiver implements TagPositionConsumer.AnnotationReceiver {
		
		JCas aJCas;
		ArrayList<GenericAnnotation> annotsToAdd;
		
		public LiaNeAnnotReceiver(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void initPositionConsumer() throws AlignmentException {
			annotsToAdd = new ArrayList<GenericAnnotation>();
		}

		/**
		 * Called by the position consumer (final step of the re-alignment process)
		 */
		public void addAnnotation(String content, long start, long end) throws AlignmentException {
			if (!content.equals("s")) { // les annots 'sentence' sont ignorées
				synchronized (aJCas) {
					NamedEntity ne = new NamedEntity(aJCas);
					caller.setGenericAttributes(ne, (int) start, (int) end);
					ne.setValue(content);
					annotsToAdd.add(ne);
				}
				caller.getLogger().log(Level.FINEST,"Entity found: '"+content+"' ("+start+"-"+end+").");
			}
		}
		
		public void closePositionConsumer() throws AlignmentException {
			synchronized (aJCas) {
				for (GenericAnnotation a : annotsToAdd) {
					a.addToIndexes();
				}
			}
			caller.getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");

		}

	}

}

