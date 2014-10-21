package fr.lipn.nlptools.uima.lia.common;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.common.BasicPositionedTextTokenIterator;

/**
 * TODO doc
 * @author moreau
 *
 */
public class LiaPositionedTextTokenIterator extends BasicPositionedTextTokenIterator {
	
	protected LiaCleanedTokenProvider liaCleanedTokenProvider;

	
	// TODO revoir si besoin iterateSentences ???
	public LiaPositionedTextTokenIterator(JCas aJCas, Annotation range, boolean cleanedVersion, boolean threadSafe) {
		super(aJCas, range, false, LipnLiaExternalProgramGenericAnnotator.LIA_INVALID_CHARS_REGEXPS, null, threadSafe);
		liaCleanedTokenProvider = new LiaCleanedTokenProvider(cleanedVersion, threadSafe);
	}

	public LiaPositionedTextTokenIterator(JCas aJCas, boolean cleanedVersion, boolean threadSafe) {
		this(aJCas, null, cleanedVersion, threadSafe);
	}

	public LiaPositionedTextTokenIterator(JCas aJCas, boolean threadSafe) {
		this(aJCas, null, true, threadSafe);
	}

	/**
	 * thread-safe
	 * @param aJCas
	 * @param threadSafe
	 */
	public LiaPositionedTextTokenIterator(JCas aJCas) {
		this(aJCas, null, true, true);
	}
	
	@Override
	protected String getTokenText(Annotation token) {
		return liaCleanedTokenProvider.getTokenText(aJCas, token);
	}



}
