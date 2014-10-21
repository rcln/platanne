package fr.lipn.nlptools.utils.align;

import java.util.Iterator;

import fr.lipn.nlptools.utils.misc.PositionedText;


/**
 * 
 * This <code>InputReader</code> reads tokens from an iterator over {@link PositionedText} objects.
 * 
 * TODO As far as I remember the class has not been tested with the separator iterator. 
 * 
 * 
 * @author moreau
 *
 */
public class TokenIteratorInputReader implements InputReader {

	boolean ignoreCase = true;
	Iterator<PositionedText> tokenIterator;
	Iterator<PositionedText> separatorIterator;
	PositionedText nextToken;
	long lastPos = 0;
	boolean doNotCompare;
	char[] wildcardChars;


	/**
	 * Detailed constructor
	 * 
	 * @param ignoreCase Ignore case during token comparison (true by default)
	 * @param tokenIterator Iterator over the tokens (should match exactly the tokens sent by the {@link AnnotatedTextReader}, possibly ignoring case)
	 * @param separatorIterator if used, must contain exactly N+1 separators, where N is the number of tokens contained in <code>tokenIterator</code>.
	 *                          Set to null not to use.
	 * @param doNotCompareTokens Skip the comparison between the original (input) token and the one read by the annotated reader. This option should be
	 *                     used with caution: if set, the alignment between both sources is not controlled anymore (the only error that the aligner
	 *                     can catch in this case is when the number of tokens differ). This option is mainly proposed as a work-around in the case
	 *                     where errors are expected (originally due to a bug in TreeTagger, which has been corrected now).                            
	 * @param wildcardChars A set of characters that will be considered as wildcards, in the sense of the classical '?' regexp operator. In other words
	 *                      any char in this set will be allowed to match any other char, but only in the case where this 'wildcard' is read from
	 *                      the annotated text (matched against the original input text). This exception is introduced to skip cases where a character
	 *                      en/de-coding error happened: if the coding process was defined with a <code>CodingErrorAction</code> object to replace
	 *                      the faulty chars by some special char, then this char must be considered as a wildcard (otherwise the char from the 
	 *                      original text will of course be different from the one read in the annotated text, though that was on purpose).
	 */
	public TokenIteratorInputReader(boolean ignoreCase, Iterator<PositionedText> tokenIterator, Iterator<PositionedText> separatorIterator, boolean doNotCompareTokens, char[] wildcardChars) {
		this.ignoreCase = ignoreCase;
		this.tokenIterator = tokenIterator;
		this.separatorIterator = separatorIterator;
		this.doNotCompare = doNotCompareTokens;
		this.wildcardChars  = wildcardChars;
	}

	public TokenIteratorInputReader(Iterator<PositionedText> tokenIterator, boolean doNotCompare, char[] wildcardChars) {
		this(true, tokenIterator, null, doNotCompare, wildcardChars);
	}

	public TokenIteratorInputReader(Iterator<PositionedText> tokenIterator, char[] wildcardChars) {
		this(true, tokenIterator, null, false, wildcardChars);
	}

	public TokenIteratorInputReader(Iterator<PositionedText> tokenIterator, boolean doNotCompare) {
		this(true, tokenIterator, null, doNotCompare, null);
	}

	public TokenIteratorInputReader(Iterator<PositionedText> tokenIterator) {
		this(true, tokenIterator, null, false, null);
	}
	
	/**
	 * Checks that the iterator(s) do not have more tokens
	 */
	public void closeReader(AlignerConsumer consumer) throws AlignmentException {
		if (tokenIterator.hasNext()) {
			PositionedText t = tokenIterator.next();
			throw new AlignmentException("Tokens remaining in the input. Next token: '"+t.getText()+"', position "+t.getStart());
		}
		if ((separatorIterator != null) && separatorIterator.hasNext()) {
			throw new AlignmentException("Separators remaining in the input.");
		}
	}

	/**
	 * reads the first token (if any), and
	 * if separators are required and the separator iterator is not null, sends the first one
	 */
	public void initReader(AlignerConsumer consumer) throws AlignmentException {
		if (tokenIterator.hasNext()) {
			nextToken = tokenIterator.next();
		} else {
			nextToken = null;
		}
		if (consumer.requiresSeparator()) {
			if (separatorIterator == null) {
				throw new AlignmentException("Separators needed but no separator iterator provided.");
			} else {
				if (separatorIterator.hasNext()) {
					PositionedText e = separatorIterator.next();
					consumer.receiveTokenAndAnnotation(null, null, null, e.getText(), e.getStart(), e.getEnd());
					lastPos = e.getEnd();
				}
			}
		} else {
			separatorIterator = null;
		}
		
	}

	/**
     * Caution: if position is between two different tokens (i.e. no token is given), sets end=end of previous token and start=start of next token
	 * That means that start>end in this case ! but consumer is supposed to receive something like &lt;start annot&gt;&lt;token&gt;&lt;end annot&gt;,
	 * so it is relevant (because  a starting annot concerns the next token whereas an ending one concerns the previous one)
	 * 
	 */
	public void receiveTokenAndAnnotation(AlignerConsumer consumer,	String token, String annotation) throws AlignmentException {
		long startPos = (nextToken != null)?nextToken.getStart():lastPos;
		long endPos = lastPos;
		String separator = null;
		String refToken = null;
		boolean match = true;
		if ((token != null) && (!token.isEmpty())) {
			if (nextToken != null) {
				if (!doNotCompare) {
					if (ignoreCase) {
						match = token.equalsIgnoreCase(nextToken.getText());
					} else {
						match = token.equals(nextToken.getText());
					}
					if (!match && (wildcardChars != null)) {
						match = compareModuloWilcardChars(token, nextToken.getText());
					}
				}
				if (match) {
					refToken = nextToken.getText();
					startPos = nextToken.getStart();
					endPos = nextToken.getEnd();
					lastPos = nextToken.getEnd();
				} else {
					throw new AlignmentException("Input reader: tokens differ position "+nextToken.getStart()+"-"+nextToken.getEnd()+" (input reader position): annotated='"+token+"', input reader token='"+nextToken.getText()+"'");
				}
				if (tokenIterator.hasNext()) {
					nextToken = tokenIterator.next();
				} else {
					nextToken = null;
				}
			} else {
				throw new AlignmentException("Input reader: no more tokens.");
			}			
			if (separatorIterator != null) {
				if (separatorIterator.hasNext()) {
					separator = separatorIterator.next().getText();
				} else {
					throw new AlignmentException("Input reader: no more separators.");				
				}
			}
		}
		if (consumer.requiresUnannotatedTokens() || annotation != null) {
			consumer.receiveTokenAndAnnotation(token, refToken, annotation, separator, startPos, endPos);
		}


	}

	/**
	 * Comparison taking 'wildcard chars' into account. Used only if the simple comparison fails.
	 * 
	 * @param token the token read from the annotated text
	 * @param originalToken the token read from the original input text
	 * @return true if the strings match, false otherwise
	 */
	protected boolean compareModuloWilcardChars(String token, String originalToken) {
		if (token.length() == originalToken.length()) {
			int i = 0;
			boolean same = true;
			while (same && (i<token.length())) {
				same = (token.charAt(i)  == originalToken.charAt(i)) || (ignoreCase && (Character.toLowerCase(token.charAt(i))  == Character.toLowerCase(originalToken.charAt(i))));
				if (!same) {
					for (int j=0; j < wildcardChars.length; j++) {
						if (token.charAt(i) == wildcardChars[j]) {
							same = true;
						}
					}
				}
				i++;
			}
			return same;
		} else {
			return false;
		}

	}

}
