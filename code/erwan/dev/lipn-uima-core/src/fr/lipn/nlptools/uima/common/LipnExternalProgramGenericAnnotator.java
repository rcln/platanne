package fr.lipn.nlptools.uima.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.utils.externprog.BroadcastReaderConsumer;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.StringReaderConsumer;
import fr.lipn.nlptools.utils.externprog.StringWriterFeeder;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * This class is intended to be a super-class for all LIPN componants built using an encapsulated external program.  
 * It simply provides a few protected methods that these componants should always call in their own methods, as well as 
 * a few constants which they should refer to whenever needed.
 * 
 * Important information for classes extending from this AE: there are a few parameters that you must include in the AE descriptor, namely
 * <code>TimeOut</code>, <code>CharacterCodingErrorReplacementValue</code> and a path for the program location. The behaviour due to
 * the <code>CharacterCodingErrorReplacementValue</code> parameter also requires to be cautious when reading the output from the external program:
 * if a value is defined, then some chars in the input text transmitted to the process may have been replaced with this value. That's why when
 * the output is read and possibly re-aligned with the original input there may be non matching chars: use appropriate options.
 * 
 */
public abstract class LipnExternalProgramGenericAnnotator extends JCasAnnotator_ImplBase {
	
	public static final String LIPN_MESSAGE_DIGEST = "fr.lipn.nlptools.uima.common.ErrorMessages";
	public static final String DEFAULT_CAS_LANGUAGE = org.apache.uima.cas.CAS.DEFAULT_LANGUAGE_NAME;
	public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
	public static final String DIR_SEPARATOR = System.getProperty("file.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String UNKNOWN_SOURCE = "UNKNOWN";
	public static final float UNDEFINED_CONFIDENCE = Float.NaN;
	public static final float DEFAULT_CONFIDENCE = UNDEFINED_CONFIDENCE;
	public static final String TIMEOUT_PARAM_NAME = "TimeOut";
	public static final String CHAR_CODING_ERR_REPLACE_VAL_PARAM_NAME = "CharacterCodingErrorReplacementValue";
	public static final String RUN_ID_VALUE_PARAM_NAME = "RunIdValue";
	public static final String KEEP_OUTPUT_IN_VIEW_PARAM_NAME = "KeepProgramOutputInView";
	public static final String ANNOTATION_FILTER_ATTR_VAL_SEPARATOR = "=";

	public final String DEFAULT_COMPONENT_ID = this.getClass().getCanonicalName();
	
	public static boolean DEBUG_USE_STRING_STREAMS = false;
	public static boolean DEBUG_WRITE_STREAMS_TO_LOG = false;
/*
	public static boolean DEBUG_USE_STRING_STREAMS = true;
	public static boolean DEBUG_WRITE_STREAMS_TO_LOG = true;
	*/
	File programDir;
	File programExec;
	int timeOut = 0;
	ExternalProgram program;
	String userFriendlyAEName;
	// must be REPORT or REPLACE - IGNORE is not allowed
	CodingErrorAction codingErrorAction;
	// must be only one char long
	String codingErrorReplacementValue;
	String runIdValue;
	String keepOutputInView;

	
	
	
	static Logger debugLog = null;

	/**
	 * This method should be called by the AE in its <code>initalize</code> method. It sets and checks the parameters which are common to
	 * all such AEs: program location (directory and name), time out optional parameter (notice that time out parameter name must be
	 * {@link #TIMEOUT_PARAM_NAME}), optional parameter named {@link #CHAR_CODING_ERR_REPLACE_VAL_PARAM_NAME} for coding error behaviour, and
	 * optional parameter {@link #RUN_ID_VALUE_PARAM_NAME}.
	 * 
	 * @param context The context containing the parameters values
	 * @param programDirParamName The name of the program directory parameter 
	 * @param programExecRelPath The name of the program (normally a path relative to the "program directory", e.g. "bin/myprog" - see below)
	 * @param userFriendlyAEName The AE name, as written in messages to the user.
	 * @param execPathIsRelativeToProgramDir tells how the <code>programExecRelPath</code> should be interpreted: relative or absolute 
	 * @throws ResourceInitializationException
	 */
	protected void initCommonParameters(UimaContext context, String programDirParamName, String programExecPath, String userFriendlyAEName, boolean execPathIsRelativeToProgramDir) throws ResourceInitializationException {

		context.getLogger().log(Level.FINE, "Initializing process for component "+userFriendlyAEName);
		// get mandatory program dir parameter 
		programDir = new File((String) context.getConfigParameterValue(programDirParamName));
		// get optional time out parameter
		if (context.getConfigParameterValue(TIMEOUT_PARAM_NAME) != null) {
			timeOut = ((Integer) context.getConfigParameterValue(TIMEOUT_PARAM_NAME)).intValue();
		}
		
		// get optional parameter for coding error actions
		
		codingErrorReplacementValue = (String) context.getConfigParameterValue(CHAR_CODING_ERR_REPLACE_VAL_PARAM_NAME);
		if ((codingErrorReplacementValue != null) && !codingErrorReplacementValue.isEmpty()) {
			codingErrorAction = CodingErrorAction.REPLACE;
			if (codingErrorReplacementValue.length() != 1) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{codingErrorReplacementValue, CHAR_CODING_ERR_REPLACE_VAL_PARAM_NAME, "must be empty or one character long"});
			}
		} else {
			codingErrorReplacementValue = null;
			codingErrorAction = CodingErrorAction.REPORT;
		}

		// can be null
		runIdValue = (String) context.getConfigParameterValue(RUN_ID_VALUE_PARAM_NAME);
		
		// can be null
		keepOutputInView = (String) context.getConfigParameterValue(KEEP_OUTPUT_IN_VIEW_PARAM_NAME);
		
			// check dir
		if (!programDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{programDir.getAbsolutePath()});
		}
		// check program
		if (programExecPath != null) {
			try {
				if (execPathIsRelativeToProgramDir) {
					programExec = new File(programDir.getCanonicalPath()+DIR_SEPARATOR+programExecPath);
				} else {
					programExec = new File(programExecPath);
				}
			} catch (IOException e) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "filesystem_error", new Object[]{programExec.getAbsolutePath()});
			}
			if (!programExec.exists()) { //NB : existence only is tested, since the program may be a symbolic link
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{programExec.getAbsolutePath()});
			}
		}

		this.userFriendlyAEName = userFriendlyAEName;

		try {
			context.getLogger().log(Level.INFO, "Initialization completed for component "+userFriendlyAEName+". Using executable "+((programExec!=null)?programExec.getCanonicalPath():"<not specified>")+", time-out: "+timeOut);
		} catch (IOException e) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "filesystem_error", new Object[]{programExec.getAbsolutePath()});
		}
		debugLog = context.getLogger();
	}
	

	/**
	 * equivalent to <code>initProgramLocation(context, programDirParamName, programExecPath, userFriendlyAEName, true)</code>
	 * (the exec path is interpreted relatively to the directory path).
	 * 
	 */
	protected void initCommonParameters(UimaContext context, String programDirParamName, String programExecPath, String userFriendlyAEName) throws ResourceInitializationException {
		initCommonParameters(context, programDirParamName, programExecPath, userFriendlyAEName, true);
	}

	
	/**
	 * This method can be called at the beginning of the <code>process</code> method, in the case where the AE needs that the language is set.
	 * Depending on the fact that <code>languageMap</code> is null or not, it also checks that the language is valid for the component and
	 * converts it to the form that the external program expects (e.g. "fr" -> "FRENCH")
	 * 
	 * @param maybeLanguage null or a string corresponding to a language (generally obtained as the language parameter of the AE)
	 * @param aJCas the CAS
	 * @param languageMap if not null, the method additionally checks that the language is valid (exists as a key) and returns its mapping. 
	 * @return
	 * <ul> 
	 * <li> <code>maybeLanguage</code> (or <code>languageMap.get(maybeLanguage)</code>)  if <code>maybeLanguage</code> is not null (higher priority),</li>
	 * <li> the language defined in the CAS  (or its mapping through <code>languageMap</code>) if it is defined and <code>maybeLanguage</code> is null (lower priority)</li>
	 * </ul>
	 * @throws AnalysisEngineProcessException if 
	 * <ul>
	 * <li><code>maybeLanguage</code> is null and there is no language defined in the CAS (no language error)</li>
	 * <li> <code>maybeLanguage</code> is not null and  does not contain the language as a key (invalid language error)</li>
	 * </ul>
	 */
	public String ensureLanguage(String maybeLanguage, JCas aJCas, Map<String, String> languageMap) throws AnalysisEngineProcessException {
		String theLanguage = maybeLanguage;
		if (maybeLanguage == null) {
			if ((aJCas.getDocumentLanguage() != null) && !aJCas.getDocumentLanguage().equals(LipnExternalProgramGenericAnnotator.DEFAULT_CAS_LANGUAGE)) {
				theLanguage = aJCas.getDocumentLanguage();
			} else {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "no_language", new Object[]{userFriendlyAEName});
			}
		}
		if (languageMap != null) {
			if (!languageMap.containsKey(theLanguage)) {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{getUserFriendlyAEName(), theLanguage, languageMap.keySet().toString()});
			} else {
				return languageMap.get(theLanguage);
			}
		}
		return theLanguage;
		
	}
	
	
	/*
	 * Finally aborted because it's probably not the right way: actually there is no good reason to use "approximatly the same language"
	 * if the user asks for en-US then and "en" is the only language available, an error should be raised in order to warn the user
	 * that this precise variant is not available.
	 * If the "variant" code is used, it must be that the component really handles it.
	 *  
	 *  
	 * @param lang1
	 * @param lang2
	 * @return true if languages codes are considered equal, e.g. "en-US" = "en"
	 * @throws AnalysisEngineProcessException if languages are not valid languages codes
	 
	public boolean compareLanguages(String lang1, String lang2) throws AnalysisEngineProcessException {
		int index= firstNonLetter(lang1);
		if (index != firstNonLetter(lang2)) {
			return false;
		}
		if (!lang1.substring(0, index).equals(lang2.substring(0, index))) {
			return false;
		}
		if ((lang1.length() == index) || (lang2.length() == index)) {
			return true; // first part of the language code is the same is the second part is not provided for at least one of them 
		}
		// return comparison between second parts:
		return (lang1.substring(index+1).equals(lang2.substring(index+1)));		
	}
	
	private int firstNonLetter(String s) {
		int i=0;
		while ((i<s.length()) && Character.isLetter(s.charAt(i))) {
			i++;
		}
		return i;
	}
	*/
	
	
	/**
	 * Should be called in the <code>process</code> method. 
	 * Initializes the {@link ExternalProgram} instance: program path, time out.
	 * 
	 * @param workingDir dir where the program will run (null for default)
	 * @param encoding charset encoding for the input/output streams (null for default)
	 * @return the ExternalProgram object.
	 * @throws AnalysisEngineProcessException never, actually.
	 */
	protected ExternalProgram initProgram(String workingDir, String encoding) throws AnalysisEngineProcessException {
		
		getContext().getLogger().log(Level.FINEST, "Starting to process doc. Initializing program for component "+userFriendlyAEName);
		List<String> command = new ArrayList<String>();
		try {
			command.add(programExec.getCanonicalPath());
		} catch (IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		program = new ExternalProgram(command, timeOut, workingDir, encoding);
		program.setLogger(this.getContext().getLogger());
		program.setDefaultCodingErrorAction(codingErrorAction);
		if (codingErrorReplacementValue != null) {
			program.setDefaultReplacementValue(codingErrorReplacementValue);
		}
		return program;

	}

	/**
	 * Convenience method for {@link #initProgram(String, String)}. current directory and default enxoding are used.
	 * 
	 * @return the ExternalProgram object.
	 * @throws AnalysisEngineProcessException never, actually.
	 */
	protected ExternalProgram initProgram() throws AnalysisEngineProcessException {
		return initProgram(null, null);
	}

	
	/**
	 * See {@link #runProgram(fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer)}
	 *
	 * This version of the method should be used for debugging purpose only: it permits to use "String streams" instead of using directly the
	 * objets responsible for writing/reading the input/output streams. This is safer, because concurrency errors are less likely this way; but
	 * it is also a lot less efficient, because 1) storing strings takes time and space, 2) even in the case you want to use String streams, here
	 * they are converted once again (because the method is not supposed to know that they are already string streams objects). So the first interest
	 * to use this method is: you are facing bugs using complex <code>WriterFeeder/ReaderConsumer</code> objects, this method provides a convenient
	 * way to transform them as string streams objets to check whether bugs come from concurrency problems. 
	 * Additionally, the method permits to write the content of the streams to the logger (log level is <code>Level.INFO</code>): that is the second
	 * interest in using this method.
	 * 
	 * @param stdin the object responsible for providing the input stream (may be null)
	 * @param stdout the object responsible for receiving the output stream (may be null)
	 * @param boolean debugUseStringStreams convert objets stdin and stdout to/from String (reader or writer). 
	 * @param boolean debugWriteStreamsToLog only in case <code>debugUseStringStreams</code> is set, prints input/output streams to log.  
	 * @return the exit code
	 * @throws AnalysisEngineProcessException if exit code is not 0 or stderr is not empty, or if an exception is raised at a lower level.
	 */
	protected int runProgram(JCas aJCas, WriterFeeder stdin, ReaderConsumer stdout, boolean debugUseStringStreams, boolean debugWriteStreamsToLog, boolean checkStderr) throws AnalysisEngineProcessException {
		
		String source = getSourceDocumentAnnotation(aJCas);
		int exitCode = 0;
		JCas newView = null;
		if ((keepOutputInView != null) && !keepOutputInView.isEmpty()) {
			if (stdout == null) {
				getContext().getLogger().log(Level.WARNING, "Parameter '"+KEEP_OUTPUT_IN_VIEW_PARAM_NAME+"' is defined but no stdout stream is expected (consumer is null): ignoring, no view created.");
			} else {
				// CREATE NEW VIEW
				getContext().getLogger().log(Level.INFO, "Creating new view '"+keepOutputInView+"' to store program output.");
				try {
					newView = aJCas.createView(keepOutputInView);
				} catch (CASException e) {
					throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "cas_exception", new Object[]{"error trying to create view '"+keepOutputInView+"'"}, e);
				} catch (CASRuntimeException cre) {
					throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "view_already_exists", new Object[]{"error trying to create view '"+keepOutputInView+"'"}, cre);
				}

			}
		}
		getContext().getLogger().log(Level.INFO, "Running program for component "+userFriendlyAEName+" "+program.getCommand().toString()+" with data from "+source+" (debug 'string' mode is "+debugUseStringStreams+")...");
		try {
			if (debugUseStringStreams) {
				StringWriter inputContent = new StringWriter();
				if (stdin != null) {
					stdin.feedWriter(inputContent);
				}
				if (debugWriteStreamsToLog) {
					getContext().getLogger().log(Level.INFO, "DEBUG MODE - INPUT STREAM: "+LINE_SEPARATOR+((stdin==null)?"NULL INPUT STREAM":inputContent.toString()));
				}
				exitCode = program.run((stdin==null)?null:(new StringWriterFeeder(inputContent.toString())), (stdout==null)?null:new StringReaderConsumer(), new StringReaderConsumer());
				if (debugWriteStreamsToLog) {
					getContext().getLogger().log(Level.INFO, "DEBUG MODE - OUTPUT STREAM: "+LINE_SEPARATOR+((stdout==null)?"NULL OUTPUT STREAM":((StringReaderConsumer) program.getLastStdoutConsumer()).getString()));
				}
				if (stdout != null) {
					stdout.consumeReader(new StringReader(((StringReaderConsumer) program.getLastStdoutConsumer()).getString()));
					if (newView != null) {
						getContext().getLogger().log(Level.FINE, "Writing program output to view '"+keepOutputInView+"'.");
						newView.setDocumentText(((StringReaderConsumer) program.getLastStdoutConsumer()).getString());
					}
				}
			} else { // the normal way
				ReaderConsumer stdoutConsumer = stdout;
				StringReaderConsumer newViewStringConsumer = null;
				if (newView != null) {
					newViewStringConsumer = new StringReaderConsumer();
					stdoutConsumer = new BroadcastReaderConsumer(new ReaderConsumer[]{stdout, newViewStringConsumer}, getContext().getLogger());
				}
				exitCode = program.run(stdin, stdoutConsumer, new StringReaderConsumer());
				if (newView != null) {
					getContext().getLogger().log(Level.FINE, "Writing program output to view '"+keepOutputInView+"'.");
					newView.setDocumentText(newViewStringConsumer.getString());
				}
			}
		} catch (ExternalProgramException e) {
			checkStderr(program, exitCode, source); // if stderr not empty or exit code not 0, throw this exception (and not e)				
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "external_program_error", new Object[]{userFriendlyAEName, source, program.getCommand().toString()}, e);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unexpected_error", new Object[]{userFriendlyAEName}, e);
		}
		if (checkStderr) {
			checkStderr(program, exitCode, source);
			getContext().getLogger().log(Level.FINEST, "Program exited normally for component "+userFriendlyAEName);
		} else {
			getContext().getLogger().log(Level.FINEST, "End of program for component '"+userFriendlyAEName+"' (stderr output has not been checked)");
		}
		return exitCode;
	}

	/**
	 * Should be called in the process method to run the external program (after setting all its parameters).
	 * This method mainly handles errors by calling {@link #checkStderr(String, ExternalProgram, int)} (depending on <code>checkStderr</code> value). 
	 * Notice that in any error case priority is given to the stderr stream: if it is not empty, an exception corresponding to this error
	 *  is sent first. That means that an alignment error can be hidden behind this error (but in general it is actually caused by this error, 
	 *  that's why this strategy is chosen).
	 * 
	 * @param stdin the object responsible for providing the input stream (may be null)
	 * @param stdout the object responsible for receiving the output stream (may be null)
	 * @param checkStderr whether to check stderr output (true by default, see {@link #runProgram(JCas, fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer, boolean)}
	 *                    Not always possible because some programs always output information on the stderr stream (including if no error happens)
	 * @return the exit code
	 * @throws AnalysisEngineProcessException if exit code is not 0 or stderr is not empty, or if an exception is raised at a lower level.
	 */
	protected int runProgram(JCas aJCas, WriterFeeder stdin, ReaderConsumer stdout, boolean checkStderr) throws AnalysisEngineProcessException {
		return runProgram(aJCas, stdin, stdout, DEBUG_USE_STRING_STREAMS, DEBUG_WRITE_STREAMS_TO_LOG, checkStderr);
	}

	protected int runProgram(JCas aJCas, WriterFeeder stdin, ReaderConsumer stdout) throws AnalysisEngineProcessException {
		return runProgram(aJCas, stdin, stdout, DEBUG_USE_STRING_STREAMS, DEBUG_WRITE_STREAMS_TO_LOG, true);
	}


	protected String getSourceDocumentAnnotation(JCas aJCas) {
	    FSIterator<Annotation> it = FSIteratorFactory.createFSIterator(aJCas, SourceDocumentInformation.type, false);
	    if (it.hasNext()) {
	      SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
	      return fileLoc.getUri();
	    } else {
	      return UNKNOWN_SOURCE;
	    }
	}
	
	/**
	 * This method should only be used if {@link #runProgram(String, fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder, fr.lipn.uima.util.externprog.ExternalProgram.ReaderConsumer, boolean, boolean)}
	 * is not used (thus generally not used). It handles possible errors <strong>after</strong> running <code>program<code>, by raising an exception if stderr is not empty
	 * or if the exit code is not 0.
	 * 
	 * 
	 * @param program program instance after it has been run. Moreover it <strong>must</strong> have been run with a {@link StringReaderConsumer} object
	 *                to consume the stderr stream. 
	 * @param exitCode program exit code; set to 0 if no exception should be raised because of that.
	 * @throws AnalysisEngineProcessException if stderr is not empty or if the exit code is not 0.
	 */
	protected void checkStderr(ExternalProgram program, int exitCode, String source) throws AnalysisEngineProcessException {
		String stderr = ((StringReaderConsumer) program.getLastStderrConsumer()).getString();
		if (exitCode != 0) {
			if ((stderr == null) || (stderr.isEmpty())) {
				throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "external_program_nonnul_exitcode_empty_stderr", new Object[]{userFriendlyAEName, source, exitCode, program.getCommand().toString()});
			} else {
				throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "external_program_nonnul_exitcode_with_stderr", new Object[]{userFriendlyAEName, source, exitCode, program.getCommand().toString(), stderr});
			}
		}
		if ((stderr != null) && (!stderr.isEmpty())) {
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "external_program_non_empty_stderr", new Object[]{userFriendlyAEName, source, program.getCommand().toString(), stderr});
		}
	}
	
	public static void setGenericAttributes(GenericAnnotation a, String componentId, String typeId, String runId, double confidence) {
		a.setComponentId(componentId);
		a.setTypeId(typeId);
		a.setConfidence(confidence);
		if (runId != null) {
			a.setRunId(runId);
		}
	}


	public static void setGenericAttributes(GenericAnnotation a, int start, int end, String componentId, String typeId, String runId, double confidence) {
		setGenericAttributes(a, componentId, typeId, runId, confidence);
		a.setBegin(start);
		a.setEnd(end);
	}
	
	public void setGenericAttributes(GenericAnnotation a, String componentId, String typeId, double confidence) {
		setGenericAttributes(a, componentId, typeId, runIdValue, confidence);
	}
	
	public void setGenericAttributes(GenericAnnotation a, double confidence) {
		setGenericAttributes(a, DEFAULT_COMPONENT_ID, a.getClass().getSimpleName(), confidence);
	}

	public void setGenericAttributes(GenericAnnotation a, int start, int end, String componentId, String typeId, double confidence) {
		setGenericAttributes(a, componentId, a.getClass().getSimpleName(), confidence);
		a.setBegin(start);
		a.setEnd(end);
	}

	public void setGenericAttributes(GenericAnnotation a, int start, int end, double confidence) {
		setGenericAttributes(a, start, end, DEFAULT_COMPONENT_ID, a.getClass().getSimpleName(), confidence);
	}

	public void setGenericAttributes(GenericAnnotation a, int start, int end) {
		setGenericAttributes(a, start, end, DEFAULT_COMPONENT_ID, a.getClass().getSimpleName(), DEFAULT_CONFIDENCE);
	}
	
	public void setGenericAttributes(GenericAnnotation a) {
		setGenericAttributes(a, DEFAULT_COMPONENT_ID, a.getClass().getSimpleName(), DEFAULT_CONFIDENCE);
	}

	/**
	 * Creates two arrays of features names and values in order to build a constraint to filter annotations, based on a String[] parameter containing strings of the form
	 * 'featureName=featureValue'. The resulting constraint will be a 'AND' between all these conditions.
	 * See also {@link #createAnnotationFilterConstraint(JCas, int, String[][], boolean)}
	 * 
	 * @param filter the input string array
	 * @param parameterName the name of the parameter where this data was read, to provide an explicit error message.
	 * @return An array of size 2, containing two String arrays: the first one contains the names, the second one contains the values. This array
	 *         can be used with {@link #createAnnotationFilterConstraint(JCas, int, String[][], boolean)}.
	 * @throws ResourceInitializationException if the format is not satisfied
	 */
	public String[][] parseAnnotationFilter(String[] filter, String parameterName) throws ResourceInitializationException {
		if (filter == null) {
			return null;
		}
		String[] names = new String[filter.length];
		String[] values = new String[filter.length];
		for (int i=0; i< filter.length; i++) {
			int sep = filter[i].indexOf(ANNOTATION_FILTER_ATTR_VAL_SEPARATOR);
			if (sep == -1) {
				throw new ResourceInitializationException(LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{filter[i], parameterName, "must be of the form 'featureName+ANNOTATION_FILTER_ATTR_VAL_SEPARATOR+value'"});
			}
			names[i] = filter[i].substring(0, sep);
			values[i] = filter[i].substring(sep + ANNOTATION_FILTER_ATTR_VAL_SEPARATOR.length());
	//		System.err.println("DEBUG parse constraint: "+names[i]+"/"+values[i]);
		}
		return new String[][]{names, values};
	}
	
	
	/**
	 * Given an already parsed filter (see {@link #parseAnnotationFilter(String[], String)}), creates the constraint
	 * 
	 * @param aJCas the CAS
	 * @param type the type of the annotations
	 * @param filterArrays the result of <code>parseAnnotationFilter</code>: an array containing two String arrays (first for names, second for values)
	 * @param threadSafe whether CAS access should be synchronized or not
	 * @return theconstraint, or null if the input array is null, empty, or does not have the valid format.
	 */
	public FSMatchConstraint createAnnotationFilterConstraint(JCas aJCas, int type, String[][] filterArrays, boolean threadSafe) {
		if ((filterArrays == null) || (filterArrays.length != 2)) {
			return null;
		}
		return FSIteratorFactory.createMultiStringConstraint(aJCas, type, filterArrays[0], filterArrays[1], threadSafe);
	}

	
	/**
	 * simpler version where type is <code>GenericAnnotation</code> and threadSafe is false.
	 * 
	 * @param aJCas
	 * @param filterArrays
	 * @return the constraint
	 */
	public FSMatchConstraint createAnnotationFilterConstraint(JCas aJCas, String[][] filterArrays) {
		return createAnnotationFilterConstraint(aJCas, GenericAnnotation.type, filterArrays, false);
	}

	
	
	public static void debugWriteLog(String s) {
		if (debugLog != null) {
		   debugLog.log(Level.INFO, s);
		}
	}
	
	public Logger getLogger() {
		return getContext().getLogger();
	}
	
	public String getDefaultComponentId() {
		return DEFAULT_COMPONENT_ID;
	}
	

	public ExternalProgram getProgram() {
		return program;
	}


	public void setProgram(ExternalProgram program) {
		this.program = program;
	}


	public File getProgramDir() {
		return programDir;
	}


	public void setProgramDir(File programDir) {
		this.programDir = programDir;
	}


	public File getProgramExec() {
		return programExec;
	}


	public void setProgramExec(File programExec) {
		this.programExec = programExec;
	}


	public int getTimeOut() {
		return timeOut;
	}


	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}


	public String getUserFriendlyAEName() {
		return userFriendlyAEName;
	}


	public void setUserFriendlyAEName(String userFriendlyAEName) {
		this.userFriendlyAEName = userFriendlyAEName;
	}


	public char getCodingErrorReplacementValue() {
		if (codingErrorReplacementValue == null) {
			return (char) -1;
		} else {
			return codingErrorReplacementValue.charAt(0);
		}
	}

	public void setCodingErrorReplacementValue(char codingErrorReplacementValue) {
		this.codingErrorReplacementValue = ""+codingErrorReplacementValue;
	}
	
	
	// TODO defined here ??
	public CharsetEncoder createCharsetEncoderWithErrorCodingAction(String encoding) throws AnalysisEngineProcessException  {
		CharsetEncoder csEncoder = Charset.forName(encoding).newEncoder();
		if (codingErrorReplacementValue != null) {
			try {
				ByteBuffer bytesValue = csEncoder.encode(CharBuffer.wrap(codingErrorReplacementValue,0,1));
				csEncoder.replaceWith(bytesValue.array());
			} catch (CharacterCodingException e) {
				throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "invalid_char_replacement_value_for_encoding", new Object[]{userFriendlyAEName, codingErrorReplacementValue, encoding});
			}
			csEncoder.reset();
		}
		if (codingErrorAction != null) {
			csEncoder.onMalformedInput(codingErrorAction).onUnmappableCharacter(codingErrorAction);
		}
		return csEncoder;
		
	}
	
	public boolean isCodingErrorActionReplace() {
		return codingErrorAction.equals(CodingErrorAction.REPLACE);
	}

	// TODO : this method should not be defined here, probably in nlptools.utils.misc
	// remark: similar method in TokenIteratorInputReader
	/**
	 * TODO
	 */
	public static boolean compareModuloWilcardChar(String original, String copy, char wildcardChar, boolean ignoreCase) {
		if (ignoreCase && original.equalsIgnoreCase(copy)) {
			return true;
		}
		if (!ignoreCase && original.equals(copy)) {
			return true;
		}
		if (wildcardChar == -1) {
			return false;
		}
		if (original.length() == copy.length()) {
			for (int i=0; i<original.length(); i++) {
				if ((copy.charAt(i) != wildcardChar) && (original.charAt(i)  != copy.charAt(i)) && (!ignoreCase || (Character.toLowerCase(original.charAt(i))  != Character.toLowerCase(copy.charAt(i))))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	

}
