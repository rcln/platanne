package fr.lipn.nlptools.uima.common;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;

import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * TODO doc
 * @author moreau
 *
 */
public class TokenByLineWriterFeeder implements WriterFeeder {

	public final static String DEFAULT_COLUMN_SEPARATOR = " ";
	public final static String DEFAULT_NULL_STRING_REPLACEMENT_VALUE = "";
	String lineSeparator;
	String columnSeparator;
	Iterator<String[]> dataIterator;
	String nullStringReplacementValue;
	
	
	public TokenByLineWriterFeeder(Iterator<String[]> dataIterator, String lineSeparator, String columnSeparator, String nullStringReplacementValue) {
		this.lineSeparator = lineSeparator;
		this.columnSeparator = columnSeparator;
		this.dataIterator = dataIterator;
		this.nullStringReplacementValue = nullStringReplacementValue;
	}

	public TokenByLineWriterFeeder(Iterator<String[]> dataIterator, String columnSeparator) {
		this(dataIterator, LipnExternalProgramGenericAnnotator.LINE_SEPARATOR, columnSeparator, DEFAULT_NULL_STRING_REPLACEMENT_VALUE);
	}

	public TokenByLineWriterFeeder(Iterator<String[]> dataIterator) {
		this(dataIterator, LipnExternalProgramGenericAnnotator.LINE_SEPARATOR, DEFAULT_COLUMN_SEPARATOR, DEFAULT_NULL_STRING_REPLACEMENT_VALUE);
	}
	
	public void feedWriter(Writer writer) throws ExternalProgramException {
		try {
			while (dataIterator.hasNext()) {
				String[] line = dataIterator.next();
				//System.err.print("columnSeparator='"+columnSeparator+"' FEEDERINPUT=");
				for (int i=0; i <line.length-1; i++) {
					//System.err.print(((line[i]!=null)?line[i]:nullStringReplacementValue) + columnSeparator);
					writer.write(((line[i]!=null)?line[i]:nullStringReplacementValue) + columnSeparator);
				}
				//System.err.print(((line[line.length-1]!=null)?line[line.length-1]:nullStringReplacementValue) + lineSeparator);
				writer.write(((line[line.length-1]!=null)?line[line.length-1]:nullStringReplacementValue) + lineSeparator);
			}
			writer.flush();
			writer.close();
		} catch(IOException e) {
			throw new ExternalProgramException("I/O error in token-by-line writer feeder.", e);
		}			
	}

}
