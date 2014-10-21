package fr.lipn.nlptools.uima.common;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.types.AnnotationTag;
import fr.lipn.nlptools.uima.types.Interpretation;

public class POSLemmaPairInterpretationTokenPOSLemmaIterator extends BasicTokenPOSLemmaIterator {
	
	public static int POS_INDEX = 0;
	public static int LEMMA_INDEX = 1;
	
	Interpretation currentPOSLemmaPair = null;
	
	
	public POSLemmaPairInterpretationTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe, String sentenceComponentIdFilter, String tokenComponentIdFilter, String posComponentIdFilter, String lemmaComponentIdFilter) {
		super(aJCas, range, withPOS, withLemma,  iterateSentences, regexps, replaceWith, threadSafe,
				(sentenceComponentIdFilter==null)?null:FSIteratorFactory.createComponentIdFilterConstraint(aJCas, sentenceComponentIdFilter, threadSafe),
				(tokenComponentIdFilter==null)?null:FSIteratorFactory.createComponentIdFilterConstraint(aJCas, tokenComponentIdFilter, threadSafe),
				(posComponentIdFilter==null)?null:FSIteratorFactory.createComponentIdFilterConstraint(aJCas, posComponentIdFilter, threadSafe),
				(lemmaComponentIdFilter==null)?null:FSIteratorFactory.createComponentIdFilterConstraint(aJCas, lemmaComponentIdFilter, threadSafe)
		);
	}

	public POSLemmaPairInterpretationTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, range, withPOS, withLemma,  iterateSentences, regexps, replaceWith, threadSafe, null, null, null, null);
	}

	public POSLemmaPairInterpretationTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, false, regexps, replaceWith, threadSafe, null, null, null, null);
	}

	public POSLemmaPairInterpretationTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, false, null, null, threadSafe);
	}
	
	@Override
	protected String getPOS(Annotation token) {
		currentPOSLemmaPair = null;
		FSIterator<Annotation> iter = FSIteratorFactory.createFSIterator(aJCas, posType, token, threadSafe, posConstraint);
		// if zero or more than a single POS, then look for Interpretation
		Annotation pos = null;
		boolean singlePos = true;
		if (iter.hasNext()) {
			pos = iter.next();
		} else {
			singlePos = false;
		}
		if (iter.hasNext()) {
			singlePos = false;
		}
		if (singlePos) {
			return ((AnnotationTag) pos).getValue();
		} else {
			FSIterator<Annotation> iterInterpretations = FSIteratorFactory.createFSIterator(aJCas, Interpretation.type, null, threadSafe, 
					FSIteratorFactory.createWindowConstraint(aJCas, token.getBegin(), token.getEnd(), true, threadSafe));
			double maxProb = Double.NEGATIVE_INFINITY;
			while (iterInterpretations.hasNext()) {
				Interpretation i = (Interpretation) iterInterpretations.next();
				if (Double.isNaN(i.getConfidence())) {
					currentPOSLemmaPair = i;
				} else {
					if (i.getConfidence() > maxProb) {
						maxProb = i.getConfidence();
						currentPOSLemmaPair = i;
					}
				}
			}
			if (currentPOSLemmaPair == null) {
				return null;
			} else {
				if (currentPOSLemmaPair.getSerie().size() < 2) { // RAISE EXCEPTION WOULD BE BETTER !!!
					currentPOSLemmaPair = null;
					return null;
				} else {
					return ((AnnotationTag) currentPOSLemmaPair.getSerie(POS_INDEX)).getValue();
				}
			}
			
		}
		
	}

	@Override
	protected String getLemma(Annotation token) {
		if (currentPOSLemmaPair == null) {
			FSIterator<Annotation> iter = FSIteratorFactory.createFSIterator(aJCas, lemmaType, token, threadSafe, lemmaConstraint);
			// if zero or more than a single POS, then look for Interpretation
			if (iter.hasNext()) {
				return ((AnnotationTag) iter.next()).getValue();
			} else {
				return null;
			}
		} else {
			return ((AnnotationTag) currentPOSLemmaPair.getSerie(LEMMA_INDEX)).getValue();
		}
	}


	
}
