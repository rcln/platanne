package fr.lipn.nlptools.utils.externprog;

import java.io.Writer;


/**
 * The thread controlling stdin steam calls {@link WriterFeeder#feedWriter(Writer)} after initializing
 * the text encoder (the <code>Writer</code> object) connected to this stream.
 * If the process expects some input, the WriterFeeder MUST feed its <code>Writer</code> object, otherwise
 * th process will wait forever for its input stream. 
 * 
 * @author moreau
 *
 */
public interface WriterFeeder {
	public void feedWriter(Writer writer) throws ExternalProgramException;
}

