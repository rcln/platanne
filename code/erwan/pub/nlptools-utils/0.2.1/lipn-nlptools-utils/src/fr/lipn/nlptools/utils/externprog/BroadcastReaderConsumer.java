package fr.lipn.nlptools.utils.externprog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;


/**
 * 
 * This class is a {@link ReaderConsumer} multiplier: it is a ReaderConsumer which sends the data it reads to several other ReaderConsumer objects.
 * Since a ReaderConsumer is an object which reads the data on a Reader object, it can be used to send textual data read from a stream to multiple
 * receivers. The classical example of such a behaviour is the standard "tee" program, which reads data on stdin and writes it both to stdout and
 * to a file.<br/>
 * It is intended to make an {@link ExternalProgram} output available to several components (e.g. some parser object and a file writer), but can
 * easily be used in a different context. <br/>
 * The main interest in such an object is the fact that it uses concurrency to avoid storing the whole data in memory/file: the source reader data
 * is bufferised and all child readers threads have to read the whole buffer before loading new data from the source reader.   
 * 
 * @author moreau
 *
 */
/*
 * Note for developers: maybe this class could be more efficient using a read/write lock instead of standard synchronization mechanism:
 * currently any access to the buffer is synchronized, even in the case of readers (only one reader can access the buffer at a time).
 * Nevertheless imho it is very unlikely that this class is used with a lot of readers, so it's not sure that read/write locks approach would
 * be really more efficient. See Java documentation about concurrency. 
 * 
 */
public class BroadcastReaderConsumer implements ReaderConsumer {

	public final static int DEFAULT_BUFFER_SIZE = 2048;
	int bufferSize;
	char[] buffer;
	int currentBufferLimit = 0;
	int alreadyRead[];
	Thread mainThread;
	Thread[] threads;
	Logger logger;
	
	Exception firstException= null;
	
	/**
	 * 
	 * Main constructor, initializes the threads.
	 * 
	 * @param receiverReaderConsumers the set of {@link ReaderConsumer} objects to which data will be sent.
	 * @param bufferSize buffer size: it is an important parameter because all child readers must "consume" it before it is re-filled.
	 * @param log a logger object, may be null (no logging).
	 */
	public BroadcastReaderConsumer(ReaderConsumer[] receiverReaderConsumers, int bufferSize, Logger log) {
		this.bufferSize = bufferSize;
		buffer = new char[bufferSize];
		alreadyRead = new int[receiverReaderConsumers.length];
		Arrays.fill(alreadyRead, 0);
		threads = new Thread[receiverReaderConsumers.length];
		for (int i=0; i< receiverReaderConsumers.length; i++) {   // init readers
			threads[i] = new Thread(new RunnableReaderConsumer(i, receiverReaderConsumers[i], new ConcurrentReader(i)));
		}
		mainThread = Thread.currentThread();
	}

	public BroadcastReaderConsumer(ReaderConsumer[] receiverReaderConsumers, int bufferSize) {
		this(receiverReaderConsumers, bufferSize, null);
	}

	public BroadcastReaderConsumer(ReaderConsumer[] receiverReaderConsumers, Logger log) {
		this(receiverReaderConsumers, DEFAULT_BUFFER_SIZE, log);
	}

	
	public BroadcastReaderConsumer(ReaderConsumer[] receiverReaderConsumers) {
		this(receiverReaderConsumers, DEFAULT_BUFFER_SIZE);
	}
	
	
	/**
	 * Reads the buffer content (called by the readers)
	 * 
	 * @param readerId calling reader id
	 * @param dest calling reader buffer
	 * @param offset
	 * @param len
	 * @return number of chars read, or -1 if end of stream
	 * @throws ExternalProgramException if an interruption is received by the reader
	 */
	protected int readRequest(int readerId, char[] dest, int offset, int len) throws ExternalProgramException {
		synchronized (buffer) {
			controlReadReaquestAccess(readerId);
			log(Level.FINE, "Request for reader "+readerId+" accepted.");
			if (currentBufferLimit == -1) { // end of source
				log(Level.FINE, "Nothing to read anymore");
				return -1;
			} else {
				int nbToRead = Math.min(len -offset, currentBufferLimit - alreadyRead[readerId]);
				log(Level.FINEST, "Details: reader size/offset/len="+dest.length+"/"+offset+"/"+len+", current common buffer limit/length="+buffer.length+"/"+currentBufferLimit+", reader has already read "+alreadyRead[readerId]+" chars in buffer -> will read "+nbToRead+" chars this time.");
				System.arraycopy(buffer, alreadyRead[readerId], dest, offset, nbToRead);
				alreadyRead[readerId] += nbToRead;
				log(Level.FINE, "Request completed for reader "+readerId+".");
				if (alreadyRead[readerId] >= currentBufferLimit) { // actually it is always <=
					buffer.notifyAll();
				} 
				return nbToRead;
			}
		}
	}


	protected int readRequest(int readerId) throws ExternalProgramException {
		synchronized (buffer) {
			controlReadReaquestAccess(readerId);
			log(Level.FINE, "Request for reader "+readerId+" accepted.");
			if (currentBufferLimit == -1) { // end of source
				return -1;
			} else {
				int c = buffer[alreadyRead[readerId]];
				alreadyRead[readerId] ++;
				if (alreadyRead[readerId] >= currentBufferLimit) { // actually it is always <=
					buffer.notifyAll();
				} 
				log(Level.FINE, "Request completed for reader "+readerId+".");
				return c;
			}
		}
	}

	private void controlReadReaquestAccess(int readerId) throws ExternalProgramException  {
		log(Level.FINE, "Reader "+readerId+": reading request waiting... ");
		while ((currentBufferLimit != -1) && (alreadyRead[readerId] >= currentBufferLimit)) { // actually it is always <=
			try {
				buffer.wait();
			} catch (InterruptedException e) {
				throw new ExternalProgramException("Interruption while waiting for data during a reading request", e);
			}
		}
	}

	
	
	/**
	 * When the buffer is ready to be written (all readers have read its content), 
	 * reads the source reader and writes the data into the buffer
	 * @param reader the source reader
	 * @throws ExternalProgramException if the thread is interrupted (by a reader) or in case of I/O error
	 */
	protected void fillBuffer(Reader reader) throws ExternalProgramException {
		synchronized (buffer) {
			log(Level.FINE, "Writer (main process) waiting to write...");
			while (!allReadersDone()) {
				try {
					buffer.wait();
				} catch (InterruptedException e) {
					throw new ExternalProgramException("Main thread has been interrupted while waiting readers", e);
				}
			}
			log(Level.FINE, "Writer (main process) starting to write...");
			try {
				int nbRead = reader.read(buffer, 0, bufferSize);  
				currentBufferLimit = nbRead;  //including case end of stream (-1)
				for (int i=0; i<alreadyRead.length; i++) {
					alreadyRead[i] = 0;
				}
				log(Level.FINE, "Writing operation terminated"+((nbRead==-1)?", end of source reader.":"."));
				buffer.notifyAll();
			} catch (IOException e) {
				throw new ExternalProgramException("I/O error while reading the source reader.", e);
			}
		}
	}

	
	/**
	 * Starts child readers then fills the buffer each time needed until end of source reader
	 * @param Reader the source reader object.
	 */
	public void consumeReader(Reader reader) throws ExternalProgramException {

		log(Level.INFO, "Main process (writer) starting");
		for (int i=0; i< threads.length; i++) {  // starting readers
			threads[i].start();
		}
		
		while (currentBufferLimit != -1) {  // main process : fill buffer each time it is ready ( = all readers have read it totally)
			try {
				fillBuffer(reader);
			} catch (ExternalProgramException e) {
				currentBufferLimit = -1; // to exit loop
				receiveInterrupt(-1, new ExternalProgramException ("Error received by main thread while trying to fill buffer", e));
			}
		}

		try {  // closing source reader
			reader.close();
		} catch (IOException e) {
			receiveInterrupt(-1, new ExternalProgramException ("I/O error when closing the source reader", e));
		}
		
		for (int i=0; i<alreadyRead.length; i++) {  // waiting for all readers to terminate (both cases: normal end or interruption)
			while (threads[i].isAlive()) {
				try {
					log(Level.FINE, "Main process: waiting for thread "+i+" to terminate");
					threads[i].join();
				} catch (InterruptedException e) {
					receiveInterrupt(-1, new ExternalProgramException("Main thread has been interrupted while waiting for readers to terminate", e));
				}
			}
		}
		log(Level.INFO, "Main process (writer) terminating.");
		
		if (firstException != null) {  // an interruption has happened
			throw new ExternalProgramException("Main process: an error occured", firstException);
		}
	}
	

	/**
	 * 
	 * @return true if all readers have totally read the current buffer content 
	 */
	protected boolean allReadersDone() {
		for (int i=0; i<alreadyRead.length; i++) {
			if (alreadyRead[i] < currentBufferLimit) {
				return false;
			}
		}
		return true;
	}
		

	/**
	 * Called by a reader receiving an exception: if it is the first one received, the exception is stored and all threads will be interrupted, including main thread
	 * Notice that the main (writer) thread has id -1 when calling this method.
	 * @param id the reader id
	 * @param e the exception received
	 */
	protected synchronized void receiveInterrupt(int id, Exception e) {
		log(Level.FINE, "Thread "+id+" entering interrupting process because of exception:"+e.getMessage());
		if (firstException == null) {  // 
			log(Level.FINE, "Thread "+id+" is the first cause of exception. Interrupting all threads.");
			firstException = e;
			for (int i = 0; i < threads.length; i++) {  // interrupting all threads
				threads[i].interrupt();
			}
			mainThread.interrupt();
		}
	}
	
	/**
	 * If Exception is not null, its stack trace is added to the message
	 * @param level
	 * @param message
	 * @param e
	 */
	private void log(Level level, String message, Exception e) {
		if (logger != null) {
			if (e!=null) {
				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				message += writer.toString();
			}
			logger.log(level, message);			
		}
	}
	
	private void log(Level level, String message) {
		log(level, message, null);
	}



	/**
	 * class for child readers threads (only the concurrency stuff is done here)
	 * 
	 * @author moreau
	 *
	 */
	protected class RunnableReaderConsumer implements Runnable {
		
		int id;
		ReaderConsumer consumer;
		Reader reader;
		
		public RunnableReaderConsumer(int myOwnId, ReaderConsumer consumer, Reader reader) {
			this.id = myOwnId;
			this.consumer = consumer;
			this.reader = reader;
		}
		
		public void run() {
			log(Level.INFO, "Reader thread "+id+" starting.");
			try {
				consumer.consumeReader(reader);
			} catch (Exception e) {
				receiveInterrupt(id, new ExternalProgramException("Error in reader "+id+", stopping thread", e));
			}
			log(Level.INFO, "Reader thread "+id+" terminating.");
		}
		
		
	}
	
	
	/**
	 * 
	 * class for child readers threads (only the Reader I/O stuff is done here)
	 * 
	 * @author moreau
	 *
	 */
	public class ConcurrentReader extends Reader {
		
		int id;
		boolean closed = false;
		
		public ConcurrentReader(int id) {
			this.id = id;
		}
		
		public void close() throws IOException {
			closed = true;
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public int read() throws IOException {
			if (!closed) {
				try {
					return readRequest(id);
				} catch (ExternalProgramException e) {
					throw new IOException("Reader "+id+": error during reading request", e);
				}
			} else {
				throw new IOException("Reader has been closed");
			}
		}

		@Override
		public int read(char[] buff, int offset, int len) throws IOException {
			if (!closed) {
				try {
					return readRequest(id, buff, offset, len);
				} catch (ExternalProgramException e) {
					throw new IOException("Reader "+id+": error during reading request", e);
				}
			} else {
				throw new IOException("Reader has been closed");
			}
		}


		@Override
		public boolean ready() throws IOException {
			return (!closed && (alreadyRead[id] < currentBufferLimit));
		}

		
	}
	
}
