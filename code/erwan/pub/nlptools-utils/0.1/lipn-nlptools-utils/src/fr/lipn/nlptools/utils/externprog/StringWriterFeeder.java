package fr.lipn.nlptools.utils.externprog;

import java.io.IOException;
import java.io.Writer;

import fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder;

/**
 * This {@link ExternalProgram.WriterFeeder} object takes stdin as a String and writes it to the stream.
 * @author moreau
 *
 */
public class StringWriterFeeder implements WriterFeeder {
	
	String content;
	
	public StringWriterFeeder(String content) {
		this.content = content;			
	}
	
	public void feedWriter(Writer writer) throws ExternalProgramException {
		try {
			writer.write(content);
			writer.flush();
			writer.close();
		} catch(IOException e) {
			throw new ExternalProgramException("I/O error in string writer feeder.", e);
		}			
	}
	
}
