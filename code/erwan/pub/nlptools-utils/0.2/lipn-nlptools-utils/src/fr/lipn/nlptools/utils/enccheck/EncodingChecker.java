package fr.lipn.nlptools.utils.enccheck;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.HashSet;

public class EncodingChecker {

	public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
		
	/*
	 * 
	 * THAT WAS A FIRST VERSION - NOT SUFFICIANT IN MY OPINION
	 * 
	 * 
	 * Checks whether characters sent in Reader <code>data</code> can be written in 
	 * different charset <code>encodings</code>.
	 * This method does not write anything top stdout, and prints messages to stderr only
	 * if <code>printErrors</code> is set (and errors actually occur).
	 * 
	 * @param data the data to be tested
	 * @param encodings the charset encodings
	 * @param checkAllCharOccurrences if true, all occurrences of an invalid character will
	 *        be tested and reported. Useful only if <code>printError</code> is true.
	 * @param printErrors if true, an error message is printed to stderr for each invalid
	 * 	      character.
	 * @return an array containg the set of invalid character for each encoding, or null
	 *         if no error was found.
	 * @throws IOException
	public static char[][] checkEncoding(BufferedReader data, String[] encodings, boolean checkAllCharOccurrences, boolean printErrors) throws IOException {
		
		int lineNo = 0;
		String line;
		Writer[] encoders = new Writer[encodings.length];
		ArrayList<HashSet<Character>> validChars = new ArrayList<HashSet<Character>>();
		ArrayList<HashSet<Character>> invalidChars = new ArrayList<HashSet<Character>>();
		OutputStream out = new NullOutputStream();
		boolean error = false;

		for (int i=0; i < encodings.length; i++) {
			encoders[i] = Channels.newWriter(Channels.newChannel(out), encodings[i]);
			validChars.add(new HashSet<Character>());
			invalidChars.add(new HashSet<Character>());
		}

		while ((line = data.readLine()) != null) {
			lineNo++;
			//System.out.println("DEBUG line "+lineNo+", "+validChars.get(0).size()+" different valid chars for enc 0");
			for (int i=0; i<line.length(); i++) {
				for (int j=0; j< encoders.length; j++) {
					Character c = line.charAt(i);
					if (!validChars.get(j).contains(c) && (checkAllCharOccurrences || !invalidChars.get(j).contains(c))) {
						try {
							encoders[j].write(c);
							validChars.get(j).add(c);
						} catch (UnmappableCharacterException e) { // other IO exceptions are not caught here
							error = true;
							invalidChars.get(j).add(c);
							if (printErrors) {
								System.err.println(lineNo+","+(i+1)+": "+c+" not valid for "+encodings[j]);
							}
						}
					} else {

					}
				}
			}
		}
		if (error) {
			char [][] charErrors = new char[encodings.length][];
			for (int i=0; i<charErrors.length; i++) {
				int j = 0;
				charErrors[i] = new char[invalidChars.get(i).size()];
				for (Character c : invalidChars.get(i)) {
					charErrors[i][j++] = c;
				}
			}
			return charErrors;
		} else {
			return null;
		}
	}
	
	
	public static char[][] checkEncoding(BufferedReader data, String[] encodings, boolean checkAllCharOccurrences)  throws IOException {
		return checkEncoding(data, encodings, checkAllCharOccurrences, true);
	}
	
	public static char[][] checkEncoding(BufferedReader data, String[] encodings)  throws IOException {
		return checkEncoding(data, encodings, false, true);
	}
	
	public static char[][] checkEncoding(BufferedReader data, String encoding) throws IOException {
		return checkEncoding(data, new String[]{encoding});
	}
		
	public static char[][] checkEncoding(BufferedReader data) throws IOException {
		return checkEncoding(data, DEFAULT_ENCODING);
	}
	
	public static char[][] checkEncoding(String data, String[] encodings) throws IOException {
		return checkEncoding(new BufferedReader(new StringReader(data)), encodings);
	}
	
	public static char[][] checkEncoding(String data, String encoding) throws IOException {
		return checkEncoding(new BufferedReader(new StringReader(data)), encoding);
	}

	public static char[][] checkEncoding(String data) throws IOException {
		return checkEncoding(new BufferedReader(new StringReader(data)));
	}
	*/
	
	
	
	/**
	 * Checks whether 1) the given input stream can be interpreted using the given input 
	 * encoding and 2) can then be written using the given output encoding.
	 * Thus there are two cases where errors can be encoutered: at reading, when 
	 * the raw data does not correspond to a valid character in this encoding ; and at
	 * writing, when a character can not be converted in the output encoding.
	 * 
	 *
	 * @param input the input stream
	 * @param inputEnc the input charset encoding name
	 * @param outputEnc the output charset encoding name
	 * @param printErrors set to true if errors should be printed to stdout
	 * @param stopOnError set to true to stop as soon as an error happens.
	 * @return null if no error happens, and an array containing the invalid characters
	 * found when writing. This array may be empty in case errors happened only when reading.
	 * @throws IOException when it is NOT a character coding error.
	 */
	public static char[] convertCheckEncoding(InputStream input, String inputEnc, String outputEnc, boolean printErrors, boolean stopOnError) throws IOException {

		HashSet<Character> invalidChars = new HashSet<Character>();
		Reader in = new InputStreamReader(input, inputEnc);
		Writer out = Channels.newWriter(Channels.newChannel(new NullOutputStream()), outputEnc);
		
		boolean error = false;
		boolean lastWasCR = false;
		int lineNo = 1;
		int colNo = 1;
		int c = -1;
		String currLine = "";
		do {
			try {
				c = in.read();
				if (c != -1) {
//					System.err.println("Reading char '"+(char) c+"' ("+c+").");
					try {
						out.write(c);
					} catch (UnmappableCharacterException e) { // other IO exceptions are not caught here
						error = true;
						invalidChars.add((char) c);
						if (printErrors) {
							System.err.println(lineNo+","+colNo+": writing '"+(char) c+" ("+(int) c+"), not valid as "+outputEnc+" (line starts with '"+currLine+"')");
						}
					}
					if (c == '\r') {
						lastWasCR = true;
						lineNo++;
						colNo = 0;
						currLine = "";
					} else if (c == '\n') {
						if (lastWasCR) {
							lastWasCR = false;
						} else {
							lineNo++;
							colNo = 0;
							currLine = "";
						}
					}
					currLine += (char) c;
				}
			} catch (MalformedInputException e) {  // other IO exceptions are not caught here
				error = true;
				if (printErrors) {
					System.err.println(lineNo+","+colNo+": was not able to read the character as "+inputEnc+" (line starts with '"+currLine+"')");
				}
			} // NB: we suppose there is no UnmappableCharacterException in input (though in theory that's possible - can be caught here if somenone wants to code it)
			colNo++;
		} while ((c != -1) && (!error || !stopOnError));

		if (error) {
			char [] charErrors = new char[invalidChars.size()];
			int i=0;
			for (Character ch : invalidChars) {
				charErrors[i++] = ch;
			}
			return charErrors;
		} else {
			return null;
		}
	}


	public static char[] convertCheckEncoding(InputStream input, String inputEnc, boolean printErrors, boolean stopOnError) throws IOException {
		return convertCheckEncoding(input, inputEnc, "UTF-16", printErrors, stopOnError);

	}

	
	/**
	 * Checks whether a text file can be written in a given charset encoding.
	 * If no charset is provided the default encoding is used.
	 * 
	 * @param args Parameters syntax: &lt;filename&gt; [encoding] 
	 */
	public static void main(String[] args) {
		if ((args.length < 1) || (args.length > 3)) {
			System.err.println("Expected arguments: <filename> [input-encoding [output-encoding]]");
			System.err.println("Checks whether characters in the text file <filename> can be read\n as <input encoding>, and written as output-encoding.\ntests only reading if no output enc is provided, and uses\n default enc if no input-enc is provided");	
			System.exit(1);
		} else {
			String filename = args[0];
			char[] res = null;
			try {
				switch (args.length) {
				case 1:
					res = convertCheckEncoding(new FileInputStream(filename), DEFAULT_ENCODING, true, false);
					break;
				case 2:
					res = convertCheckEncoding(new FileInputStream(filename), args[1], true, false);
					break;
				case 3:
					res = convertCheckEncoding(new FileInputStream(filename), args[1], args[2], true, false);
					break;
				}
			} catch (Exception e) {
				System.err.println("Error:");
				e.printStackTrace();
				System.exit(2);
			}
			if (res == null) {
				System.out.println("No error found.");
			}
			System.exit(0);
		}
	}




	
	/**
	 * 
	 * Copied from http://www.javafaq.nu/java-example-code-1043.html
	 *
	 */
	public static class NullOutputStream extends OutputStream {

		  private boolean closed = false; 
		   
		  public void write(int b) throws IOException {
		    if (closed) throw new IOException("Write to closed stream");
		  }
		 
		  public void write(byte[] data, int offset, int length) throws IOException {
		    if (data == null) throw new NullPointerException("data is null");
		    if (closed) throw new IOException("Write to closed stream");
		  }
		 
		  public void close() {
		    closed = true;   
		  }   
		}

}
