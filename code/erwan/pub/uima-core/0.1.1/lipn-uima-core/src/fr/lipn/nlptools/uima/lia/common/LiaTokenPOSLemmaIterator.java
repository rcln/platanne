package fr.lipn.nlptools.uima.lia.common;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import fr.lipn.nlptools.uima.common.POSLemmaPairInterpretationTokenPOSLemmaIterator;

public class LiaTokenPOSLemmaIterator extends POSLemmaPairInterpretationTokenPOSLemmaIterator {

	protected LiaCleanedTokenProvider liaCleanedTokenProvider;

	public LiaTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean cleanedVersion, boolean threadSafe, String sentenceComponentIdFilter, String tokenComponentIdFilter, String posComponentIdFilter, String lemmaComponentIdFilter) {
		super(aJCas, range, withPOS, withLemma, iterateSentences, regexps, replaceWith, threadSafe, sentenceComponentIdFilter, tokenComponentIdFilter, posComponentIdFilter, lemmaComponentIdFilter);
		liaCleanedTokenProvider = new LiaCleanedTokenProvider(cleanedVersion, threadSafe);
	}

	public LiaTokenPOSLemmaIterator(JCas aJCas, Annotation range, boolean withPOS, boolean withLemma, boolean iterateSentences, String[] regexps, String[] replaceWith, boolean cleanedVersion, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, iterateSentences, regexps, replaceWith, true, threadSafe, null, null, null, null);
	}

	public LiaTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, boolean iterateSentences, boolean threadSafe) {
		this(aJCas, null, withPOS, withLemma, iterateSentences, LipnLiaExternalProgramGenericAnnotator.LIA_INVALID_CHARS_REGEXPS, null, true, threadSafe);
	}
	
	/**
	 * thread-safe
	 * @param aJCas
	 * @param withPOS
	 * @param withLemma
	 * @param iterateSentences
	 */
	public LiaTokenPOSLemmaIterator(JCas aJCas, boolean withPOS, boolean withLemma, boolean iterateSentences) {
		this(aJCas, null, withPOS, withLemma, iterateSentences, LipnLiaExternalProgramGenericAnnotator.LIA_INVALID_CHARS_REGEXPS, null, true, true);
	}


	@Override
	protected String getTokenText(Annotation token) {
		return liaCleanedTokenProvider.getTokenText(aJCas, token);
	}


	@Override
	protected String[] endSentence(Annotation sentence) {
		return sentenceBoundaryData(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_CLOSE_TAG);
	}


	@Override
	protected String[] startSentence(Annotation sentence) {
		return sentenceBoundaryData(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_SENTENCE_OPEN_TAG);
	}
	
	private String[] sentenceBoundaryData(String tag) {
		String[] data = new String[dataSize];
		data[0] = tag;
		int index = 1;
		if (withPOS) {
			data[index] = LipnLiaExternalProgramGenericAnnotator.LIA_SENTENCE_SEPARATOR_POS_TAG;
			index++;
		}
		if (withLemma) {			
			data[index] = tag;
		}
		return data;
	}

}
