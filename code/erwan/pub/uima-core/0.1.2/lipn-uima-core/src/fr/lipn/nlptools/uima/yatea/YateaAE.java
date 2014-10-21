package fr.lipn.nlptools.uima.yatea;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.lipn.nlptools.utils.misc.FileUtil;
import fr.lipn.nlptools.uima.common.BasicTokenIterator;
import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.common.POSLemmaPairInterpretationTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.treetagger.TreeTaggerAE;
import fr.lipn.nlptools.uima.types.Token;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.StringReaderConsumer;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * 
 * TODO doc
 * 
 * config file is created for each document in the process method because the language is unknown before.
 * @author moreau
 *
 */
public class YateaAE extends LipnExternalProgramGenericAnnotator {
	

	public static final String YATEA_DEFAULT_ENCODING = "ISO-8859-1";
	public final String AE_NAME = getDefaultComponentId();

	public static final String YATEA_STANDARD_LINGUISTIC_DATA_DIR = "config";
	public static final String YATEA_STANDARD_LOCALE_DIR = "locale";
	// maybe dangerous: relative path from "Yatea Data Dir", hoping the dtd is always located here
	public static final String YATEA_STANDARD_DTD_LOCATION = "../doc/YaTeA/DTD/yatea.dtd";
	public static final String YATEA_OPTIONS_START_TAG = "<OPTIONS>";
	public static final String YATEA_OPTIONS_END_TAG = "</OPTIONS>";
	public static final String YATEA_CONFIG_START_TAG = "<DefaultConfig>";
	public static final String YATEA_CONFIG_END_TAG = "</DefaultConfig>";
	public static final String YATEA_CONFIG_LING_DATA_NAME = "CONFIG_DIR";
	public static final String YATEA_CONFIG_LOCALE_NAME = "LOCALE_DIR";
	public static final String YATEA_CONFIG_RESULT_NAME = "RESULT_DIR";
	public static final String YATEA_CONFIG_FILE_OPTION = "-rcfile";
	
	public static final String YATEA_INPUT_FILE_EXTENSION = ".tmp";
	public static final String YATEA_XML_OUTPUT_DIR = "default/xml";
	public static final String YATEA_XML_OUTPUT_NAME = YATEA_XML_OUTPUT_DIR + DIR_SEPARATOR + "candidates.xml";
	public static final String YATEA_DTD_EXPECTED_FILENAME = "extracteurDeTermes.dtd";
	
	public static final Map<String, String> YATEA_LANGUAGE_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("fr", "FR");
        aMap.put("en", "EN");
        YATEA_LANGUAGE_MAP = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<String, String> YATEA_UNMODIFIABLE_OPTIONS;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        // most of these options concern "result display"
        aMap.put("termList", "0");
        aMap.put("xmlout", "1");
        aMap.put("printChunking", "0");
        aMap.put("TT-for-BioLG", "0");
        aMap.put("XML-corpus-for-BioLG", "0");
        aMap.put("TC-for-BioLG", "0");
        aMap.put("TTG-style-term-candidates", ""); // output in some kind of TT format (with repetitions)
        aMap.put("suffix", "default"); // output dir/files name (unused, a specific temp dir is used)
        aMap.put("debug", "0");
        YATEA_UNMODIFIABLE_OPTIONS = Collections.unmodifiableMap(aMap);
    }

    String encoding = YATEA_DEFAULT_ENCODING;
    String lang;
    File customConfigFile;
    File linguisticDataDir;
    File localeDir;
    File tempDir;
    File outputDTDFile;
    HashMap<String, String> yateaOptions;
    boolean tagOccurrencesOnly;
    boolean useYateaDetailedTerms;
    boolean deleteTempFilesAfterProcess;
	String[][] sentenceFilter; 
	String[][] tokenFilter;
	String[][] posFilter;
	String[][] lemmaFilter;

    
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		// please notice that super.programDir is used to store the yatea DATA path (since data and executable are not together in std install)
		// we call "DATA PATH" the path containing "locale", "config" and "samples" dirs
		String execName = (String) context.getConfigParameterValue("YateaExecutableFile");
		initCommonParameters(context, "YateaDataDir", execName, AE_NAME, false);

		sentenceFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("SentenceFilter"),"SentenceFilter");
		tokenFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("TokenFilter"),"TokenFilter");
		posFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("PartOfSpeechFilter"),"PartOfSpeechFilter");
		lemmaFilter = parseAnnotationFilter((String[]) context.getConfigParameterValue("LemmaFilter"),"LemmaFilter");
		
		yateaOptions = new HashMap<String, String>();
		for (String optKey : YATEA_UNMODIFIABLE_OPTIONS.keySet()) {   // put permanent options
			yateaOptions.put(optKey, YATEA_UNMODIFIABLE_OPTIONS.get(optKey));
		}
		// put user-defined options (except language)
		if (context.getConfigParameterValue("StrictMatchType") != null) {
			String val = (String) context.getConfigParameterValue("StrictMatchType");
			if (val.isEmpty() || val.equals("strict") || val.equals("loose")) {
				yateaOptions.put("match-type", val);
			} else {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter_list", new Object[]{val, "StrictMatchType", "'loose', 'strict' or empty string"});
			}
		} else {
			yateaOptions.put("match-type", "");
		}
		if (context.getConfigParameterValue("Termino") != null) {
			yateaOptions.put("termino", (String) context.getConfigParameterValue("Termino"));
			if (!(new File(yateaOptions.get("termino")).exists())) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{yateaOptions.get("termino")});				
			}
		}
		yateaOptions.put("monolexical-all", (((Boolean) context.getConfigParameterValue("MonolexicalAll")).booleanValue())?"1":"0");
		yateaOptions.put("monolexical-included", (((Boolean) context.getConfigParameterValue("MonolexicalIncluded")).booleanValue())?"1":"0");
		// warning: it seems that this option has been removed in YaTeA (thus has no effect) 
		//yateaOptions.put("annotate-only", (((Boolean) context.getConfigParameterValue("AnnotateOnly")).booleanValue())?"1":"0");
		if (Locale.getDefault().getLanguage().equals(Locale.FRENCH.getLanguage())) {
			yateaOptions.put("MESSAGE_DISPLAY", YATEA_LANGUAGE_MAP.get("fr"));				
		} else {
			yateaOptions.put("MESSAGE_DISPLAY", YATEA_LANGUAGE_MAP.get("en"));				
		}

		if (context.getConfigParameterValue("Language") != null) {
			lang = (String) context.getConfigParameterValue("Language");
			if (!YATEA_LANGUAGE_MAP.containsKey(lang)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{lang, AE_NAME, YATEA_LANGUAGE_MAP.keySet().toString()});
			}
		}
		if (((Boolean) context.getConfigParameterValue("UseUTF8Encoding")).booleanValue()) {
			encoding = "UTF-8";
		}
		if (context.getConfigParameterValue("TempDir") != null) {
			tempDir = new File((String) context.getConfigParameterValue("TempDir"));
		}
		
		if (context.getConfigParameterValue("CustomConfigFile") != null) {
			customConfigFile = new File((String) context.getConfigParameterValue("CustomConfigFile"));
		}
		tagOccurrencesOnly = (((Boolean) context.getConfigParameterValue("TagOccurrencesOnly")).booleanValue());
		useYateaDetailedTerms = (((Boolean) context.getConfigParameterValue("YateaTermDetails")).booleanValue());
		deleteTempFilesAfterProcess = ((Boolean) context.getConfigParameterValue("DeleteTempFilesAfterProcess")).booleanValue();

		try {
			if (context.getConfigParameterValue("CustomLinguisticDataDir") != null) {
				linguisticDataDir = new File((String) context.getConfigParameterValue("CustomLinguisticDataDir"));
			} else {
				linguisticDataDir = new File(getProgramDir().getCanonicalPath()+DIR_SEPARATOR+YATEA_STANDARD_LINGUISTIC_DATA_DIR);
			}
			if (context.getConfigParameterValue("DtdLocation") != null) {
				outputDTDFile = new File((String) context.getConfigParameterValue("DtdLocation"));
			} else {
				outputDTDFile = new File(getProgramDir().getCanonicalPath()+DIR_SEPARATOR+YATEA_STANDARD_DTD_LOCATION);
			}
				
			localeDir = new File(getProgramDir().getCanonicalPath()+DIR_SEPARATOR+YATEA_STANDARD_LOCALE_DIR+DIR_SEPARATOR);

			// check file(s)
			if ((tempDir != null) && (!tempDir.isDirectory())) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{tempDir.getName()});
			}
			if (!outputDTDFile.exists()) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{outputDTDFile.getName()});
			}
			if ((customConfigFile != null) && !customConfigFile.exists()) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{customConfigFile.getName()});
			} else {
//				not used if user-defined config file
				if (!linguisticDataDir.isDirectory()) { 
					throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{linguisticDataDir.getName()});
				}
				if (!localeDir.isDirectory()) { 
					throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{localeDir.getName()});
				}
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", new Object[]{});
		}
		
		
		try {
			// Warning: it is necessary to create a temp dir because YaTeA creates its own subdir named after the input filenamename (by default)
			// thus if we don't make a special temp dir problems may appear if there are other files/dir with the same name and/or if UIMA runs
			// several instances of this AE. Even if such problems are unlikely, preventing is better than debugging ;-)
			// moreover this way cleaning temp files is easier
			tempDir = FileUtil.createTempDir("YaTeA_task_in_progress_", null, tempDir);
		} catch (IOException e) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_write_temp_file", null, e);
		}

		}

	
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// ensure language is set
		String language = ensureLanguage(lang, aJCas, YATEA_LANGUAGE_MAP);

		FSMatchConstraint sentenceConstraint = createAnnotationFilterConstraint(aJCas, sentenceFilter);
		FSMatchConstraint tokenConstraint = createAnnotationFilterConstraint(aJCas, tokenFilter);
		FSMatchConstraint posConstraint = createAnnotationFilterConstraint(aJCas, posFilter);
		FSMatchConstraint lemmaConstraint = createAnnotationFilterConstraint(aJCas, lemmaFilter);

		
		if (customConfigFile == null) {
			try {
			// check that language-specific data dir exists (if no user-defined config file is used)
			if (!new File(linguisticDataDir.getCanonicalPath()+DIR_SEPARATOR+language).isDirectory()) {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{linguisticDataDir.getName()+DIR_SEPARATOR+YATEA_LANGUAGE_MAP.get(language)});				
			}
			// check YaTeA locale dir (except  if user-defined config file is used)
			if (!new File(localeDir.getCanonicalPath()+DIR_SEPARATOR+yateaOptions.get("MESSAGE_DISPLAY")).isDirectory()) {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{localeDir.getName()+DIR_SEPARATOR+yateaOptions.get("MESSAGE_DISPLAY")});
			}
			} catch (IOException e) {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
			}
			
		}

		File tempConfigFile;
		File tempInputFile;
		try {

			// create config file (specific to a process run because of language -> it is possible that texts in 
			//different languages belong to the same collection of texts
			tempConfigFile = File.createTempFile("YaTeA_AE_config_", null, tempDir);
			OutputStreamWriter config = new OutputStreamWriter(new java.io.FileOutputStream(tempConfigFile), encoding);
			config.write(YATEA_OPTIONS_START_TAG+LINE_SEPARATOR);
			config.write("  language = "+language+LINE_SEPARATOR);
			for (String optKey : yateaOptions.keySet()) {
				config.write("  "+optKey+" = "+yateaOptions.get(optKey)+LINE_SEPARATOR);
			}
			config.write(YATEA_OPTIONS_END_TAG+LINE_SEPARATOR);
			config.write(YATEA_CONFIG_START_TAG+LINE_SEPARATOR);
			config.write("  "+YATEA_CONFIG_LING_DATA_NAME+" = "+linguisticDataDir.getCanonicalPath()+LINE_SEPARATOR);
			config.write("  "+YATEA_CONFIG_RESULT_NAME+" = "+tempDir.getCanonicalPath()+LINE_SEPARATOR);
			config.write("  "+YATEA_CONFIG_LOCALE_NAME+" = "+localeDir.getCanonicalPath()+LINE_SEPARATOR);
			config.write(YATEA_CONFIG_END_TAG+LINE_SEPARATOR);
			config.close();

			// create input file
			// Warning: YaTeA inputs MUST have an extension, otherwise it can not create a directory with the same name without extension
			//          However Java createTempFile function adds ".tmp" if suffix is set to null (cf Java API), 
			//          but we need to know the suffix in order to find the output file(s)
			tempInputFile = File.createTempFile("YaTeA_Input", YATEA_INPUT_FILE_EXTENSION, tempDir);  // NB: if tempDir is null the default system temp dir is used
			OutputStreamWriter input = new OutputStreamWriter(new java.io.FileOutputStream(tempInputFile), encoding);
	    	WriterFeeder writerFeeder = new TokenByLineWriterFeeder(new POSLemmaPairInterpretationTokenPOSLemmaIterator(aJCas, true, true, false, sentenceConstraint, tokenConstraint, posConstraint, lemmaConstraint), LipnExternalProgramGenericAnnotator.LINE_SEPARATOR, TreeTaggerAE.TREETAGGER_COLUMN_SEPARATOR, TreeTaggerAE.TREETAGGER_UNKNOWN_LEMMA_TAG);
			writerFeeder.feedWriter(input);
		} catch (IOException e) { // a bit simplified, but normally all kind of I/O errors concern writing temp file
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_write_temp_file", null, e);			
		} catch (ExternalProgramException epe) {  // the same actually
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_write_temp_file", null, epe);
		}

		// build command
		try {
			ExternalProgram program = initProgram(tempDir.getCanonicalPath(), encoding);
			List<String> command = program.getCommand();
			command.add(YATEA_CONFIG_FILE_OPTION);
			if (customConfigFile != null) {
				command.add(customConfigFile.getCanonicalPath());
			} else {
				command.add(tempConfigFile.getCanonicalPath());
			}
			command.add(tempInputFile.getCanonicalPath());

			// run program and deal with errors
			// NB: YaTeA always print information on stderr, that's why we don't check stderr
			int exitCode = runProgram(aJCas, null, null, false);
			if (exitCode != 0) {
				String stderr = ((StringReaderConsumer) program.getLastStderrConsumer()).getString();
				throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "external_program_nonnul_exitcode_with_stderr", new Object[]{AE_NAME, getSourceDocumentAnnotation(aJCas), exitCode, program.getCommand().toString(), stderr});
			}
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		
		
		// parse the resulting file to add annotations
		String yateaOutputDir = tempInputFile.getAbsolutePath().substring(0, tempInputFile.getAbsolutePath().length()-YATEA_INPUT_FILE_EXTENSION.length());
		
		File outputFile = new File(yateaOutputDir+ DIR_SEPARATOR + YATEA_XML_OUTPUT_NAME);
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(new YateaXMLOutputParser(aJCas, parser, this, getContext().getLogger(), tagOccurrencesOnly, useYateaDetailedTerms));
			parser.setEntityResolver(new YateaDTDFileResolver());
			parser.parse(new InputSource(new FileInputStream(outputFile)));
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_read_file", new Object[]{outputFile.getAbsolutePath()}, e);						
		} catch (SAXException se) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "xml_parse_error", new Object[]{AE_NAME, outputFile.getAbsolutePath()}, se);						
		}

	}
	
	/**
	 * 
	 * This resolver is needed because the xml output file refers to a DTD file which does
	 * not exist (location and name are wrong), so it must be redirected to the right 
	 * filename. 
	 * 
	 * @author moreau
	 *
	 */
	protected class YateaDTDFileResolver implements EntityResolver {

		public InputSource resolveEntity (String publicId, String systemId)  throws IOException {
			if (systemId.endsWith(YATEA_DTD_EXPECTED_FILENAME)) {
				return new InputSource(new FileInputStream(outputDTDFile));
			} else {
				// use the default behaviour
				return null;
			}
		}
	}

	
	
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		if (deleteTempFilesAfterProcess) {
			if (!FileUtil.recDeleteDir(tempDir)) {
				throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unable_delete", new Object[]{tempDir.getAbsolutePath()});
			}
		} else {
			getContext().getLogger().log(Level.INFO, "YaTeA temporary files are not deleted in directory "+tempDir.getAbsolutePath());
		}
	}

	
	
	/**
	 * start/end indexes in the xml output are computed from the 'token by line' format,
	 * thus are generally different from the original indexes.
	 * This method is used to match a "Yatea output" index to the corresponding UIMA token/index
	 *
	 * Important information: the Yatea indexing is quite strange: it is based on sentences
	 * (that is to say the counter is reset at each new sentence), and more strangely the
	 * counter is incremented at each token (+ the length of the token of course)
	 *
	 * @param aJCas
	 * @return
	 * @throws AnalysisEngineProcessException
	 */
	public static ArrayList<HashMap<Integer, Annotation>> initTokenIndexMapping(JCas aJCas) throws AnalysisEngineProcessException {
		ArrayList<HashMap<Integer, Annotation>> sentenceTokenIndexMap = new ArrayList<HashMap<Integer, Annotation>>(); 
		YateaTokenIterator iter = new YateaAE.YateaTokenIterator(aJCas);
		
		int index = 0; // (this index follows the Yatea indexing strategy)
		while (iter.hasNext()) {
			Token token = iter.next();
			int sentenceId = iter.getSentenceId();
			while (sentenceId >= sentenceTokenIndexMap.size()) { // normally 'if' would be enough, maybe there can be empty sentences ??
				index = 0;
				sentenceTokenIndexMap.add(new HashMap<Integer, Annotation>());
			}
			Integer start = index;
			index += token.getCoveredText().length(); // maybe quite dirty ?
			Integer end = index;
			index++;
			//System.err.println("DEBUG Writing mapping from start "+start+"->"+token.getBegin()+" and end "+end+"->"+token.getEnd()+" for sentence "+sentenceId);
			if ((sentenceTokenIndexMap.get(sentenceId).get(start) != null) || (sentenceTokenIndexMap.get(sentenceId).get(end) != null)) {
				throw new AnalysisEngineProcessException(YateaAE.LIPN_MESSAGE_DIGEST, "yatea_duplicate_tokens", new Object[]{});
			}
			sentenceTokenIndexMap.get(sentenceId).put(start, token);
			sentenceTokenIndexMap.get(sentenceId).put(end, token);
		}
		return sentenceTokenIndexMap;
	}

	
	protected static class YateaTokenIterator extends BasicTokenIterator {
		
		int countSentence;
		
		public YateaTokenIterator(JCas aJCas) {
			super(aJCas, null, true, false);
			countSentence = 0;
		}

		public int getSentenceId() {
			return countSentence;
		}

		
		protected Token endSentence(Annotation sentence) {
			countSentence++;
			return null;
		}
	}

}
