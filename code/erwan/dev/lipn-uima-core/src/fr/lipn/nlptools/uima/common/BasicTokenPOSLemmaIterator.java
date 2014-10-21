package fr.lipn.nlptools.uima.common;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.types.AnnotationTag;

/**
 * Implements {@link TokenPOSLemmaIterator} using LIPN standard types. Can handle multiple POS or lemma annotations for a given token: in this case
 * returns the most likely one, or an arbitrary one if this is not possible.
 * 
 * @author erwan
 *
 */
public class BasicTokenPOSLemmaIterator extends TokenPOSLemmaIterator {

	public BasicTokenPOSLemmaIterator(JCas aJCas, Annotation range, int start, int end, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint, FSMatchConstraint posConstraint, FSMatchConstraint lemmaConstraint) {
		super(aJCas, range, start, end, withPOS, withLemma, iterateSentences, regexps, replaceWith, threadSafe, sentenceConstraint, tokenConstraint, posConstraint, lemmaConstraint);
	}

	
	public BasicTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint, FSMatchConstraint posConstraint, FSMatchConstraint lemmaConstraint) {
		this(aJCas, range, -1, -1, withPOS, withLemma, iterateSentences, regexps, replaceWith, threadSafe, sentenceConstraint, tokenConstraint, posConstraint, lemmaConstraint);
	}

	public BasicTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, range, withPOS, withLemma, iterateSentences, regexps, replaceWith, threadSafe, null, null, null, null);
	}

	public BasicTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, false, regexps, replaceWith, threadSafe);
	}

	
	public BasicTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, boolean iterateSentences, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, iterateSentences, null, null, threadSafe);
	}

	public BasicTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, false, null, null, threadSafe);
	}

	
	protected FSIterator<Annotation> getTokensFromSentence(Annotation sentence) {
		return FSIteratorFactory.createFSIterator(aJCas, tokenType, currentSentence, threadSafe, tokenConstraint);
	}

	
	protected String getTokenText(Annotation t) {
		return t.getCoveredText();
	}
	
	/**
	 * returns the most likely POS tag, or null if there is no POS tag
	 * if there is a POS with confidence NaN, the first such POS is returned (arbitrary)
	 */
	protected String getPOS(Annotation token) {
		return getPosOrLemma(token, posType, posConstraint);
	}
	
	/**
	 * returns the most likely lemma tag, or null if there is no lemma tag
	 * if there is a lemma with confidence NaN, the first such lemma is returned (arbitrary)
	 */
	protected String getLemma(Annotation token) {
		return getPosOrLemma(token, lemmaType, lemmaConstraint);
	}
	
	private String getPosOrLemma(Annotation source, int type, FSMatchConstraint constraint) {
		FSIterator<Annotation> iter = FSIteratorFactory.createFSIterator(aJCas, type, source, threadSafe, constraint);
		double maxProb = Double.NEGATIVE_INFINITY;
		String s = null;
		while (iter.hasNext()) {
			AnnotationTag e = (AnnotationTag) iter.next();
			if (Double.isNaN(e.getConfidence())) {
				return e.getValue();
			} else {
				if (e.getConfidence() > maxProb) {
					maxProb = e.getConfidence();
					s = e.getValue();
				}
			}
		}
		return s;
	}
	
	
	/**
	 * Caution: null array means "nothing to do" (no new item), array with empty string(s) means new item with empty content
	 * 
	 * @param s
	 * @return null (default behaviour, which means there is nothing to do)
	 */
	protected String[] startSentence(Annotation s) {
		return null;
	}
	
	/**
	 * Caution: null array means "nothing to do" (no new item), array with empty string(s) means new item with empty content
	 * 
	 * @param s
	 * @return null (default behaviour, which means there is nothing to do)
	 */
	protected String[] endSentence(Annotation s) {
		return null;
	}

}
