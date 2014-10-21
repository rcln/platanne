package fr.lipn.nlptools.uima.lia.common;

import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.common.BasicPositionedTextTokenIterator;

/**
 * A {@link BasicPositionedTextTokenIterator} specialized for LIA (dealing with <code>Cleaned</code> annotations, see
 * {@link LiaCleanedTokenProvider}. 
 * @author moreau
 *
 */
public class LiaPositionedTextTokenIterator extends BasicPositionedTextTokenIterator {
	
	protected LiaCleanedTokenProvider liaCleanedTokenProvider;

	
	//  to study: iterateSentences not needed ?
	public LiaPositionedTextTokenIterator(JCas aJCas, Annotation range, boolean cleanedVersion, boolean threadSafe, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint) {
		super(aJCas, range, false, LipnLiaExternalProgramGenericAnnotator.LIA_INVALID_CHARS_REGEXPS, null, threadSafe, sentenceConstraint, tokenConstraint);
		liaCleanedTokenProvider = new LiaCleanedTokenProvider(cleanedVersion, threadSafe);
	}

	public LiaPositionedTextTokenIterator(JCas aJCas, boolean cleanedVersion, boolean threadSafe) {
		this(aJCas, null, cleanedVersion, threadSafe, null, null);
	}

	public LiaPositionedTextTokenIterator(JCas aJCas, boolean threadSafe) {
		this(aJCas, null, true, threadSafe, null, null);
	}

	
	/**
	 * thread-safe
	 */
	public LiaPositionedTextTokenIterator(JCas aJCas, FSMatchConstraint sentenceConstraint, FSMatchConstraint tokenConstraint) {
		this(aJCas, null, true, true, sentenceConstraint, tokenConstraint);
	}

	
	/**
	 * thread-safe
	 */
	public LiaPositionedTextTokenIterator(JCas aJCas) {
		this(aJCas, null, true, true, null, null);
	}
	
	@Override
	protected String getTokenText(Annotation token) {
		return liaCleanedTokenProvider.getTokenText(aJCas, token);
	}



}
