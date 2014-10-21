package fr.lipn.nlptools.utils.align;

import java.io.Reader;
import java.util.ArrayList;


/**
 * 
 * General reader processing alignment in a simple way: simply sends separators (if necessary) to the consumer; any token received 
 * from the annotated text must at least be a prefix for the current token in the reference text (possibly ignoring case). That means that
 *  it is allowed that several tokens in the annotated text correspond to only one in the reference text, but the converse is false.
 * 
 * This InputReader can be used with any kind of <code>java.io.Reader</code> (file, string, etc.)<br/>
 * Examples:
 * <ul>
 * <li>A simple text file: <code>new SimpleInputReader(new java.io.FileReader(filename))</code></li>
 * <li>A text file with special encoding: <code>new SimpleInputReader(new java.io.InputStreamReader(new java.io.FileInputStream(filename), encoding))</code></li>
 * <li>A java.lang.String object: <code>new SimpleInputReader(new java.io.StringReader(myString))</code></li>
 * </ul>
 *  
 *  The input must not be empty.<br/>
 *  NB: Case is ignored by default.
 *  
 * @author moreau
 *
 */
public class SimpleInputReader implements InputReader {

	Reader reader;
	StringBuilder currSeparator;
	long lastTokenEndPos = 0;
	boolean sendUnannotatedTokens;
	boolean sendSeparator;
	int currCar =-1;
	char[] otherWhiteSpaceChars = null;
	boolean ignoreCase = true;
	int wordsSequenceChar = -1;
	char[] wildcardChars;
	CharByCharTextLocator locator;
	boolean skipWhileNoMatch;
	boolean storeWarnings = true;
	ArrayList<String> warnings;
	
	
	/**
	 * Characters corresponding to non-breaking spaces are not considered as whitespaces by <code>Character.isWhitespace(char)</code>
	 * (see Java API). This flag controls whether the aligner extends whitespaces to this set of characters (true) or not (false, default).
	 * This flag can be accessed using {@link #getConsiderNonBreakingSpacesAsWhitespaces()} and {@link #setConsiderNonBreakingSpacesAsWhitespaces(boolean)}.
	 */
	boolean considerNonBreakingSpacesAsWhitespaces = false;
	
	/**
	 * Characters corresponding to non-breaking spaces, as listed in Java API for <code>Character.isWhitespace(char)</code>.
	 */
	public static final char[] NON_BREAKING_SPACES = { '\u00A0', '\u2007', '\u202F' };
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static int separatorsBufferSize = 2048;
	
	
	/**
	 * Detailed constructor.
	 * 
	 * @param reader the Reader object from which the input text is read
	 * @param otherWhiteSpaceChars a set of characters that will be considered as whitespaces (if null only standard whitespaces are used, as
	 *                             defined by <code>Character.isWhitespace(char)</code>)
	 * @param wildcardChars A set of characters that will be considered as wildcards, in the sense of the classical '?' regexp operator. In other words
	 *                      any char in this set will be allowed to match any other char, but only in the case where this 'wildcard' is read from
	 *                      the annotated text (matched against the original input text). This exception is introduced to skip cases where a character
	 *                      en/de-coding error happened: if the coding process was defined with a <code>CodingErrorAction</code> object to replace
	 *                      the faulty chars by some special char, then this char must be considered as a wildcard (otherwise the char from the 
	 *                      original text will of course be different from the one read in the annotated text, though that was on purpose).
	 * @param wordsSequenceChar	a special char that is used by the annotated text reader (generally in the annotated text) to denote
	 *                          multi-words token: e.g. if this char is '_' and the token "as_soon_as" is sent, the input reader will
	 *                           be able to match it with the corresponding three words sequence (otherwise it would raise an alignment error).
	 *                           Default value = -1 (do not use).
	 * @param skipWhileNoMatch allows skipping characters from the original input until a match is found with the expected token (from annotated text)
	 *                         This option is NOT recommended: it should be used for debugging purpose only, or in very special cases, because
	 *                         it makes the matching very loose. If it is enabled, any character can be considered as a whitespace in the original
	 *                         input, thus what is checked os only that each annotated token has a corresponding token in the original input.
	 *                         Notice that information about location of an error will often be wrong using this option, since the reader may
	 *                         read the whole input before throwing an error.
	 *                         Warnings about skipped chars are written in {@link #warnings} and can be accessed using {@link #getWarnings()}.     
	 */
	public SimpleInputReader(Reader reader, final char[] otherWhiteSpaceChars, final char[] wildcardChars, int wordsSequenceChar, boolean skipWhileNoMatch) {
		this.otherWhiteSpaceChars = otherWhiteSpaceChars;
		this.reader = reader;
		this.wildcardChars = wildcardChars;
		this.wordsSequenceChar = wordsSequenceChar;
		this.skipWhileNoMatch = skipWhileNoMatch;
	}

	public SimpleInputReader(Reader reader, final char[] otherWhiteSpaceChars, final char[] wildcardChars, int wordsSequenceChar) {
		this(reader, otherWhiteSpaceChars, wildcardChars, wordsSequenceChar, false);
	}

	public SimpleInputReader(Reader reader, final char[] otherWhiteSpaceChars, final char[] wildcardChars) {
		this(reader, otherWhiteSpaceChars, wildcardChars, -1, false);
	}

	public SimpleInputReader(Reader reader, int wordsSequenceChar) {
		this(reader, null, null, wordsSequenceChar, false);
	}

	
	public SimpleInputReader(Reader reader) {
		this(reader, null, null, -1, false);
	}

	/**
	 * Moves to next token: possibly sends the separators read during this move to the consumer.
	 */
	public void initReader(AlignerConsumer alignerConsumer) throws AlignmentException {
		warnings = new ArrayList<String>();
		locator = new CharByCharTextLocator();
		sendUnannotatedTokens = alignerConsumer.requiresUnannotatedTokens();
		sendSeparator = alignerConsumer.requiresSeparator();
		if (sendSeparator) {
			currSeparator = new StringBuilder(separatorsBufferSize);
		}
		try {
			/*	if (!reader.ready()) { BAD IDEA: a reader that is not ready may be able to send (later). 
				throw new AlignmentException("Input reader is not ready.");
			}*/
			currCar = reader.read();
			locator.readChar(currCar);
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error: input reader fails to read first character (maybe empty input).", e);
		}
		moveToNextToken();
		// send first separator
		if (sendSeparator) {
			alignerConsumer.receiveTokenAndAnnotation(null, null, null, currSeparator.toString(), 0, locator.getCharPosition());
			currSeparator.setLength(0);
		}
	}

	
	/**
	 * closes the reader.
	 */
	public void closeReader(AlignerConsumer consumer) throws AlignmentException {
		if (currCar != -1) {
			throw new AlignmentException("There are remaining tokens in reference input text when closing.");
		}
		try {
			reader.close();
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while closing input reader.", e);
		}
	}
	
	
	/**
	 * Very simple alignment method: the next token must start with the token received from the annotated text.
	 * In other words, this alignment method will handle correctly cases where one or several tokens correspond to only one
	 * token in the reference text, but will fail in the opposite case (several tokens in reference corresponding to only one 
	 * in annotated text). Since tokens in this reference reader are simply "words without whitespaces", i.e. the most basic
	 *  kind of tokenization, that method should be sufficiant because any smarter tokenizer will likely create more tokens, not 
	 *  less. For example punctuation marks are glued to the word before it : "it works." = [ "it" , "works." ], whereas a 
	 *  "normal" tokenizer will send [ "it" , "works", "." ] (more tokens).<br/>
	 *
     * Caution: if position is between two different tokens (i.e. no token is given), sets end=end of previous token and start=start of next token
	 * That means that start>end in this case ! but consumer is supposed to receive something like &lt;start annot&gt;&lt;token&gt;&lt;end annot&gt;,
	 * so it is relevant (because  a starting annot concerns the next token whereas an ending one concerns the previous one)
	 * 
	 * @param token the token to find.
	 * @throws AlignmentException if the tokens are different.
	 */
	public void receiveTokenAndAnnotation(AlignerConsumer consumer, String token, String annotation) throws AlignmentException {
		//System.err.println("debug: receiving '"+token+"', at "+locator.getLocationAsLongString());
		long startPos = locator.getCharPosition();
		long endPos = lastTokenEndPos;
		String separator = null;
		String refToken = null;
		if ((token != null) && (!token.isEmpty())) {
			refToken = readerStartsWith(token);
			if (refToken != null) {
				endPos = locator.getCharPosition();
				lastTokenEndPos = endPos;
				moveToNextToken();
			} else {
				throw new AlignmentException("Token '"+token+"' not found by input reader at "+locator.getLocationAsLongString()+".");
			}
		}
		if (sendSeparator) {
			separator = currSeparator.toString();
		}
		if (sendUnannotatedTokens || (annotation != null)) {
			//System.err.println("DEBUG: IR sending *"+token+"*"+annotation+"*"+separator+"*"+startPos+"*"+endPos);
			consumer.receiveTokenAndAnnotation(token, refToken, annotation, separator, startPos, endPos);
		}
		if (sendSeparator) {
			currSeparator.setLength(0);
		}
	}

	/**
	 * Reads the next characters in the input and compares them to the given token.
	 * 
	 * @param token the string to find, must not be null
	 * @return the original (input text) string if the input contains the token at the current position, null otherwise.
	 * @throws AlignmentException in case end if input is reached before reading the token or comparison fails
	 */
	protected String readerStartsWith(String token) throws AlignmentException {
		int tokPos = 0;
		boolean skipping = false;
		StringBuilder s = new StringBuilder();
		try {
			while ( (tokPos < token.length()) && (currCar != -1) && 
					( (currCar == token.charAt(tokPos)) || 
					  (ignoreCase && (Character.toLowerCase(currCar) == Character.toLowerCase(token.charAt(tokPos)))) ||
					  (considerAsWhitespace(currCar) && (token.charAt(tokPos) == wordsSequenceChar)) ||
					  isWildcardChar(token.charAt(tokPos)) ||   // the wildcard "option" MUST be tested after all others, because the corresponding
					                                      		 // character from (original) input text will be skipped, so if it is skipped whereas
																 // it should have been matched that will cause an error with the chars after this one.
					  (skipWhileNoMatch && (skipping = true)) ) ) {  // Actually the same applies for skipping chars in the input text (it's even worst)
																	// Caution: the '=' sign is on purpose, it is an assignment and not a comparison.
				s.append((char) currCar);
//				System.out.println("debug startsWith: current="+s.toString());
				if (skipping) {
					if (storeWarnings) {
						warnings.add("Skipping char '"+(char) currCar+"' ("+currCar+") at "+locator.getLocationAsLongString());
					}
					skipping = false;
				} else {
					tokPos++;
				}
				currCar = reader.read();
				locator.readChar(currCar);
				if (skipping) {
					moveToNextToken();
//					System.err.println("Debug: now testing char '"+(char) currCar+"' ("+currCar+"), compared to '"+token.charAt(tokPos)+"'.");
				}
			}
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while looking for token '"+token+"' at "+locator.getLocationAsLongString()+".", e);			
		}
		if (tokPos < token.length()) {
			if (currCar == -1) {
				throw new AlignmentException("End of input reached while looking for token '"+token+"' at "+locator.getLocationAsLongString()+".");
			} else {
				return null;
			}
		} else {
			return s.toString();
		}
	}
		

	/**
	 * Increments current position until a non-whitespace character is reached, and stores separators.
	 *
	 */
	protected void moveToNextToken() throws AlignmentException {
//		int nb=0;
		try {
	//		System.err.println("test whitespace '"+(char) currCar+"' ("+currCar+") ...");
			while ((currCar != -1) && considerAsWhitespace(currCar)) {
		//		System.err.println("skip '"+(char) currCar+"' ("+(++nb)+")");
				if (sendSeparator) {
					currSeparator.append((char) currCar);
				}
				currCar = reader.read();
				locator.readChar(currCar);
//				System.err.println("test whitespace '"+(char) currCar+"' ("+currCar+") ...");
			}
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while reading input text at "+locator.getLocationAsLongString()+".", e);			
		}
	}

	public void setSeparatorsBufferSize(int size) {
		separatorsBufferSize = size;
	}
	
	public int getSeparatorsBufferSize() {
		return separatorsBufferSize;
	}

	/**
	 * A character <code>c</code> is a whitespace if (1) Java <code>Character.isWhitespace(c)</code> returns true, or (2) flag
	 * <code>considerNonBreakingSpacesAsWhitespaces</code> is true and <code>c</code> belongs to {@link #NON_BREAKING_SPACES}, or
	 * (3) {@link #otherWhiteSpaceChars} is not null and contains <code>c</code>.
	 * 
	 * @param c
	 * @return true if one of these three conditions holds, false otherwise.
	 */
	protected boolean considerAsWhitespace(int c) {
		if (Character.isWhitespace(c)) {
			return true;
		}
		if 	(considerNonBreakingSpacesAsWhitespaces) {
			for (int i=0; i<NON_BREAKING_SPACES.length; i++) {
				if (c==NON_BREAKING_SPACES[i]) {
					return true;
				}
			}
		}
		if (otherWhiteSpaceChars == null) {
			return false;
		} else {
			for (int i=0; i<otherWhiteSpaceChars.length; i++) {
				if (c==otherWhiteSpaceChars[i]) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean isWildcardChar(int c) {
		if (wildcardChars != null) {
			for (int i=0; i<wildcardChars.length; i++) {
				if (c==wildcardChars[i]) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean getIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public int getWordsSequenceChar() {
		return wordsSequenceChar;
	}

	public void setWordsSequenceChar(int wordsSequenceChar) {
		this.wordsSequenceChar = wordsSequenceChar;
	}
	
	public boolean getConsiderNonBreakingSpacesAsWhitespaces() {
		return considerNonBreakingSpacesAsWhitespaces;
	}

	public void setConsiderNonBreakingSpacesAsWhitespaces(boolean value) {
		considerNonBreakingSpacesAsWhitespaces = value;
	}

	public boolean getStoreWarnings() {
		return storeWarnings;
	}

	public void setStoreWarnings(boolean storeWarnings) {
		this.storeWarnings = storeWarnings;
	}

	/**
	 * To enable/disable storing warnings, see {@link #getStoreWarnings()} and {@link #setStoreWarnings(boolean)}.
	 * 
	 * @return the warnings that have been stored during process, mainly about skipped chars.
	 */
	public ArrayList<String> getWarnings() {
		return warnings;
	}


}
