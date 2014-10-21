package fr.lipn.nlptools.utils.align;

import org.apache.uima.util.Logger;

/**
 * An "aligner consumer" is a class that takes as input the output from the {@link InputReader} (which depends on the {@link AnnotatedTextReader}).
 * His role consists in "doing something" with this output: it may write it to a file or store it in memory, for instance.
 * 
 * The general principle is explained in {@link AnnotatedTextReader}.
 * 
 * @author moreau
 *
 */
public interface AlignerConsumer {
	
	/**
	 * Called before starting aligning process.
	 * Initialize any resource here.
	 * This method is called before initializing the {@link  AnnotatedTextReader} and the {@link InputReader}.
	 */
	public void initConsumer()  throws AlignmentException;
	
	/**
	 * Called after finishing aligning process.
	 * Dispose any resource here.
	 * This method is called after closing the {@link  AnnotatedTextReader} and the {@link InputReader}.
	 */
	public void closeConsumer()  throws AlignmentException;
	
	/**
	 * @return true if this consumer needs tokens with no annotation (i.e. all tokens).
	 */
	public boolean requiresUnannotatedTokens();
	
	/**
	 * @return true if this consumer needs the position of tokens.
	 */
	public boolean requiresPosition();
	
	/**
	 * @return true if this consumer needs the separator occuring before a token.
	 */
	public boolean requiresSeparator();

	/**
	 * Receives data to process from the {@link InputReader} and consumes it (e.g. writes to the output file).
	 * Some parameters may be unitialized/unused depending on the fact that this consumer requires it.
	 *  
	 * @param annotatedToken The token to process (may be null or empty), as read in the annotated text
	 * @param inputToken The token to process (may be null or empty), as read in the reference input text
	 * @param annotation The annotation associated to this token (may be null or empty). It is up to the annotated text reader which metadata
	 *                   is taken into account: for instance it may be the left or right context of the token.
	 * @param separator A separator string if needed (may be null or empty). This AlignerConsumer is responsible for the way to use it. 
	 * @param startPos Starting position of the token in the input text (if needed).
	 * @param endPos Ending position of the token in the input text (if needed).
	 */
	public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos)  throws AlignmentException;


}
