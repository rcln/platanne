package fr.lipn.nlptools.utils.align;

import java.io.Reader;


/**
 * This {@link AnnotatedTextReader} reads input from a <code>Reader</code> object where annotations are provided
 *  as usual tags: &lt;mytagname myattr=myval ... &gt;
 * 
 * {@link #processNextToken(InputReader, AlignerConsumer)} will send only one element at a time, an element being either a 
 * text sequence without whitespaces (token) or an annotation, consisting in a single tag (opening or closing). 
 * When sending a tag the token parameter is set to null.<br/>
 * 
 * This AnnotatedTextReader can be used with any kind of <code>java.io.Reader</code> (file, string, etc.)<br/>
 * Examples:
 * <ul>
 * <li>A simple text file: <code>new SimpleTaggedTextReader(new java.io.FileReader(filename))</code></li>
 * <li>A text file with special encoding: <code>new SimpleTaggedTextReader(new java.io.InputStreamReader(new java.io.FileInputStream(filename), encoding))</code></li>
 * <li>A java.lang.String object: <code>new SimpleTaggedTextReader(new java.io.StringReader(myString))</code></li>
 * </ul>
 *  
 * @author moreau
 *
 */

public class SimpleTaggedTextReader extends AnnotatedTextReader {

	Reader reader;
	CharByCharTextLocator locator;
	
	/**
	 * True by default.
	 */
	boolean acceptClosingTagCharAlone = true;
	static final char openingTagChar = '<'; 
	static final char closingTagChar = '>';
	int currCar =-1;
	char[] otherWhiteSpaceChars = null;
	FilterTokenAndAnnot filter = null;
	long lastTokenStart = -1;



	/**
	 * Detailed constructor.
	 * 
	 * @param reader the Reader object from which the input text is read
	 * @param otherWhiteSpaceChars a set of characters that will be considered as whitespaces (if null only standard whitespaces are used, as
	 *                             defined by <code>Character.isWhitespace(char)</code>)
	 * @param filter An object implementing {@link FilterTokenAndAnnot}, which is used to pre-process some special
	 *               cases. It is called in {@link #processNextToken(InputReader, AlignerConsumer)} before calling
	 *                {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)}, thus this object
	 *                is able to remove/transform the token and annotation that were read. null = do not use.
	 */
	public SimpleTaggedTextReader(Reader reader, final char[] otherWhiteSpaceChars, FilterTokenAndAnnot filter) {
		this.otherWhiteSpaceChars = otherWhiteSpaceChars;
		this.reader = reader;
		this.filter = filter;
	}

	public SimpleTaggedTextReader(Reader reader, final char[] otherWhiteSpaceChars) {
		this(reader, otherWhiteSpaceChars, null);
	}

	public SimpleTaggedTextReader(Reader reader, FilterTokenAndAnnot filter) {
		this(reader, null, filter);
	}
	
	public SimpleTaggedTextReader(Reader reader) {
		this(reader, null, null);
	}

	/**
	 * Only moves to next token.
	 */
	@Override
	public void initReader(InputReader inputReader,	AlignerConsumer alignerConsumer) throws AlignmentException {
		try {
			/* BAD IDEA: the reader may not be ready even if it will be able to be read (sooner or later)
			if (!reader.ready()) {
				throw new AlignmentException("Input reader is not ready.");
			}
			*/
			locator = new CharByCharTextLocator();
			currCar = reader.read();
			locator.readChar(currCar);
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error: input reader fails to read first character (maybe empty input).", e);
		}
		moveToNextToken();
	}


	/**
	 * closes the reader.
	 */
	@Override
	public void closeReader(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException {
		try {
			reader.close();
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while closing tagged text reader.", e);
		}
	}
	
	
	/**
	 * @return the position of the last token which was sent, if any.
	 */
	@Override
	public long getPosition() {
		return lastTokenStart;
	}


	/**
	 * Reads a token or annotation, one at a time. Then sends it to the <code>InputReader</code>
	 */
	@Override
	public void processNextToken(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException {
		String token = null;
		String annot = null;
		StringBuilder data = new StringBuilder();
		boolean isAnnot = (currCar == openingTagChar);
		boolean stop = false;
		try {
			lastTokenStart = locator.getCharPosition();
			while ((currCar != -1) && !stop) {
				data.append((char) currCar);
				currCar = reader.read();
				locator.readChar(currCar);
				stop = (currCar == closingTagChar) || (currCar == openingTagChar) || (!isAnnot && considerAsWhitespace(currCar)); 
			}
			if (isAnnot) { 	//		 tag found : return empty token and set annotation.
				if  ((currCar == -1) || (currCar == openingTagChar)) {
					throw new AlignmentException("Unbalanced tag at "+locator.getLocationAsLongString()+" (tagged text input reader).");
				} else {
					data.append((char) currCar);
					currCar = reader.read();
					locator.readChar(currCar);
					annot = data.toString();
				}					
			} else {  //normal text
				if (currCar == closingTagChar) {
					if (!acceptClosingTagCharAlone) {
						throw new AlignmentException("Unbalanced tag at "+locator.getLocationAsLongString()+" (tagged text input reader).");
					}
				}
				// whatever the reason (end of input or not-token-char), token has been totally read
				token = data.toString();
			}
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while reading at "+locator.getLocationAsLongString()+" in tagged text input.", e);
		}
		moveToNextToken();
		boolean processToken = true;
		if (filter != null) {
			String newToken = filter.filteredToken(token, annot);
			annot = filter.filteredAnnot(token, annot);
			token = newToken; // possibly transformed token
			processToken = filter.sendTokenAndAnnot();
		}
		if (processToken) {
			//System.out.println("DEBUG TaggedTextFileReader token="+token+", annot="+annot);
			inputReader.receiveTokenAndAnnotation(alignerConsumer, token, annot);
		}
	}

	
	@Override
	public boolean hasToken() {
		return (currCar != -1); 
	}
	
	
	/**
	 * Increments current position until a non-whitespace character is reached
	 *
	 */
	protected void moveToNextToken() throws AlignmentException {
		try {
			while ((currCar != -1) && considerAsWhitespace(currCar)) {
				currCar = reader.read();
				locator.readChar(currCar);
			}
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while reading input text at "+locator.getLocationAsLongString()+".", e);			
		}
	}
	
	/**
	 * Flag to accept a &gt; alone in a text, i.e. not related to a tag.
	 * @return the value of that flag
	 */
	public boolean getAcceptClosingTagCharAlone() {
		return acceptClosingTagCharAlone;
	}

	/**
	 * Flag to accept a &gt; alone in a text, i.e. not related to a tag.
	 * @param acceptClosingTagCharAlone the value of that flag
	 */
	public void setAcceptClosingTagCharAlone(boolean acceptClosingTagCharAlone) {
		this.acceptClosingTagCharAlone = acceptClosingTagCharAlone;
	}
	
	
	protected boolean considerAsWhitespace(int c) {
		if (Character.isWhitespace(currCar)) {
			return true;
		}
		if (otherWhiteSpaceChars == null) {
			return false;
		}
		for (int i=0; i<otherWhiteSpaceChars.length; i++) {
			if (c==otherWhiteSpaceChars[i]) {
				return true;
			}
		}
		return false;
	}

	public FilterTokenAndAnnot getFilter() {
		return filter;
	}

	public void setFilter(FilterTokenAndAnnot filter) {
		this.filter = filter;
	}

	public char[] getOtherWhiteSpaceChars() {
		return otherWhiteSpaceChars;
	}

	public void setOtherWhiteSpaceChars(char[] otherWhiteSpaceChars) {
		this.otherWhiteSpaceChars = otherWhiteSpaceChars;
	}


}
