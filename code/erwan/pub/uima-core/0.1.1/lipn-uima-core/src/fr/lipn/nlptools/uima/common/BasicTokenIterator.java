package fr.lipn.nlptools.uima.common;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.uima.types.Token;

/**
 * This class does not add anything to a simple FSIterator over Token types.
 * However it is more generic to use it because this way any future implementation of
 * a component using it can be provided with a subclass which implements a more complex case
 * instead (e.g. ignoring some tokens, etc.)
 * 
 * TODO doc
 * @author moreau
 *
 */
public class BasicTokenIterator extends GenericTokenIterator<Token> {

	public BasicTokenIterator(JCas aJCas, Annotation range, boolean iterateSentences, boolean threadSafe) {
		super(aJCas, range, iterateSentences, Token.type, Sentence.type, null, null, threadSafe, null, null);
	}

	public BasicTokenIterator(JCas aJCas, boolean threadSafe) {
		this(aJCas, null, false, threadSafe);
	}

	public BasicTokenIterator(JCas aJCas) {
		this(aJCas, null, false, false);
	}
	
	
	protected FSIterator<Annotation> getTokensFromSentence(Annotation sentence) {
		return FSIteratorFactory.createFSIterator(aJCas, tokenType, currentSentence, threadSafe, tokenConstraint);
	}

	@Override
	protected String getTokenText(Annotation token) {
		return token.getCoveredText();
	}

	@Override
	protected Token obtainDataFromToken(Annotation token, String tokenText) {
		return (Token) token;
	}

	
	@Override
	protected Token endSentence(Annotation sentence) {
		return null;
	}


	@Override
	protected Token startSentence(Annotation sentence) {
		return null;
	}
	
}
