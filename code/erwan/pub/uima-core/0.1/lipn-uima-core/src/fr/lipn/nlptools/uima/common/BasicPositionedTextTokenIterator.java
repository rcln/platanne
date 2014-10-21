package fr.lipn.nlptools.uima.common;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.uima.types.Token;
import fr.lipn.nlptools.utils.misc.PositionedText;

public class BasicPositionedTextTokenIterator extends GenericTokenIterator<PositionedText> {

	public static final int DEFAULT_SENTENCE_TYPE = Sentence.type;
	public static final int DEFAULT_TOKEN_TYPE = Token.type;


	/**
	 * Detailed constructor
	 * 
	 * @param aJCas
	 * @param range if not null, the resulting iterator is a subiterator for this Annotation 
	 * @param tokenType UIMA token type
	 * @param sentenceType UIMA sentence type
	 * @param iterateSentences use sentences boundaries: that means that the resulting iterator will only iterate over tokens that belong to some
	 *                         sentence (a subiterator is used), and that {@link #startSentence(Annotation)} and {@link #endSentence(Annotation)} will be called for each sentence.
	 * @param regexps if not null, an array of strings representing regular expressions to be applied to each token. This can be 
	 *                used to filter some characters. By default the matches are deleted (see below).
	 * @param replaceWith if not null, an array of strings that will be used to replace the matches corresponding to the regular expressions,
	 *                    instead of deleting them (e.g. whitespace chars or some special char).
	 */
	public BasicPositionedTextTokenIterator(JCas aJCas, Annotation range, boolean iterateSentences, int tokenType, int sentenceType, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint tokenConstraint, FSMatchConstraint sentenceConstraint) {
		super(aJCas, range, iterateSentences, tokenType, sentenceType, regexps, replaceWith, threadSafe, tokenConstraint, sentenceConstraint);
	}

	public BasicPositionedTextTokenIterator(JCas aJCas, Annotation range, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, range, iterateSentences, DEFAULT_TOKEN_TYPE, DEFAULT_SENTENCE_TYPE, regexps, replaceWith, threadSafe, null, null);
	}

	public BasicPositionedTextTokenIterator(JCas aJCas, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, null, false, DEFAULT_TOKEN_TYPE, DEFAULT_SENTENCE_TYPE, regexps, replaceWith, threadSafe, null, null);
	}
	

	public BasicPositionedTextTokenIterator(JCas aJCas, Annotation range, boolean iterateSentences, boolean threadSafe) {
		this(aJCas, range, iterateSentences, null, null, threadSafe);
	}

	
	public BasicPositionedTextTokenIterator(JCas aJCas, boolean iterateSentences, boolean threadSafe) {
		this(aJCas, null, iterateSentences, null, null, threadSafe);
	}

	public BasicPositionedTextTokenIterator(JCas aJCas, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean threadSafe) {
		this(aJCas, null, iterateSentences, regexps, replaceWith, threadSafe);
	}


	public BasicPositionedTextTokenIterator(JCas aJCas, boolean threadSafe) {
		this(aJCas, null, false, null, null, threadSafe);
	}

	protected FSIterator<Annotation> getTokensFromSentence(Annotation sentence) {
		return FSIteratorFactory.createFSIterator(aJCas, tokenType, currentSentence, threadSafe, tokenConstraint);
	}
	
	@Override
	protected PositionedText obtainDataFromToken(Annotation token, String tokenText) {
		return new PositionedText(tokenText, token.getBegin(), token.getEnd());
	}
	
	protected FSIterator<Annotation> getTokensFromSentence(JCas aJCas, int tokenType, Annotation sentence, boolean threadSafe, FSMatchConstraint tokenConstraint) {
		return FSIteratorFactory.createFSIterator(aJCas, tokenType, currentSentence, threadSafe, tokenConstraint);
	}


	@Override
	protected String getTokenText(Annotation t) {
		return t.getCoveredText();
	}

	@Override
	protected PositionedText endSentence(Annotation sentence) {
		return null;
	}

	@Override
	protected PositionedText startSentence(Annotation sentence) {
		return null;
	}

	
}
