package fr.lipn.nlptools.utils.externprog;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

import fr.lipn.nlptools.utils.externprog.ExternalProgram.ReaderConsumer;

/**
 * This {@link ExternalProgram.ReaderConsumer} object writes an output stream to a generic Writer object.
 * @author moreau
 *
 */
public class ToWriterReaderConsumer implements ReaderConsumer {
	
	int bufferSize = ExternalProgram.DEFAULT_BUFFER_SIZE;
	Writer target;

	public ToWriterReaderConsumer(Writer target) {
		this(target, ExternalProgram.DEFAULT_BUFFER_SIZE);
	}
	
	public ToWriterReaderConsumer(Writer target, int bufferSize) {
		this.target = target;
		this.bufferSize = bufferSize;
	}
	
	public void consumeReader(Reader reader)  throws ExternalProgramException {
		CharBuffer buffer = java.nio.CharBuffer.allocate(bufferSize);
		BufferedWriter writer = new BufferedWriter(target, bufferSize); // buffered writer needed in order to be able to flush stream ! (?)
		try {
			int nbRead = reader.read(buffer); // returns -1 if end of stream
			while(nbRead >= 0) { // while not end of stream
				if (nbRead > 0) {
					buffer.flip();
					writer.write(buffer.array(), 0, buffer.limit());
					writer.flush();
					buffer.clear();
				}
				nbRead = reader.read(buffer);
			}
			writer.flush();
			writer.close();
			target.close();
			reader.close();
		} catch(IOException e) {
			throw new ExternalProgramException("I/O error in ToWriterReaderConsumer.", e);
		}
	}

	public Writer getTarget() {
		return target;
	}

	public void setTarget(Writer target) {
		this.target = target;
	}
	
	
}
