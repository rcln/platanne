package fr.lipn.nlptools.utils.externprog;

import java.io.StringWriter;

/**
 * This {@link ExternalProgram.ReaderConsumer} object stores an output stream in a String.
 * The resulting String is obtained after the process has ended using {@link #getString()}.
 * Convenience class.
 * @author moreau
 *
 */
public class StringReaderConsumer extends ToWriterReaderConsumer {
	
	public StringReaderConsumer() {
		this(ExternalProgram.DEFAULT_BUFFER_SIZE);
	}
	
	public StringReaderConsumer(int bufferSize) {
		super(new StringWriter(), bufferSize);
	}
	
	public String getString() {
		return ((StringWriter) super.getTarget()).toString();
	}
}
