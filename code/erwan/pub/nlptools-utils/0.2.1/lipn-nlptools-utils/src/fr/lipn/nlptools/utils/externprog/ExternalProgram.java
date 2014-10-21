package fr.lipn.nlptools.utils.externprog;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;


/**
 * Used to launch an external application in a rather safe, efficient and convenient way: streams are
 * processed in different threads (which use buffering through the Channels methods - unless I'm wrong !) (see Java API about <code>ProcessBuilder</code> and <code>Process</code>).
 * The process input/output/error streams must be valid text streams, since they are passed as the content of <code>Writer</code>
 * or <code>Reader</code> objects. The behaviour of these objects is controlled using objects implementing interfaces {@link ReaderConsumer} 
 * and/or {@link WriterFeeder}: by default, there is no stdin stream and a {@link VoidReaderConsumer} (which simply ignores the data) is used for both output streams.
 * The classes {@link StringReaderConsumer} and {@link StringWriterFeeder} provide a simple way to deal with input/output streams, but this class
 * also offers the possibility to read/write data "on the fly" (through Reader and/or Writer objects), thus avoiding loss of time and/or memory.
 * 
 *<br/>
 *
 * The {@link #run(fr.lipn.nlptools.util.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer)}
 *  method waits for the process to terminate before ending. Exceptions are thrown for any error which 
 * does not occur inside the external program process: I/O error with input/output streams, thread failures, time out, etc. 
 *<br/>
 * Among the features available: this class permits to configure the way the input/output streams are transmitted to/from the process, in particular
 * the charset encoding issues; A <code>Logger</code> instance can also be provided, so that the class can write detailed messages about the threads. 
 * 
 * Important recall : launching an external application is always risky and usually not portable !
 *<br/><br/>
 *
 * A <code>Logger</code> object can be set using {@link #setLogger(Logger)} to obtain information about the process. By default there is no log.
 *
 * This class is inspired from <code>ProcessLauncher</code>, written by Fabio Marazzeto and Yann D'Isanto.
 *
 *
 * @author moreau
 *
 */
/*
 * Note for developpers: input/output streams/threads names may seem confusing, since
 * an output becomes an input as soon as it is connected to the input stream that consumes it 
 * (mmh, I tried to make it clear but i'm not sure it is...)
 * Whatever, don't forget that the input of the process goes to an outputstream (as it is written in this class), and 
 * conversely the outputs of the process go to two inputstream (they are read in this class).
 */

public class ExternalProgram {

	static final int DEFAULT_BUFFER_SIZE = 2048;
	static final String DEFAULT_ENCODING = System.getProperty("file.encoding");

	List<String> command;
	WriterFeeder lastStdinFeeder;
	ReaderConsumer lastStdoutConsumer;
	ReaderConsumer lastStderrConsumer;
	int lastExitCode = -1;
	String workingDir;
	long timeOut = 0;
	ProcessBuilder processBuilder;
	ExternalProgramException firstException = null;
	Thread inputFeederThread, outputConsumerThread, errorConsumerThread, processThread;
	Process externalProcess;
	boolean waitForStreamThreads = true;	
	boolean mergeStdoutAndStderr = false;
	Logger logger = null;
	
	static final int STDIN_ID = 0;
	static final int STDOUT_ID = 1;
	static final int STDERR_ID = 2;
	static final int DEFAULT_ID = 3;
	final static String[] STREAM_THREAD_NAME = { "stdin stream feeder thread", "stdout stream consumer thread", "stderr stream consumer thread", "process waiter thread"};
	
	String[] encoding = new String[4];
	CodingErrorAction[] codingErrorAction = new CodingErrorAction[4];
	String[] replacementValue = new String[4];
	
	

	/**
	 * Constructor.  
	 *
	 * @param command command line with arguments. Don't forget that this command line is not interpreted by the shell: that means
	 *                that the command must be correctly tokenized and can not use any shell-level tool (like name expansion etc.). 
	 *                See <code>ProcessBuilder</code> API.
	 * @param timeOut maximum execution time (in ms) before interruption (0L not to interrupt)
	 * @param workingDir Directory where the process will run (null for current directory)
	 * @param encoding charset encoding used to communicate with the process (null to use default encoding). The same encoding will be
	 *                 used for input and output streams.
	 * 
	 */
	public ExternalProgram(List<String> command, long timeOut, String workingDir, String encoding) {
		this.command = command;
		this.timeOut = timeOut;
		this.workingDir = workingDir;
		if (encoding != null) {
			this.encoding[DEFAULT_ID] = encoding;
		}
		processBuilder = new ProcessBuilder(command);
	}


	
	/**
	 * Simple constructor.
	 * @param command command line with arguments. Don't forget that this command line is not interpreted by the shell: that means
	 *                that the command must be correctly tokenized and can not use any shell-level tool (like name expansion etc.)
	 */
	public ExternalProgram(List<String> command) {
		this(command, 0L, null, null);
	}
	
	
	/**
	 * Wrapper for {@link java.lang.ProcessBuilder#environment()}.
	 * 
     * Returns a string map view of the underlying process builder's environment. Whenever a process builder is created, 
     * the environment is initialized to a copy of the current process environment. 
     * Subprocesses subsequently started by this object's start() method will use this map as their environment.
     * 
     *  The returned object may be modified using ordinary Map operations. These modifications will be visible to subprocesses 
     *  started via the start()  method. Two ProcessBuilder instances always contain independent process environments, so 
     *  changes to the returned map will never be reflected in any other ProcessBuilder instance or the values returned
     *   by System.getenv. 
     *  
   	 * @return This process builder's environment 
	 */
	public Map<String, String> environment() {
		return processBuilder.environment();
	}
	
	
	/**
	 * Starts the process and waits for it to end. Streams are processed using the provided feeder and consumers objets.
	 * 
	 *  Tries to catch any error which is not due to the process itself.
	 * 
	 * @param stdinFeeder    the {@link WriterFeeder} object responsible for writing the stdin stream. Set to null for no stdin stream.
	 * @param stdoutConsumer the {@link ReaderConsumer} object responsible for reading the stdout stream. Set to null to ignore stdout stream 
	 *                       (a {@link VoidReaderConsumer} object will be used to consume the stream). 
	 * @param stderrConsumer the {@link ReaderConsumer} object responsible for reading the stderr stream. Set to null to ignore stderr stream 
	 *                       (a {@link VoidReaderConsumer} object will be used to consume the stream).
	 *  
	 * @return the exit code of the process
	 * @throws ExternalProgramException for any error which is not due to the process.
	 */
	public int run(WriterFeeder stdinFeeder, ReaderConsumer stdoutConsumer, ReaderConsumer stderrConsumer) throws ExternalProgramException {
		
		log(Level.INFO, "Starting an ExternalProgram process: "+command.toString()+" (timeOut: "+timeOut+"; dir:"+workingDir+"; default charset enc: "+encoding[DEFAULT_ID]+")");
		lastStdinFeeder = stdinFeeder;
		// default: to ensure that output streams will be consumed, VoidReaderConsumer are used (they simply read the data and ignore it)
		lastStdoutConsumer = (stdoutConsumer==null)?new VoidReaderConsumer():stdoutConsumer;
		lastStderrConsumer = (stderrConsumer==null)?new VoidReaderConsumer():stderrConsumer;
		if (workingDir != null) {
			processBuilder.directory(new File(workingDir));
		}
		processBuilder.redirectErrorStream(mergeStdoutAndStderr);
		try {
			externalProcess = processBuilder.start();
		} catch (Exception e) {
			throw new ExternalProgramException("Error starting process.", e);
		}
		UncaughtExceptionHandler exceptionHandler = new ExternalProgramThreadExceptionHandler();
		
		outputConsumerThread = new Thread(new InputStreamConsumer(externalProcess.getInputStream(), lastStdoutConsumer, this, STDOUT_ID), STREAM_THREAD_NAME[STDOUT_ID]);
		outputConsumerThread.setUncaughtExceptionHandler(exceptionHandler);
		outputConsumerThread.start();
		errorConsumerThread = new Thread(new InputStreamConsumer(externalProcess.getErrorStream(), lastStderrConsumer, this, STDERR_ID), STREAM_THREAD_NAME[STDERR_ID]);
		errorConsumerThread.setUncaughtExceptionHandler(exceptionHandler);
		errorConsumerThread.start();
		int status = -1;
		processThread = new Thread(new WaiterThread(externalProcess, this, DEFAULT_ID), STREAM_THREAD_NAME[DEFAULT_ID]);
		processThread.setUncaughtExceptionHandler(exceptionHandler);
		processThread.start();
		if (lastStdinFeeder != null) {
			inputFeederThread = new Thread(new OutputStreamFeeder(externalProcess.getOutputStream(), lastStdinFeeder, this, STDIN_ID), STREAM_THREAD_NAME[STDIN_ID]);
			inputFeederThread.setUncaughtExceptionHandler(exceptionHandler);
			inputFeederThread.start();
		}
		try {
			processThread.join(timeOut); //waits for end of process or time out (0=no time limit)
		} catch (InterruptedException ie) {
			informInterrupt(DEFAULT_ID, ie);
		}
		// possible cases: 
		// 1) interrupted for unknown reason => firstException has been set (possibly process has not terminated)
		// 2) timed out => no exception at this step but process has not terminated
		// 3) normal exit.
		try {
			status = externalProcess.exitValue();
		} catch (IllegalThreadStateException itse) {  
			// process had not yet terminated -> a priori possible in only 2 cases: 
			// - an exception occured causing an interruption of the waiterThread (in this case firstException should be set) 
			// - or interruption is due to time out (firstException is not yet set)
			if (firstException == null) {
				log(Level.WARNING, "TIME OUT !");
			}
			log(Level.WARNING, "Interruption ! Killing the process.");
			externalProcess.destroy();  // kill process
			informInterrupt(DEFAULT_ID, new TimeOutException("Time out, main process interrupted."));
		}
		if (waitForStreamThreads) {  // if any thread has already been interrupted, they should all have been interrupted (thus there will be no infinite waiting)
			log(Level.FINE, "Waiting for stream threads to end.");
			waitForThread(inputFeederThread, STDIN_ID);
			waitForThread(errorConsumerThread, STDERR_ID);
			waitForThread(outputConsumerThread, STDOUT_ID);
		} else {
			log(Level.INFO, "Stopping remaining threads !");
			interruptAllThreads();
		}
		// at this step either all threads have terminated or they all have been interrupted;
		// nevertheless, in case of interruption it is necessary to wait that the thread(s) react before terminating:
		// otherwise the firstException could stay null and the error is not caught 
		// (and anyway it can not take a long time, since the thread(s) can not try to terminate normally).
		waitForThread(inputFeederThread, STDIN_ID);
		waitForThread(errorConsumerThread, STDERR_ID);
		waitForThread(outputConsumerThread, STDOUT_ID);
		if (firstException != null) {
			log(Level.SEVERE, "An exception has been raised:", firstException);
			throw firstException;
		}
		lastExitCode = status;
		return status;
	}


	/**
	 * Default run() method: no stdin stream is provided, stdout and stderr streams are ignored using {@link VoidReaderConsumer} objets. 
	 * 
	 * @return process exit code
	 * @throws ExternalProgramException for any error which is not due to the process itself.
	 */
	public int run() throws ExternalProgramException {
		return run(null, null, null);
	}
	
	
	protected void waitForThread(Thread t, int id) {
		if ((t != null) && t.isAlive()) {
			log(Level.FINE, STREAM_THREAD_NAME[id]+" is alive, waiting...");
			try {
				t.join();
			} catch (InterruptedException e) {
				informInterrupt(id, e);
			}
		}
	}

	
	protected void interruptAllThreads() {
		log(Level.INFO, "Interrupting all threads !");
		log(Level.WARNING, "Interrupting the external process...");
		externalProcess.destroy();  // kill process
		if ((inputFeederThread != null) && inputFeederThread.isAlive()) {
			log(Level.FINE, "input feeder thread was alive.");
			inputFeederThread.interrupt();
		}
		if (outputConsumerThread.isAlive()) {
			log(Level.FINE, "output consumer thread was alive.");
			outputConsumerThread.interrupt();
		}
		if (errorConsumerThread.isAlive()) {
			log(Level.FINE, "error consumer thread was alive.");
			errorConsumerThread.interrupt();
		}
		if (processThread.isAlive()) {
			log(Level.FINE, "main process waiter thread was alive.");
			processThread.interrupt();
		}
	}

	protected synchronized void informInterrupt(String id, Exception e) {
		log(Level.WARNING, "Receiving an exception sent by "+id+" (only the first one will be thrown): ", e);
		if (firstException == null) {
			firstException = new ExternalProgramException("Error sent by "+id+" (only first error is reported).", e);
			interruptAllThreads();
		}
	}
	
	protected synchronized void informInterrupt(int id, Exception e) {
		informInterrupt(STREAM_THREAD_NAME[id], e);
	}
	

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}


	/**
	 * if true, only stdout is used.
	 */
	public boolean getMergeStdoutAndStderr() {
		return mergeStdoutAndStderr;
	}

	/**
	 * if true, only stdout is used.
	 * @param mergeStdoutAndStderr
	 */
	public void setMergeStdoutAndStderr(boolean mergeStdoutAndStderr) {
		this.mergeStdoutAndStderr = mergeStdoutAndStderr;
	}



	/**
	 * Globally sets the charset encoding to use for std streams.
	 * Different streams can also have different values: use {@link #setStdinEncoding(String)}, {@link #setStdoutEncoding(String)}, {@link #setStderrEncoding(String)}
	 * @param encoding new default encoding (set to null to use system default)
	 */
	public void setDefaultEncoding(String encoding) {
		this.encoding[DEFAULT_ID] = encoding;
	}
	

	public CodingErrorAction getDefaultCodingErrorAction() {
		return this.codingErrorAction[DEFAULT_ID];
	}



	/**
	 * Globally sets the action to do in case of charset coding error (see Java API for more details). 
	 * Different streams can also have different values: use {@link #setStdinCodingErrorAction(CodingErrorAction)}, {@link #setStdoutCodingErrorAction(CodingErrorAction)}, {@link #setStderrCodingErrorAction(CodingErrorAction)}
	 */
	public void setDefaultCodingErrorAction(CodingErrorAction defaultCodingErrorAction) {
		this.codingErrorAction[DEFAULT_ID] = defaultCodingErrorAction;
	}



	public String getDefaultEncoding() {
		return this.encoding[DEFAULT_ID];
	}


	public CodingErrorAction getStderrCodingErrorAction() {
		return this.codingErrorAction[STDERR_ID];
	}
	
	public String getDefaultReplacementValue() {
		return this.replacementValue[DEFAULT_ID];
	}
	
	public void setDefaultReplacementValue(String replacementValue) {
		this.replacementValue[DEFAULT_ID] = replacementValue;
	}


	/**
	 * Locally sets the action to do in case of charset coding error (see Java API for more details).
	 */
	public void setStderrCodingErrorAction(CodingErrorAction stderrCodingErrorAction) {
		this.codingErrorAction[STDERR_ID] = stderrCodingErrorAction;
	}



	public String getStderrEncoding() {
		return this.encoding[STDERR_ID];
	}


	/**
	 * Locally sets the charset encoding
	 */
	public void setStderrEncoding(String stderrEncoding) {
		this.encoding[STDERR_ID] = stderrEncoding;
	}

	public CodingErrorAction getStdinCodingErrorAction() {
		return this.codingErrorAction[STDIN_ID];
	}



	/**
	 * Locally sets the action to do in case of charset coding error (see Java API for more details).
	 */
	public void setStdinCodingErrorAction(CodingErrorAction stdinCodingErrorAction) {
		this.codingErrorAction[STDIN_ID] = stdinCodingErrorAction;
	}



	public String getStdinEncoding() {
		return this.encoding[STDIN_ID];
	}



	/**
	 * Locally sets the charset encoding
	 */
	public void setStdinEncoding(String stdinEncoding) {
		this.encoding[STDIN_ID] = stdinEncoding;
	}



	public CodingErrorAction getStdoutCodingErrorAction() {
		return this.codingErrorAction[STDOUT_ID];
	}



	/**
	 * Locally sets the action to do in case of charset coding error (see Java API for more details).
	 */
	public void setStdoutCodingErrorAction(CodingErrorAction stdoutCodingErrorAction) {
		this.codingErrorAction[STDOUT_ID] = stdoutCodingErrorAction;
	}



	public String getStdoutEncoding() {
		return this.encoding[STDOUT_ID];
	}



	/**
	 * Locally sets the charset encoding
	 */
	public void setStdoutEncoding(String stdoutEncoding) {
		this.encoding[STDOUT_ID] = stdoutEncoding;
	}
	
	

	public String getStderrReplacementValue() {
		return replacementValue[STDERR_ID];
	}



	/**
	 * Locally sets the replacement value to use in case of charset coding error (see Java API for more details)
	 * This value is used only if the corresponding CodingErrorAction is set to REPLACE.
	 */
	public void setStderrReplacementValue(String stderrReplacementValue) {
		this.replacementValue[STDERR_ID] = stderrReplacementValue;
	}



	public String getStdinReplacementValue() {
		return replacementValue[STDIN_ID];
	}



	/**
	 * Locally sets the replacement value to use in case of charset coding error (see Java API for more details)
	 * This value is used only if the corresponding CodingErrorAction is set to REPLACE.
	 */
	public void setStdinReplacementValue(String stdinReplacementValue) {
		this.replacementValue[STDIN_ID] = stdinReplacementValue;
	}



	public String getStdoutReplacementValue() {
		return replacementValue[STDOUT_ID];
	}



	/**
	 * Locally sets the replacement value to use in case of charset coding error (see Java API for more details)
	 * This value is used only if the corresponding CodingErrorAction is set to REPLACE.
	 */
	public void setStdoutReplacementValue(String stdoutReplacementValue) {
		this.replacementValue[STDOUT_ID] = stdoutReplacementValue;
	}
	


	public List<String> getCommand() {
		return command;
	}


	public void setCommand(List<String> command) {
		this.command = command;
	}

	
	/**
	 * @return the last ReaderConsumer used to process stderr stream
	 */
	public ReaderConsumer getLastStderrConsumer() {
		return lastStderrConsumer;
	}


	/**
	 * @return the last WriterFeeder used to process stdin stream
	 */
	public WriterFeeder getLastStdinFeeder() {
		return lastStdinFeeder;
	}



	/**
	 * @return the last ReaderConsumer used to process stdout stream
	 */
	public ReaderConsumer getLastStdoutConsumer() {
		return lastStdoutConsumer;
	}
	
	

	public Logger getLogger() {
		return logger;
	}


	public void setLogger(Logger logger) {
		this.logger = logger;
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
	 * By default, after the main process has terminated (without error) the {@link #run(fr.lipn.nlptools.util.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer)} method
	 * will wait for the stream controller threads to end (they can need more time to consume the output).
	 * The flag <code>waitForStreamThreads</code> controls this behaviour. 
	 * Caution: if set to false, stream threads can be interrupted before they finish their job, and an InterruptedException will be thrown.  
	 * @return this flag value
	 */
	public boolean getWaitForStreamThreads() {
		return waitForStreamThreads;
	}



	/**
	 * By default, after the main process has terminated (without error) the {@link #run(fr.lipn.nlptools.util.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer)} method
	 * will wait for the stream controller threads to end (they can need more time to consume the output).
	 * The flag <code>waitForStreamThreads</code> controls this behaviour. 
	 * Caution: if set to false, stream threads can be interrupted before they finish their job, and an InterruptedException will be thrown.  
	 * @param waitForStreamThreads this flag value
	 */
	public void setWaitForStreamThreads(boolean waitForStreamThreads) {
		this.waitForStreamThreads = waitForStreamThreads;
	}

	

	public int getLastExitCode() {
		return lastExitCode;
	}

	
	
	// the threads that are responsible for stdout and stderr
	protected class InputStreamConsumer implements Runnable {

		InputStream stream;
		ReaderConsumer readerConsumer;
		ExternalProgram caller;
		int id;
		
		public InputStreamConsumer(InputStream stream, ReaderConsumer readerConsumer, ExternalProgram caller, int id) {
			this.stream = stream;
			this.readerConsumer = readerConsumer;
			this.caller = caller;
			this.id = id;
		}
		
		public void run() {
			//ReadableByteChannel channel = java.nio.channels.Channels.newChannel(stream);
			log(Level.FINE, "Starting "+STREAM_THREAD_NAME[id]);
			Reader decoder = Channels.newReader(Channels.newChannel(stream), initCharsetDecoder(id), -1);
			try {
				readerConsumer.consumeReader(decoder);
				log(Level.FINE, "Stream has been read normally in "+STREAM_THREAD_NAME[id]);
			} catch(Exception e) {
				log(Level.WARNING, "Exception caught and transmitted in "+STREAM_THREAD_NAME[id]+".");
				caller.informInterrupt(id, e);
			}
			log(Level.FINE, "Terminating "+STREAM_THREAD_NAME[id]);
		}
		
	}
	
	
	// the thread responsible for stdin stream
	protected class OutputStreamFeeder implements Runnable {

		OutputStream stream;
		WriterFeeder writerFeeder;
		ExternalProgram caller;
		int id;
		
		public OutputStreamFeeder(OutputStream stream, WriterFeeder writerFeeder, ExternalProgram caller, int id) {
			this.stream = stream;
			this.writerFeeder = writerFeeder;
			this.caller = caller;
			this.id = id;
		}
		
		public void run() {
			log(Level.FINE, "Starting "+STREAM_THREAD_NAME[id]);
			try {
				Writer encoder = Channels.newWriter(Channels.newChannel(stream), initCharsetEncoder(id), -1);
				writerFeeder.feedWriter(encoder);
				log(Level.FINE, "Stream has been written normally in "+STREAM_THREAD_NAME[id]);
			} catch(Exception e) {
				log(Level.WARNING, "Exception caught and transmitted in "+STREAM_THREAD_NAME[id]+".");
				caller.informInterrupt(id, e);
			}
			log(Level.FINE, "Terminating "+STREAM_THREAD_NAME[id]);
		}
		
	}

	
	// the thread that waits for the main process: will be interrupted in case of time out, causing
	// the end of the process
	protected class WaiterThread implements Runnable {
		
		Process processToWait;
		ExternalProgram caller;
		int id;
		
		public WaiterThread(Process p, ExternalProgram c, int id) {
			processToWait = p;
			caller = c;
			this.id = id;
		}
		
		public void run() {
			log(Level.FINE, "Starting "+STREAM_THREAD_NAME[id]);
			try {
				processToWait.waitFor();
			} catch (Exception e) {  // this thread has been interrupted, possibly because of time out.
				log(Level.WARNING, "Exception caught and transmitted in "+STREAM_THREAD_NAME[id]+" (possible time out).");
				caller.informInterrupt(id, e);
			}
			log(Level.FINE, "Terminating "+STREAM_THREAD_NAME[id]);
		}
		
	}

	protected class ExternalProgramThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

		public void uncaughtException(Thread t, Throwable e) {
			informInterrupt(t.getName(), new ExternalProgramException("Uncaught exception thrown in thread "+t.getName(), e));
		}

	}

	/**
	 * Default ReaderConsumer that simply reads the data and does nothing with it.
	 * 
	 * @author moreau
	 *
	 */
	public class VoidReaderConsumer implements ReaderConsumer {
		int bufferSize = DEFAULT_BUFFER_SIZE;

		public VoidReaderConsumer() {
		}
		
		public VoidReaderConsumer(int bufferSize) {
			this.bufferSize = bufferSize;
		}
		
		public void consumeReader(Reader reader)  throws ExternalProgramException {
			CharBuffer buffer = java.nio.CharBuffer.allocate(bufferSize);
			try {
				int nbRead = reader.read(buffer); // returns -1 if end of stream
				while (nbRead >= 0) { // while not end of stream
					nbRead = reader.read(buffer);
					buffer.clear();
				}
				reader.close();
			} catch (IOException e) {
				throw new ExternalProgramException("I/O error in void reader consumer.", e);
			}
		}		
	}

	private CharsetDecoder initCharsetDecoder(int id) {
		// if specific encoding is defined use it, else if (class) default enc is defined use it, otherwise use system default enc.
		String enc = (encoding[id]!=null)?encoding[id]:((encoding[DEFAULT_ID]!=null)?encoding[DEFAULT_ID]:DEFAULT_ENCODING);
		CharsetDecoder csDecoder = Charset.forName(enc).newDecoder();
		CodingErrorAction action = (codingErrorAction[id] != null)?codingErrorAction[id]:codingErrorAction[DEFAULT_ID];
		String value = (replacementValue[id] != null)?replacementValue[id]:replacementValue[DEFAULT_ID];
		if (value != null) {
			csDecoder.replaceWith(value);
		}
		if (action != null) {
			csDecoder.onMalformedInput(action).onUnmappableCharacter(action);
		}
		return csDecoder;
	}
	private CharsetEncoder initCharsetEncoder(int id) throws ExternalProgramException {
		// if specific encoding is defined use it, else if (class) default enc is defined use it, otherwise use system default enc.
		String enc = (encoding[id]!=null)?encoding[id]:((encoding[DEFAULT_ID]!=null)?encoding[DEFAULT_ID]:DEFAULT_ENCODING);
		CharsetEncoder csEncoder = Charset.forName(enc).newEncoder();
		CodingErrorAction action = (codingErrorAction[id] != null)?codingErrorAction[id]:codingErrorAction[DEFAULT_ID];
		String value = (replacementValue[id] != null)?replacementValue[id]:replacementValue[DEFAULT_ID];
		if (value != null) { 
			try {
				ByteBuffer bytesValue = csEncoder.encode(CharBuffer.wrap(value));
				csEncoder.replaceWith(bytesValue.array());
			} catch (CharacterCodingException e) {
				throw new ExternalProgramException("Error: the specified replacement value '"+value+"' can not be represented using charset encoding '"+enc+"'.");
			}
		}
		if (action != null) {
			csEncoder.onMalformedInput(action).onUnmappableCharacter(action);
		}
		return csEncoder;
		
	}



}
