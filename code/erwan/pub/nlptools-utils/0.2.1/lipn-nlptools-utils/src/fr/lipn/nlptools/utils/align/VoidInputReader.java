package fr.lipn.nlptools.utils.align;

/**
 * 
 * This is a special {@link InputReader} which does not read any original input text: it only transmits
 * the data received from the {@link AnnotatedTextReader} to the {@link AlignerConsumer}.
 * It can be used for conversions between different formats or any process of this kind.
 * 
 * It is not able to send separators, and it is able to send positions only if the constructor with the AnnotatedTextReader as
 * argument is used. 
 * 
 * @author erwan
 *
 */
public class VoidInputReader implements InputReader {

	AnnotatedTextReader annotatedReader = null;

	public VoidInputReader() {
	}
	
	/**
	 * Constructor to used if the AlignerConsumer requires position.
	 * @param ar
	 */
	public VoidInputReader(AnnotatedTextReader ar) {
		this.annotatedReader = ar;
	}
	
	public void initReader(AlignerConsumer consumer) throws AlignmentException {
		if (consumer.requiresSeparator()) {
			throw new AlignmentException("Error: VoidInputReader is not able to send separators. This is probably a bug (someone did not read the javadoc ;-)");
		}
		if (consumer.requiresPosition() && (annotatedReader == null)) {
			throw new AlignmentException("Error: VoidInputReader is not able to send position, unless the annotated text reader is provided. This is probably a bug (someone did not read the javadoc ;-)");
		}
	}

	public void closeReader(AlignerConsumer consumer) throws AlignmentException {
	}

	public void receiveTokenAndAnnotation(AlignerConsumer consumer, String token, String annotation) throws AlignmentException {
		long start = -1; 
		long end = -1;
		if (consumer.requiresPosition()) {
			start = annotatedReader.getPosition();
			if (token != null) {
				end = start + token.length();
			} else {
				end = start;
			}
		}
		if ((annotation != null) || consumer.requiresUnannotatedTokens()) {
			consumer.receiveTokenAndAnnotation(token, token, annotation, null, start, end);
		}
	}

}
