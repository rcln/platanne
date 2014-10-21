package fr.lipn.nlptools.utils.externprog;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder;

/**
 * This {@link ExternalProgram.WriterFeeder} object reads the content of a generic Reader object and writes it to the stdin stream.
 * @author moreau
 *
 */
public class FromReaderWriterFeeder implements WriterFeeder {
	
	Reader reader;
	
	public FromReaderWriterFeeder(Reader reader) {
		this.reader = reader;			
	}
	
	public void feedWriter(Writer writer) throws ExternalProgramException {
		try {
			int c = reader.read();
			while (c != -1) {
				writer.write(c);
				c = reader.read();
			}
			reader.close();
			writer.flush();
			writer.close();
		} catch(IOException e) {
			throw new ExternalProgramException("I/O error in FromReaderWriterFeeder.", e);
		}			
	}
	
}
