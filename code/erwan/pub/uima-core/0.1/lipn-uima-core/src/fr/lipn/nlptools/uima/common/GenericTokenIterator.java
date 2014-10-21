package fr.lipn.nlptools.uima.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;


/**
 * TODO doc
 * - utilisable avec n'importe quel types comme token et sentence (pas nécessaire types lipn)
 * 
 * Warning: this implementation may not be suited to the case where there are Interpretation tags used at the Sentence or Token level.
 * 
 * @author moreau
 *
 * @param <T>
 */
public abstract class GenericTokenIterator<T> implements Iterator<T> {

	protected JCas aJCas;
	protected Iterator<Annotation> tokenIterator;
	protected Iterator<Annotation> sentenceIterator;
	protected Annotation currentSentence;
	protected int tokenType;
	protected int sentenceType;
	protected Pattern[] patterns = null;
	protected String[] replaceWith = null;
	FSMatchConstraint tokenConstraint;
	FSMatchConstraint sentenceConstraint;
	private T nextData = null;
	protected int index;
	boolean threadSafe;

	/**
	 * Constructor
	 * 
	 * @param aJCas the CAS
	 * @param range if not null, a subiterator of this Annotation is returned
	 * @param iterateSentences use/do not use sentences boundaries: that means that the resulting iterator will only iterate over tokens that belong to some
	 *                         sentence (actually a subiterator will be used for each sentence), and that {@link #startSentence(Annotation)} and {@link #endSentence(Annotation)} will be called for each sentence.
	 * @param tokenType the UIMA type for tokens annotations
	 * @param sentenceType the UIMA type for sentences annotations
	 * @param regexps if not null, an array of strings representing regular expressions to be applied to each token. This can be 
	 *                used to filter some characters. By default the matches are deleted (see below).
	 * @param replaceWith if not null, an array of strings that will be used to replace the matches corresponding to the regular expressions,
	 *                    instead of deleting them (e.g. whitespace chars or some special char). If not null this array must have the same length as <code>regexps</code>.
	 * @param threadSafe whether this iterator will be thread-safe (based on {@link ThreadSafeFSIterator}) or not: if the iterator may be accessed in
	 *        a thread (even a single one), it is highly recommended to make it thread-safe. Not making it thread-safe may save time, as usual.
	 * @param tokenConstraint a constraint that will be applied to this iterator for tokens. Used to filter annotations (see also {@link FSIteratorFactory})
	 * @param sentenceConstraint a constraint that will be applied to this iterator for sentences. Used to filter annotations (see also {@link FSIteratorFactory})
	 */
	public GenericTokenIterator(JCas aJCas, Annotation range, boolean iterateSentences, int tokenType, int sentenceType, String[] regexps, String[] replaceWith, boolean threadSafe, FSMatchConstraint tokenConstraint, FSMatchConstraint sentenceConstraint) {
		super();
		this.aJCas = aJCas;
		this.tokenType = tokenType;
		this.sentenceType = sentenceType;
		this.threadSafe = threadSafe;
		this.tokenConstraint = tokenConstraint;
		this.sentenceConstraint = sentenceConstraint;
		if (regexps != null) {
			patterns = new Pattern[regexps.length];
			for (int i=0; i< regexps.length; i++)  {
				patterns[i] = Pattern.compile(regexps[i]);
			}
			this.replaceWith = replaceWith;
		}
		if (iterateSentences) {
			sentenceIterator = FSIteratorFactory.createFSIterator(aJCas, sentenceType, range, threadSafe, sentenceConstraint);
			if (sentenceIterator.hasNext()) {
				currentSentence = sentenceIterator.next();
				tokenIterator = null;
			} else {
				sentenceIterator = null;
			}
		} else {
			tokenIterator = FSIteratorFactory.createFSIterator(aJCas, tokenType, range, threadSafe, tokenConstraint);
		}
		index = 0;
	}
	

	public boolean hasNext() {
		try {
			getNextData(false);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public T next() {
		index++;
		return getNextData(true);
	}
	
	/**
	 * 
	 * @return the next element, but without moving forward: this means that the next call to this method or to {@link #next()} will return the same
	 *         element.
	 */
	public T nextWithoutMoving() {
		return getNextData(false);
	}
	
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @return the current index, which is equal to the number of successful calls to {@link #next()}. Each element returned has a unique index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Warning: may be called ONLY after all tokens have been read. Convenience method, equivalent to {@link #getIndex()} called at the end.
	 * @return the number of tokens read if all have of them have been read, or -1 otherwise;
	 */
	public int getNumber() {
		if (hasNext()) {
			return -1;
		} else {
			return getIndex();
		}
	}
	
	/**
	 * This method hides the dirty details about starting/Ending a sentence: this optional behaviour requires some quite complex
	 * strategy because it is not possible to know whether the corresponding methods will return some data, thus it's sometimes 
	 * necessary to call them (but only once!) and depending on the result going further or not.
	 * 
	 * @param goToNext true if the current data is to be consumed, or false if the next call to this method should return the same current data.
	 * @return the data, or null if the end of the iteration has been reached.
	 */
	private T getNextData(boolean goToNext) throws NoSuchElementException {
		// notice that nextData may not be null before the call to this method
		while (nextData == null) {  // the loop stops either when the condition is fulfilled or an exception is thrown
			if ((tokenIterator != null) && (tokenIterator.hasNext())) {
				Annotation nextToken = tokenIterator.next();
				String s;
				if (threadSafe) {
					synchronized (aJCas) {
						s = getTokenText(nextToken);
					}
				} else {
					s = getTokenText(nextToken);

				}
				//System.out.println("DEBUG - TOKEN="+s);
				if (patterns != null) {
					for (int i=0; i<patterns.length; i++) {
						s = patterns[i].matcher(s).replaceAll((replaceWith!=null)?replaceWith[i]:"");
					}
				}
				if (threadSafe) {
					synchronized (aJCas) {
						nextData = obtainDataFromToken(nextToken, s);
					}
				} else {
					nextData = obtainDataFromToken(nextToken, s);
				}
			} else if (sentenceIterator != null) {     // sentenceIterator != null -> starting or ending a sentence, complex case
				if (tokenIterator == null) { // arriving here means that tokenIterator has been previously set to null: starting a new sentence
					tokenIterator = getTokensFromSentence(currentSentence);
					nextData = startSentence(currentSentence); // MAY BE NULL -> in this case 'while' continues to find the next item 
				} else { //  arriving here means tokenIterator is not null but has no next item: ending a sentence
					nextData = endSentence(currentSentence); // MAY BE NULL -> in this case 'while' continues to find the next item
					if (sentenceIterator.hasNext()) {
						currentSentence = sentenceIterator.next();
					} else {
						sentenceIterator = null;  // to inform next step that all sentences have been processed
					}
					tokenIterator = null; // to inform next  step that end of sentence has been processed
				}
			}  else {  // all iterators are done 
				throw new NoSuchElementException();
			}
		}
		// at this point, nextData is not null (possibly an exception has been thrown)
		T data = nextData;
		if (goToNext) { // to force the next call to this method to look for the following item (otherwise the same item is returned) 
			nextData = null;
		}
		return data;
	}
	
	
	/**
	 * Returns an FSIterator over tokens. Usually it simply consists in returning:
	 * FSIteratorFactory.createFSIterator(aJCas, tokenType, currentSentence, threadSafe, tokenConstraint)
	 * But there may be special cases when there can be Interpretation annotations at the sentences level
	 * 
	 * @param aJCas
	 * @param tokenType
	 * @param sentence
	 * @param threadSafe
	 * @param tokenConstraint
	 * @return
	 */
	protected abstract FSIterator<Annotation> getTokensFromSentence(Annotation sentence);

	/**
	 * Returns the text corresponding to the token, usually the "covered text".
	 * @param aJCas
	 * @param token
	 * @return
	 */
	protected abstract String getTokenText(Annotation token);

	/**
	 * Returns the "data" corresponding to the token. <code>tokenText</code> corresponds to the string returned by {@link #getTokenText(JCas, Annotation)},
	 * but possibly filtered if a filter has been set.
	 * @param aJCas
	 * @param token
	 * @param tokenText
	 * @return
	 */
	protected abstract T obtainDataFromToken(Annotation token, String tokenText);

	
	/**
	 * Caution: T==null means "nothing to do" (no new item)
	 * @param s
	 * @return
	 */
	protected abstract T startSentence(Annotation sentence);

	/**
	 * Caution: T==null means "nothing to do" (no new item)
	 * @param s
	 * @return
	 */
	protected abstract T endSentence(Annotation sentence);


	public Pattern[] getPatterns() {
		return patterns;
	}


	public void setPatterns(Pattern[] patterns) {
		this.patterns = patterns;
	}


	public String[] getReplaceWith() {
		return replaceWith;
	}


	public void setReplaceWith(String[] replaceWith) {
		this.replaceWith = replaceWith;
	}


	public FSMatchConstraint getSentenceConstraint() {
		return sentenceConstraint;
	}


	public void setSentenceConstraint(FSMatchConstraint sentenceConstraint) {
		this.sentenceConstraint = sentenceConstraint;
	}


	public int getSentenceType() {
		return sentenceType;
	}


	public void setSentenceType(int sentenceType) {
		this.sentenceType = sentenceType;
	}


	public FSMatchConstraint getTokenConstraint() {
		return tokenConstraint;
	}


	public void setTokenConstraint(FSMatchConstraint tokenConstraint) {
		this.tokenConstraint = tokenConstraint;
	}


	public int getTokenType() {
		return tokenType;
	}


	public void setTokenType(int tokenType) {
		this.tokenType = tokenType;
	}

}
