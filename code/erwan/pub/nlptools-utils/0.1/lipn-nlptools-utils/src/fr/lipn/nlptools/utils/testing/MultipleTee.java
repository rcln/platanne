package fr.lipn.nlptools.utils.testing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import fr.lipn.nlptools.utils.externprog.BroadcastReaderConsumer;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ExternalProgram.ReaderConsumer;

/**
 * This program implements the classical "tee" utility: reads stdin and writes it both
 * to stdout and to a file. For testing purpose it can writes to multiple files at the same
 * time.
 * random sizes  are used for buffer to check that any size works )
 * Notice that it has no special interest but testing class {@link BroadcastReaderConsumer}
 * 
 * @author moreau
 *
 */
public class MultipleTee {
	
	public static final int MAX_BUFFER_SIZE = 4096;
	
	public static void main(String [] args) {
		if (args.length == 0) {
			System.out.println("Syntax : MultipleTee <file1> [ <file2> ... ]");
			System.out.println(" writes stdin content to stdout and to each file given as parameter.");
			System.exit(1);
		}
		// init all ReaderConsumer s
		ReaderConsumer[] outs = new ReaderConsumer[args.length +1];
		outs[0] = new  StdoutReaderConsumer();
		for (int i=0; i< args.length; i++) {
			try {
			outs[i+1] = new FileReaderConsumer(args[i]);
			} catch (IOException e) {
				System.err.println("Can not write to "+args[i]+", aborting.");
				e.printStackTrace();
				System.exit(2);
			}
		}
		BroadcastReaderConsumer broadcaster = new BroadcastReaderConsumer(outs, (int) (Math.random()*MAX_BUFFER_SIZE +1));

		// go
		try {
			broadcaster.consumeReader(new InputStreamReader(System.in));
		} catch (ExternalProgramException e) {
			System.err.println("An error happened:");
			e.printStackTrace();
			System.exit(5);
		}
		
		
	}
	
	public static class FileReaderConsumer implements ReaderConsumer {
		
		FileWriter f;
		
		public FileReaderConsumer(String filename) throws IOException {
			f= new FileWriter(filename);
		}
		
		public void consumeReader(Reader r) throws ExternalProgramException {
			char[] buffer = new char[(int) (Math.random()*MAX_BUFFER_SIZE+1)];
			try {
				int n = r.read(buffer, 0, (int) (Math.random()*buffer.length));
				while (n != -1) {
					f.write(buffer, 0, n);
					n = r.read(buffer, 0, (int) (Math.random()*buffer.length));
				}
				r.close();
				f.close();
			} catch (IOException e) {
				throw new ExternalProgramException("I/O error caught", e);
			}
		}
		
	}
	
	public static class StdoutReaderConsumer implements ReaderConsumer {
		
		public void consumeReader(Reader r) throws ExternalProgramException {
			char[] buffer = new char[(int) (Math.random()*MAX_BUFFER_SIZE+1)];
			try {
				int n = r.read(buffer, 0, (int) (Math.random()*buffer.length));
				while (n != -1) {
					for (int i=0;i<n;i++) {
						System.out.print(buffer[i]);
					}
					n = r.read(buffer, 0, (int) (Math.random()*buffer.length));
				}
				r.close();
			} catch (IOException e) {
				throw new ExternalProgramException("I/O error caught", e);
			}
		}		
	}

}
