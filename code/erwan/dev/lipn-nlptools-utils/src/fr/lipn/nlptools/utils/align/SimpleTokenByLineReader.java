package fr.lipn.nlptools.utils.align;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This {@link AnnotatedTextReader} reads input from a <code>Reader</code> object where annotations are provided
 *  line by line, i.e. each line has the form: "&lt;token&gt; &lt;annot 1&gt;...&lt;annot N&gt;"
 * 
 * {@link #processNextToken(InputReader, AlignerConsumer)} will send only the information line by line: the first non-whitespace sequence
 * in the line is considered as the token, and the remaining of the line are sent as the annotation.
 * 
 * This AnnotatedTextReader can be used with any kind of <code>java.io.Reader</code> (file, string, etc.)<br/>
 *
 * @author moreau
 *
 */
public class SimpleTokenByLineReader extends AnnotatedTextReader {

	public static final String DEFAULT_SEPARATOR_REGEX = "\\s+";
	BufferedReader reader;
	String currLine = null;
	int numLine;
	Pattern separatorPattern = null;
	Pattern additionalLineSeparator;
	FilterTokenAndAnnot filter = null;

	/**
	 * Detailed constructor.
	 * 
	 * @param reader the reader from which annotated text is read
	 * @param separatorRegex the regular expression describing the separator to consider. if null, default regex is 
	 *                       used (non-empty sequence of whitespaces)
	 * @param filter An object implementing {@link FilterTokenAndAnnot}, which is used to pre-process some special
	 *               cases. It is called in {@link #processNextToken(InputReader, AlignerConsumer)} before calling
	 *                {@link InputReader#receiveTokenAndAnnotation(AlignerConsumer, String, String)}, thus this object
	 *                is able to remove/transform the token and annotation that were read. null = do not use.
	 *  @param additionalLineSeparatorRegex An additional "line separator", in other words a regexp describing some
	 *                                      separator which sould be interpreted as a newline (i.e. a frontier before a new token).
	 *                                      Notice that new lines are always such separators, even in this case. null = do not use. 
	 */
	public SimpleTokenByLineReader(BufferedReader reader, String separatorRegex, FilterTokenAndAnnot filter, String additionalLineSeparatorRegex) {
		this.reader = reader;
		if (separatorRegex == null) {
			separatorRegex = DEFAULT_SEPARATOR_REGEX;
		}
		if (separatorRegex != null) {
			this.separatorPattern = Pattern.compile(separatorRegex);
		} else {
			this.separatorPattern = Pattern.compile(DEFAULT_SEPARATOR_REGEX);
		}
		this.filter = filter;
		if (additionalLineSeparatorRegex != null) {
			this.additionalLineSeparator = Pattern.compile(additionalLineSeparatorRegex);
		}
	}

	public SimpleTokenByLineReader(BufferedReader reader, FilterTokenAndAnnot filter) {
		this(reader, null, filter, null);
	}

	public SimpleTokenByLineReader(BufferedReader reader, String separatorRegex) {
		this(reader, separatorRegex, null, null);
	}

	public SimpleTokenByLineReader(BufferedReader reader) {
		this(reader, null, null, null);
	}
	
	
	@Override
	/**
	 * only reads first line
	 */
	public void initReader(InputReader inputReader,	AlignerConsumer alignerConsumer) throws AlignmentException {
		try {
			/* BAD IDEA: the reader may not be ready even if it will be able to be read (sooner or later)
			if (!reader.ready()) {
				throw new AlignmentException("Input reader is not ready.");
			}
			*/
			currLine = reader.readLine();
			numLine = 1;
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error: input reader is not ready.", e);
		}
	}

	/**
	 * closes the reader
	 */
	@Override
	public void closeReader(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException {
		try {
			reader.close();
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while closing tagged text reader.", e);
		}
	}

	@Override
	public boolean hasToken() {
		return (currLine != null);
	}


	/**
	 * tries to find the first separator in the line, then consider the left part as the token (blanks prefix is removed if necessary)
	 *  and the right part as the annotation.
	 *  Filtering can be used with {@link FilterTokenAndAnnot}
	 * NB: Sends null token and null annot for each empty line (or line starting with whitespaces): since empty lines
	 *  are generally used as sentence separator, the receiver thus knows where these separators are.  
	 */
	@Override
	public void processNextToken(InputReader inputReader, AlignerConsumer alignerConsumer) throws AlignmentException {
		if (currLine != null) {
			//System.err.println("debug - reading "+currLine);
			String[] dataLines;
			if (additionalLineSeparator != null) { 
				// I must admit this 'additionalLineSeparator' option make things seem more complex for a really small improvement,
				// but actually this is not so complex.
				dataLines = additionalLineSeparator.split(currLine, -1);
			} else {
				dataLines = new String[]{ currLine };
			}
			// this 'for' loop can seem strange: don't forget that in the default case there is only one pass and 'data' = 'currLine'. 
			for (int i=0; i < dataLines.length; i++) {
				String data = dataLines[i];
				//System.err.println("debug - data["+i+"]= '"+data+"'");
				Matcher matcher = separatorPattern.matcher(data);
				boolean foundSeparator = matcher.find();
				if (data.isEmpty() || (foundSeparator && (matcher.start() == 0) && (matcher.end() >= data.length()))) { 
					// either line is empty or contains only whitespaces
					inputReader.receiveTokenAndAnnotation(alignerConsumer, null, null);
				} else {
					int startToken = 0;
					if (foundSeparator && (matcher.start() == 0)) { // line starts with blanks -> go to next separator
						startToken = matcher.end();  // remember where token starts
						foundSeparator = matcher.find();
					}
					String token;
					String annot;
					if (foundSeparator) {  // separator exists after the token (normal case)
						token = data.substring(startToken, matcher.start());
						annot = data.substring(matcher.end(), data.length());
					} else {  // no separator -> no annotation
						token = data.substring(startToken, data.length());
						annot = null;
					}
					boolean processToken = true;
					if (filter != null) {
						String newToken = filter.filteredToken(token, annot);
						annot = filter.filteredAnnot(token, annot);
						token = newToken; // possibly transformed token
						processToken = filter.sendTokenAndAnnot();
					}
					if (processToken) {
						try {
							//System.err.println("DEBUG AR: sending '"+token+"'/'"+annot+"'");
							inputReader.receiveTokenAndAnnotation(alignerConsumer, token, annot);
						} catch (AlignmentException e) {
							throw new AlignmentException("Error was found reading line "+numLine+" in annotated text: '"+data+"'", e);
						}
					}
				}
			}
		} else {
			throw new AlignmentException("Error: no more tokens.");
		}
		try {
			currLine = reader.readLine();
			numLine++;
		} catch (java.io.IOException e) {
			throw new AlignmentException("I/O error while reading line "+numLine+" in tagged text input.", e);

		}
	}


	public FilterTokenAndAnnot getFilter() {
		return filter;
	}

	public void setFilter(FilterTokenAndAnnot filter) {
		this.filter = filter;
	}



}
