package fr.lipn.nlptools.utils.align;


/**
 * 
 * Main class in the package. Reads the annotated text and sends data to be processed to the other classes.<br/>
 * 
 * This package is meant to align two forms of a given text, one being the "original" (called the reference text) and the other one being a
 * transformed version, generally by adding annotations (called the annotated text). The package is intended to provide flexibility about the
 * kind of data processed and the way it is processed. For example it is possible to re-use the same componant for reading annotated text when
 * one wants to produce different forms of output.<br/>
 * 
 * General principle: <code>AnnotatedTextReader</code> is an abstract class which has one implemented method, namely  
 * {@link #align(InputReader, AlignerConsumer)}. This method calls the three classes initialization methods, then
 * processes the annotated text by calling {@link #processNextToken(InputReader, AlignerConsumer)} for each token found,
 * and finally calls the three classes closing methods. 
 * <ul>
 * <li> <code>processNextToken</code> has to send data read from the annotated text to the {@link InputReader};
 * <li> The <code>InputReader</code>'s method  {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)}
 * has to compare the data to the reference data, possibly doing some aligning stuff, and then send that new data to the {@link AlignerConsumer};
 * <li> Finally the <code>AlignerConsumer</code>'s method {@link AlignerConsumer#receiveTokenAndAnnotation(String, String, String, String, long, long)}
 * has to take this information and process it, for example by writing it to a file.
 * </ul>
 * 
 * @author moreau
 *
 */
public abstract class AnnotatedTextReader {


	/**
	 * Main alignment process.
	 *  
	 * Parses the annotated text and the input text at the same time, token by token. 
	 * For each token found in the annotated text, the method {@link #processNextToken(InputReader, AlignerConsumer)} is called and 
	 * is in charge to call {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)}, which is itself 
	 * in charge to call {@link AlignerConsumer#receiveTokenAndAnnotation(String, String, String, String, long, long)}.<br/>
	 * 
	 * It may be noticed that the <code>AlignerConsumer</code> is initialized before the <code>InputConsumer</code>, which is itself
	 * initialized before the <code>AnnotatedTextReader</code>: this way the <code>InputReader</code> is allowed to start sending data
	 * to the <code>AlignerConsumer</code> in its init method, and same holds between the <code>AnnotatedTextReader</code> and the 
	 * <code>InputReader</code>. The closing methods are called in in the reverse order, for the same reason.<br/>
	 *   
	 * There should no reason to override this method in normal use. 
	 * 
	 * @param inputReader The reader for the input text, used as a reference. 
	 * @param alignerConsumer The alignment consumer, to which results are sent.
	 */
	public void align(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException {
		
//		try {
			alignerConsumer.initConsumer();  // init depending componant after the one it depends on.
			inputReader.initReader(alignerConsumer);
			initReader(inputReader, alignerConsumer);
			while (hasToken()) {
				processNextToken(inputReader, alignerConsumer);
			}
			// problem with this 'finally' is that if an exception has been thrown, in most cases
			// the close methods will fail and thus throw another exception that will hide the first one, which is the original problem.
			// For now I don't know how to deal with that, but it seems better to throw the first exception, so: no 'finally'.
//		} finally {
			closeReader(inputReader, alignerConsumer); // close depending componant before the one it depends on.
			if (inputReader != null) {
				inputReader.closeReader(alignerConsumer);
			}
			if (alignerConsumer != null) {
				alignerConsumer.closeConsumer();
			}
//		}
	}
	
	
	/**
	 * Called before starting aligning process.
	 * Initialize any resource here.
	 * Notice that the {@link InputReader} has (normally) already been initialized before this method is called.
	 * 
	 */
	public abstract void initReader(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException;

	/**
	 * Called after finishing aligning process.
	 * Dispose any resource here.
	 * Notice that the {@link InputReader} has not (normally) been closed yet when this method is called.
	 */
	public abstract void closeReader(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException;

	/**
	 * @return true while the annotated text is not totally read, false otherwise.
	 */
	public abstract boolean hasToken();

	/**
	 * Parses the next token and possibly transmits data (token and/or annotation) to the {@link InputReader}.
	 * Should call {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)} to transmit data (if necessary).
	 * It is up to the actual reader to define how metadata is returned (e.g. before or after the token, or 
	 * even without any token).
	 *  
	 * @throws AlignmentException
	 */
	public abstract void processNextToken(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException;

	
	/**
	 * An AnnotatedTextReader MAY have a way to provide its current position in the
	 * annotated text. 
	 * This is not mandatory, therefore the following convention holds: in case the
	 * AnnotatedTextReader can not return its position, this method must always
	 * return -1 (default behaviour). Otherwise, the subclass can override this
	 * method to return the current position, and it must (by convention) return
	 * a non-negative result, as soon as it has been initialized with 
	 * {@link #initReader(InputReader, AlignerConsumer)}.
	 * 
	 * @return the current position in the annotated text if the instance is able to
	 * provide it, -1 otherwise.
	 */
	public long getPosition() {
		return -1;
	}

	/**
	 * Finds the next non-whitespace character in a string.
	 * This a convenience method (simply because it's often used).
	 * 
	 * @param s input string
	 * @param start start index
	 * @return first index after start corresponding to a non-whitespace character, or length of string if no such index found.
	 */
	public static int nextPosNonWhitespace(String s, int start) {
		int l = s.length();
		int pos = start;
		while ((pos < l) && Character.isWhitespace(s.charAt(pos))) {
			pos++;
		}
		return pos;
	}
	

}
