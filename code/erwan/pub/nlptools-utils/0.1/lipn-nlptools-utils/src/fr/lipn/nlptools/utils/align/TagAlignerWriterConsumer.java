package fr.lipn.nlptools.utils.align;

import java.io.Writer;

/**
 * Simple text writer for alignment process, reading and writing tags as annotation (like &lt;mytag&gt; .... &lt;/mytag&gt;). 
 * Writes separators in order to align exactly the same than the reference text (except that annotations are added).
 * @author moreau
 *
 */
public class TagAlignerWriterConsumer implements AlignerConsumer {


	Writer writer;
	
	static final char openingTagChar =  '<';
	static final char endTagChar = '/';

	public TagAlignerWriterConsumer(Writer writer) {
		this.writer = writer;
	}
	
	/**
	 */
	public void initConsumer() throws AlignmentException {
	}

	/**
	 */
	public void closeConsumer() throws AlignmentException {
		try {
		 writer.close();
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error closing writer", e);
		}
	}


	/**
	 * Annotation consists in a concatenation of tags, in which opening tags must appear before closing ones.
	 * Data is written in the following order: 1) opening tags 2) token 3) closing tags 4) separator.
	 * Any empty or null element will be ignored. 
	 * 
	 */
	/* (non-Javadoc)
	 * @see fr.lipn.nlptools.util.align.AlignerConsumer#receiveTokenAndAnnotation(java.lang.String, java.lang.String, java.lang.String, long, long)
	 */
	public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
				//System.err.println("DEBUG - Consumer receiving:"+inputToken+"##"+annotation+"##"+separator+"##");
		int tagLimit = -1;
		try {
			if ((annotation != null) && !annotation.isEmpty()) {
				// looks for end of opening tags/beginning of closing tags
				tagLimit = annotation.indexOf(openingTagChar, 0);
				while ((tagLimit != -1) && (annotation.charAt(tagLimit+1) != endTagChar)) {
					tagLimit = annotation.indexOf(openingTagChar, tagLimit+1);
				}
				if (tagLimit != -1) {
					writer.write(annotation.substring(0, tagLimit));
				} else {
					writer.write(annotation);
				}
			}
			if ((inputToken != null) && !inputToken.isEmpty()) {
				writer.write(inputToken);
			}
			if (tagLimit != -1) {
				writer.write(annotation.substring(tagLimit, annotation.length()));
			}
			if ((separator != null) && !separator.isEmpty()) {
				writer.write(separator);
			}
		} catch (java.io.IOException e) {
			throw new AlignmentException(e.getMessage(), e);
		}
	}

	public boolean requiresPosition() {
		return false;
	}

	public boolean requiresSeparator() {
		return true;
	}

	public boolean requiresUnannotatedTokens() {
		return true;
	}

}
