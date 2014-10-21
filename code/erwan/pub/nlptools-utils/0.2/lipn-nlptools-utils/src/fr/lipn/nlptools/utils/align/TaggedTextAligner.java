package fr.lipn.nlptools.utils.align;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.lipn.nlptools.utils.align.TagPositionConsumer.AnnotationReceiver;


/**
 * A main program for standard alignment, file to file and reading/writing annotations as tags.
 * @author moreau
 *
 */
public class TaggedTextAligner {

	public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String VOID_RESERVED_WORD = "void";
	public static final String DEFAULT_POS_OUTPUT_SEPARATOR = "\t";

	static String referenceInputFilename = null;
	static String annotatedTextFilename = null;
	static String outputFilename = null;
	static boolean position = false;
	static String[] encodings = null;
	static char[] whitespaceChars = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws java.io.FileNotFoundException, java.io.UnsupportedEncodingException {
		
		dealWithOptions(args);
		String refEnc = DEFAULT_ENCODING;
		String annotEnc = DEFAULT_ENCODING; 
		String outEnc = DEFAULT_ENCODING;
		if (encodings != null) { 
			refEnc=encodings[0];
			annotEnc=encodings[0];
			outEnc=encodings[0];
			if (encodings.length == 2) {
				outEnc = encodings[1];
			} else if (encodings.length == 3) {
				annotEnc=encodings[1];
				outEnc=encodings[2];
			}
		}
		try {
			InputReader referenceIR;
			AnnotatedTextReader annotatedTR;
			AlignerConsumer consumer;
			Reader ar; 
			if (annotatedTextFilename == null) {
				ar = new InputStreamReader(System.in, annotEnc);
			} else {
				ar = new InputStreamReader(new FileInputStream(annotatedTextFilename), annotEnc);
			}
			annotatedTR = new SimpleTaggedTextReader(ar, whitespaceChars);
			if (referenceInputFilename == null) {
				referenceIR = new VoidInputReader(annotatedTR);
			} else {
				referenceIR = new SimpleInputReader(new InputStreamReader(new FileInputStream(referenceInputFilename), refEnc), whitespaceChars, null);
			}
			Writer ow;
			if (outputFilename == null) {
				ow = new OutputStreamWriter(System.out, outEnc);
			} else {
				ow = new OutputStreamWriter(new FileOutputStream(outputFilename), outEnc);
			}
			if (position) {
				consumer = new TagPositionConsumer(new TagPositionWriterConsumer(ow));
			} else {
				consumer = new TagAlignerWriterConsumer(ow);
			}
			annotatedTR.align(referenceIR, consumer);
		} catch (AlignmentException e) {
			System.err.println("An alignment error occured: ");
			e.printStackTrace(System.err);
			System.exit(3);
		}
		System.exit(0);
	}
	
	
	public static void dealWithOptions(String[] args) {
		Options options = new Options();
		
		Option help = new Option( "h", "Print this help message." );
		
		OptionBuilder.withArgName( "enc1[/enc2[/enc3]]" );
		OptionBuilder.hasArgs();
		OptionBuilder.withValueSeparator('/');
		OptionBuilder.withDescription(  "Use <enc1> as charset encoding. If a second encoding <enc2> is specified, it will be used for the output file only; finally if 3 encodings are provided the first one is for reference, the second one for the annotated text and the third for the output." );
        Option enc = OptionBuilder.create( "e" );
		
		OptionBuilder.withArgName( "chars" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Specify a set of characters to consider as extra whitespaces.");
		Option whChars = OptionBuilder.create( "w" );
		
		OptionBuilder.withDescription("If this flag is set, the output consists in indexes of the tags in the reference text rather than the actual text. The tags are written one by line with three columns: tag content, start index, end index.");
		Option pos = OptionBuilder.create( "p" );

		OptionBuilder.withArgName( "annotated" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The annotated text file that will be re-aligned with respect to the reference input file. If this option is not provided the annotated text is read from stdin.");
		Option annotated = OptionBuilder.create("a");
		
		OptionBuilder.withArgName( "output" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The output aligned file. If this option is not provided the result is written to stdout.");
		Option output = OptionBuilder.create("o");
		
		options.addOption(help);
		options.addOption(enc);
		options.addOption(whChars);
		options.addOption(pos);
		options.addOption(annotated);
		options.addOption(output);
		
		 CommandLineParser parser = new BasicParser();
		    try {
		        // parse the command line arguments
		        CommandLine line = parser.parse( options, args );
			    if (line.hasOption("h")) {
			    	printHelpAndExit(options, 0);
			    }
			    if (line.hasOption("p")) {
			    	position = true;
			    }
			    if (line.hasOption("e")) {
			    	encodings = line.getOptionValues("e");
			    }
			    if (line.hasOption("w")) {
			    	whitespaceChars = new char[line.getOptionValue("w").length()];
			    	line.getOptionValue("w").getChars(0, line.getOptionValue("w").length(), whitespaceChars, 0);
			    }
			    if (line.hasOption("a")) {
			    	annotatedTextFilename = line.getOptionValue("a");
			    }
			    if (line.hasOption("o")) {
			    	outputFilename = line.getOptionValue("o");
			    }
			    if (line.getArgs().length == 1) {
			    	referenceInputFilename = line.getArgs()[0];
			    	if (referenceInputFilename.equals(VOID_RESERVED_WORD)) {
			    		referenceInputFilename = null;
			    	}
			    } else {
			    	if (line.getArgs().length > 1) {
			    		System.err.println( "Too many arguments in command line" );
			    	} else {
			    		System.err.println( "Too few arguments in command line" );
			    	}
			    	printHelpAndExit(options, 1);
			    }
		    }
		    catch( ParseException exp ) {
		        // oops, something went wrong
		        System.err.println( "Error reading command line options.  Reason: " + exp.getMessage() );
		    	printHelpAndExit(options, 1);
		    }

		
	}
	

	public static void printHelpAndExit(Options options, int code) {
    	HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "TaggedTextFileAligner [options] <reference input filename>",LINE_SEPARATOR+"Aligns an annotated text (stdin or -a) with respect to the reference input file (the original text before being annotated). The result is written to stdout or to the file specified with -o. It is possible to set 'void' as a parameter instead of the reference filename: in this case no alignment is done at all, but that can be useful if -p option is set."+LINE_SEPARATOR+"Options:"+LINE_SEPARATOR,  options, LINE_SEPARATOR );
        System.exit(code);
	}

	protected static class TagPositionWriterConsumer implements AnnotationReceiver {
		
		Writer writer;
		String separator;
		
		public TagPositionWriterConsumer(Writer writer, String separator) {
			this.writer = writer;
			this.separator = separator;
		}
		
		public TagPositionWriterConsumer(Writer writer) {
			this(writer, DEFAULT_POS_OUTPUT_SEPARATOR);
		}
		
		public void initPositionConsumer() throws AlignmentException {
			
		}
		
		public void addUnannotatedToken(String inputToken, String annotatedToken, long start, long end) throws AlignmentException {
		}
		
		public void addAnnotation(String content, long start, long end) throws AlignmentException {
			try {
				writer.write(content+separator+start+separator+end+LINE_SEPARATOR);
			} catch (IOException e) {
				throw new AlignmentException("I/O error while writing results.", e);
			}
		}
		
		public void closePositionConsumer() throws AlignmentException {
			try {
				 writer.close();
				} catch (java.io.IOException e) {
					throw new AlignmentException("I/O error while closing writer", e);
				}
		}

	}
	
}
