package fr.lipn.nlptools.utils.align;

/**
 * The <code>InputReader</code> is responsible for reading the reference (input) text. It receives data from the {@link AnnotatedTextReader}
 * and compares it to the reference text, then sends the aligned data to the {@link AlignerConsumer}.
 * 
 * 
 * @author moreau
 *
 */
public interface InputReader {

	/**
	 * Called before starting aligning process.
	 * Initialize any resource here.
	 * Notice that the {@link AlignerConsumer} has (normally) already been initialized before this method is called.
	 * 
	 * @param consumer
	 * @throws AlignmentException
	 */
	public abstract void initReader(AlignerConsumer consumer) throws AlignmentException;

	/**
	 * Called after finishing aligning process.
	 * Dispose any resource here.
	 * Notice that the {@link AlignerConsumer} has not (normally) been closed yet when this method is called.
	 * 
	 * @param consumer
	 * @throws AlignmentException
	 */
	public void closeReader(AlignerConsumer consumer) throws AlignmentException;
	
	
	/**
	 * Looks for the given token (sent by <code>AnnotatedTextReader</code> in the input text from current position and consumes this token.
	 * Should call {@link AlignerConsumer#receiveTokenAndAnnotation(String, String, String, String, long, long)} to transmit the aligned text (if necessary).
	 * 
	 * @param consumer
	 * @param token the token read in the annotated text (may be empty or null)
	 * @param annotation the annotation related to the token (may be empty or null)
	 * @throws AlignmentException
	 */
	public void receiveTokenAndAnnotation(AlignerConsumer consumer, String token, String annotation) throws AlignmentException;
	

}
