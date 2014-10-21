package fr.lipn.nlptools.utils.align;

import java.util.ArrayList;

/**
 *
 * A simple <code>AlignerConsumer</code> which requires only position of annotated text, reading
 *  tags as annotation (like &lt;mytag&gt; .... &lt;/mytag&gt;).<br/>
 * 
 * When the closing tag has been found and it has been checked that there is no unbalanced tags problems, the complete annotation 
 * (content+position) is sent using {@link AnnotationReceiver#addAnnotation(String, long, long)}. The innermost element is sent first.
 *
 * @author moreau
 *
 */
public class TagPositionConsumer implements AlignerConsumer {

	static final char openingTagChar =  '<';
	static final char closingTagChar =  '>';
	static final char endTagChar = '/';

	
	ArrayList<String> tagsQueue;
	ArrayList<Long> posQueue;
	AnnotationReceiver receiver;
	boolean requiresUnannotatedTokens;
	
	/**
	 * Interface to communicate with the actual final consumer, which knows what to do with the annotations.
	 * Remark: <code>addUnannotatedToken</code> is never called if requiresUnannotatedTokens is false.
	 * @author moreau
	 *
	 */
	public interface AnnotationReceiver {
		public void initPositionConsumer() throws AlignmentException;
		public void addAnnotation(String content, long start, long end) throws AlignmentException;
		public void addUnannotatedToken(String inputToken, String annotatedToken, long start, long end) throws AlignmentException;
		public void closePositionConsumer() throws AlignmentException;
	}
	
	public TagPositionConsumer(AnnotationReceiver receiver, boolean requiresUnannotatedTokens) {
		this.receiver = receiver;
		this.requiresUnannotatedTokens = requiresUnannotatedTokens;
	}

	public TagPositionConsumer(AnnotationReceiver receiver) {
		this(receiver, false);
	}

	public void initConsumer() throws AlignmentException {
		tagsQueue = new ArrayList<String>();
		posQueue = new ArrayList<Long>();
		receiver.initPositionConsumer();
	}

	public void closeConsumer() throws AlignmentException {
		receiver.closePositionConsumer();
		if (tagsQueue.size() > 0) {
			throw new AlignmentException("Unbalanced tag: opened tag(s) remaining in the queue when closing consumer.");
		}
	}

	/**
	 * Annotation consists in a concatenation of tags, in which opening tags must appear before closing ones.
	 * This method is able to deal with an annotation containing any number of tags.
	 * When the closing tag has been found and it has been checked that there is no unbalanced tags problems, the complete annotation 
	 * (content+position) is sent using {@link AnnotationReceiver#addAnnotation(String, long, long)}. The innermost element is sent first.
	 * Any empty or null element will be ignored. 
	 * 
	 */
	public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
		//System.out.println("DEBUG - Consumer receiving:"+inputToken+"##"+annotation+"##"+separator+"##"+startPos+"-"+endPos);
		int tagLimit = -1;
		if ((annotation != null) && !annotation.isEmpty()) {
			// looks for end of opening tags/beginning of closing tags
			tagLimit = annotation.indexOf(openingTagChar, 0);
			if (tagLimit == -1) {
				throw new AlignmentException("Invalid annotation string has been received (no opening tag): '"+annotation+"'");
			}
			while ((tagLimit != -1) && (annotation.charAt(tagLimit+1) != endTagChar)) {
				// opening tag
				int endTag=annotation.indexOf(closingTagChar, tagLimit+1);
				tagsQueue.add(annotation.substring(tagLimit+1, endTag));
				posQueue.add(startPos);
				tagLimit = annotation.indexOf(openingTagChar, endTag+1);
			}
			while (tagLimit != -1) {
				if (annotation.charAt(tagLimit+1) != endTagChar) {
					throw new AlignmentException("Invalid annotation string has been received (opening tag after closing tag): '"+annotation+"'");
				}
				// closing tag
				int endTag = annotation.indexOf(closingTagChar, tagLimit+1);
				int indexLast = tagsQueue.size()-1;
				String lastOpenTag = tagsQueue.get(indexLast);
				if ((tagsQueue.size() > 0) && (lastOpenTag.equals(annotation.substring(tagLimit+2, endTag)))) {
					receiver.addAnnotation(lastOpenTag, posQueue.get(indexLast), endPos);
					tagsQueue.remove(indexLast);
					posQueue.remove(indexLast);
				} else {
					throw new AlignmentException("Unbalanced tags while reading '"+annotation+"'.");
				}
				tagLimit = annotation.indexOf(openingTagChar, endTag+1);
			}
		} else {
			if (requiresUnannotatedTokens) {
				receiver.addUnannotatedToken(inputToken, annotatedToken, startPos, endPos);
			}
		}
	}

	public boolean requiresPosition() {
		return true;
	}

	public boolean requiresSeparator() {
		return false;
	}

	public boolean requiresUnannotatedTokens() {
		return requiresUnannotatedTokens;
	}

}
