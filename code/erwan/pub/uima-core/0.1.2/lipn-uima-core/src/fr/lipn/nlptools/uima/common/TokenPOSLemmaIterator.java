package fr.lipn.nlptools.uima.common;


import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;


import fr.lipn.nlptools.uima.types.Lemma;
import fr.lipn.nlptools.uima.types.PartOfSpeech;
import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.uima.types.Token;


/**
 * This generic abstract class implements the {@ link GenericTokenIterator} class for iterating over 3-sized String arrays, each containing the token and its corresponding POS and Lemma annotations.
 * It only implements {@link #obtainDataFromToken(Annotation, String)}.
 * 
 *  
 * @author moreau
 *
 */
public abstract class TokenPOSLemmaIterator extends GenericTokenIterator<String[]> {
	
	public static final int DEFAULT_SENTENCE_TYPE = Sentence.type;
	public static final int DEFAULT_TOKEN_TYPE = Token.type;
	public static final int DEFAULT_POS_TYPE = PartOfSpeech.type;
	public static final int DEFAULT_LEMMA_TYPE = Lemma.type;
	protected boolean withPOS;
	protected boolean withLemma;
	protected int dataSize;
	protected int posType;
	protected int lemmaType;
	protected FSMatchConstraint posConstraint;
	protected FSMatchConstraint lemmaConstraint;
	
	
	/**
	 * 
	 * See {@link GenericTokenIterator#GenericTokenIterator(JCas, Annotation, boolean, int, int, String[], String[], boolean, FSMatchConstraint, FSMatchConstraint)}
	 * for details about most parameters.
	 * 
	 * @param aJCas
	 * @param range
	 * @param withPOS whether POS annotations should be considered
	 * @param withLemma whether lemma annotations should be considered
	 * @param iterateSentences whether the main iterator should take sentences into account 
	 * @param sentenceType
	 * @param tokenType
	 * @param posType
	 * @param lemmaType
	 * @param regexps
	 * @param replaceWith
	 * @param threadSafe
	 * @param sentenceConstraint
	 * @param tokenConstraint
	 * @param posConstraint
	 * @param lemmaConstraint
	 */
	public TokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, int sentenceType, int tokenType, int posType, int lemmaType, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint, FSMatchConstraint posConstraint, FSMatchConstraint lemmaConstraint) {
		super(aJCas, range, iterateSentences, tokenType, sentenceType, regexps, replaceWith, threadSafe, tokenConstraint, sentenceConstraint);
		this.posType = posType;
		this.lemmaType = lemmaType;
		this.posConstraint = posConstraint;
		this.lemmaConstraint = lemmaConstraint;
		dataSize = 1;
		this.withPOS = withPOS;
		if (withPOS) {
			dataSize++;
		}
		this.withLemma = withLemma;
		if (withLemma) {
			dataSize++;
		}
	}

	public TokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, range, withPOS, withLemma, iterateSentences, DEFAULT_SENTENCE_TYPE, DEFAULT_TOKEN_TYPE, DEFAULT_POS_TYPE, DEFAULT_LEMMA_TYPE, regexps, replaceWith, threadSafe, null, null, null, null);
	}


	public TokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint, FSMatchConstraint posConstraint, FSMatchConstraint lemmaConstraint) {
		this(aJCas, range, withPOS, withLemma, iterateSentences, DEFAULT_SENTENCE_TYPE, DEFAULT_TOKEN_TYPE, DEFAULT_POS_TYPE, DEFAULT_LEMMA_TYPE, regexps, replaceWith, threadSafe, sentenceConstraint, tokenConstraint, posConstraint, lemmaConstraint);
	}

	
	public TokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint, FSMatchConstraint posConstraint, FSMatchConstraint lemmaConstraint) {
		this(aJCas, range, withPOS, withLemma, iterateSentences, DEFAULT_SENTENCE_TYPE, DEFAULT_TOKEN_TYPE, DEFAULT_POS_TYPE, DEFAULT_LEMMA_TYPE, null, null, threadSafe, sentenceConstraint, tokenConstraint, posConstraint, lemmaConstraint);
	}
	
	public TokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, boolean threadSafe) {
		this(aJCas, range, withPOS, withLemma, iterateSentences, DEFAULT_SENTENCE_TYPE, DEFAULT_TOKEN_TYPE, DEFAULT_POS_TYPE, DEFAULT_LEMMA_TYPE, null, null, threadSafe, null, null, null, null);
	}
	
	///////// inherited abstract methods implementation (not all methods)
	
		
	protected String[] obtainDataFromToken(Annotation token, String tokenText) {
		String[] data = new String[dataSize];
		data[0] = tokenText;
		int index = 1;
		synchronized (aJCas) {
			if (withPOS) {
				data[index] = getPOS(token);
				index++;
			}
			if (withLemma) {
				data[index] = getLemma(token);
				index++;
			}
		}
		return data;
	}
	

	/////// This class abstract methods
	
	/**
	 * @param token the input token
	 * @return the corresponding POS text
	 */
	protected abstract String getPOS(Annotation token); 
	
	/**
	 * @param token the input token
	 * @return the corresponding lemma text
	 */
	protected abstract String getLemma(Annotation token);

	
	
	public FSMatchConstraint getLemmaConstraint() {
		return lemmaConstraint;
	}

	public void setLemmaConstraint(FSMatchConstraint lemmaConstraint) {
		this.lemmaConstraint = lemmaConstraint;
	}

	public int getLemmaType() {
		return lemmaType;
	}

	public void setLemmaType(int lemmaType) {
		this.lemmaType = lemmaType;
	}

	public FSMatchConstraint getPosConstraint() {
		return posConstraint;
	}

	public void setPosConstraint(FSMatchConstraint posConstraint) {
		this.posConstraint = posConstraint;
	}

	public int getPosType() {
		return posType;
	}

	public void setPosType(int posType) {
		this.posType = posType;
	}

}