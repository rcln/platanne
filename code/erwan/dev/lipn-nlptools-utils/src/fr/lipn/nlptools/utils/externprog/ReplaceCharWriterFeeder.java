package fr.lipn.nlptools.utils.externprog;

import java.io.IOException;
import java.io.Writer;

/**
 * A @link WriteFeeder object which reads the input from a String and possibly performs
 * replacements of a given char.
 * 
 * 
 * @author erwan
 *
 */
public class ReplaceCharWriterFeeder implements WriterFeeder {

	char charToReplace;
	String replaceWith;
	String input;
	
	public ReplaceCharWriterFeeder(String input, char charToReplace, String replaceWith) {
		this.input = input;
		this.charToReplace = charToReplace;
		this.replaceWith = replaceWith;
	}
	
	public void feedWriter(Writer writer) throws ExternalProgramException {
		int currentPos = 0;
		int nextCharToReplace = input.indexOf(charToReplace, currentPos);
		try {
			while (nextCharToReplace != -1) {
				writer.write(input.substring(currentPos, nextCharToReplace));
				writer.write(replaceWith);
				currentPos = nextCharToReplace+1;
				nextCharToReplace = input.indexOf(charToReplace, currentPos);
			}
			writer.write(input.substring(currentPos));
			writer.close();
		} catch (IOException e) {
			throw new ExternalProgramException("An I/O error happened", e);
		}
	}

}
