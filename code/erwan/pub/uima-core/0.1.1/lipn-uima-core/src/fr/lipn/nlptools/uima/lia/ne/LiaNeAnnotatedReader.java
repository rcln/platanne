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
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;

/**
 * 
 * 
 * Remark: this <code>ReaderConsumer</code> class was made independent (contrary to most other wrapper AEs <code>ReaderConsumer</code> components)
 * because it was used by two AEs: {@link LiaNeTaggedTextAE} and {@link LiaNeRawTextAE}.
 * But since LIA NE version 2.2 the result of the latter can not parsed the same way, so this class is currently used only by {@link LiaNeTaggedTextAE}.
 * Neverthess it is still independent in the case some future version of LIA NE would permit to come back to the case where both can be parsed the same way. 
 * @author moreau
 *
 */
public class LiaNeAnnotatedReader implements ReaderConsumer {

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
					      new TagPositionConsumer(new LiaNeAnnotReceiver(aJCas), false));			
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

		public void addUnannotatedToken(String inputToken, String annotatedToken, long start, long end) throws AlignmentException {
		}
		
	}

}

