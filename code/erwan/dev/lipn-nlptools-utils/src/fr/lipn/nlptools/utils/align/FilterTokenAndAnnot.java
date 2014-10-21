package fr.lipn.nlptools.utils.align;

/**
 * An object can implement this interface to filter special cases in {@link SimpleTokenByLineReader} and {@link SimpleTaggedTextReader}. 
 *  Its methods are called in {@link AnnotatedTextReader#processNextToken(InputReader, AlignerConsumer)} before calling
 *                {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)}, thus an object
 *                implementing this interface is able to remove/transform the token and annotation that were read.
 *                
 * @author moreau
 *
 */
public interface FilterTokenAndAnnot {

	/**
	 * 
	 * @param token token read
	 * @param annot annotation read
	 * @return token to transmit to the InputReader
	 */
	public String filteredToken(String token, String annot);
	
	/**
	 * 
	 * @param token token read
	 * @param annot annotation read
	 * @return annotation to transmit to the InputReader
	 */
	public String filteredAnnot(String token, String annot);
	
	/**
	 * 
	 * @return true to transmit the data to the InputReader, false to ignore it.
	 */
	public boolean sendTokenAndAnnot();
}
