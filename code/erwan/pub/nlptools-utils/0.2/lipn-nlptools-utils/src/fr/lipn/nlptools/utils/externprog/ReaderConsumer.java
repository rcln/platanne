package fr.lipn.nlptools.utils.externprog;

import java.io.Reader;


/**
 * The threads controlling stdout and stderr streams call {@link ReaderConsumer#consumeReader(Reader)}
 * after initalizing the text decoder (the <code>Reader</code> object) connected to the stream.
 * A ReaderConsumer object MUST consume its <code>Reader</code> data (or raise an exception), otherwise
 * the process may wait forever for its output to be read.
 * 
 * @author moreau
 *
 */
public interface ReaderConsumer {
	public void consumeReader(Reader reader) throws ExternalProgramException;
}
