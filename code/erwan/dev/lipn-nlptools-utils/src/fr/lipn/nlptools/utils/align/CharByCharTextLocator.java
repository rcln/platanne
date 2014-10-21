package fr.lipn.nlptools.utils.align;


/**
 * 
 * This class is used to provide detailed information in error messages about the position where an error happens.
 * It simply handles different indexes about the position when reading a text from any object, it does not deal with the reading itself.
 * TODO There may exist standard objects with this kind of behaviour: to check.
 * 
 * @author erwan
 *
 */
public class CharByCharTextLocator {

	int lineNo;
	int colNo;
	int charNo;
	boolean lastWasCR;
	boolean endOfInput;
	
	/**
	 * 
	 * Initializes a locator with custom values
	 * 
	 * @param lineNo init line number (usually 0)
	 * @param colNo init column number (usually 1)
	 * @param charPosition init position (counting chars, usually -1)
	 */
	public CharByCharTextLocator(int lineNo, int colNo, int charPosition) {
		this.lineNo = lineNo;
		this.colNo = colNo;
		this.charNo = charPosition;
		lastWasCR = false; 
		endOfInput = false;
	}

	/**
	 * Default constructor: lineNo is set to 0, ColNo is set to 1, charPosition is set to -1.
	 *
	 */
	public CharByCharTextLocator() {
		this(0, 1, -1); // col = 0 since first char is not read yet (will be col 1), and charNo=-1 for the same reason (pos will be 0 for first char)
	}

	public void readChar(int c) {
		if ((c != -1) || !endOfInput) { // explanation: the position must be incremented when reaching the end of the file in order to be positioned
			// 'after' the last char, because this is the right length of line/document/last string
			if (c == '\r') {
				lastWasCR = true;
				lineNo++;
				colNo = 1;
			} else if (c == '\n') {
				if (lastWasCR) {
					lastWasCR = false;
				} else {
					lineNo++;
					colNo = 1;
				}
			} else if (c == -1) {
				endOfInput = true;
			}
			charNo++;
			colNo++;
		}
	}
	
	public String getLocationAsLCString() {
		return "L"+lineNo+",C"+colNo;
	}
	
	public String getLocationAsLongString() {
		return "line "+lineNo+", column "+colNo;
	}
	
	public int getLineNo() {
		return lineNo;
	}
	
	public int getColNo() {
		return colNo;
	}
	
	public int getCharPosition() {
		return charNo;
	}
}
