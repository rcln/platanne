package fr.lipn.nlptools.uima.tagen;


import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.NamedEntity;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.SimpleInputReader;
import fr.lipn.nlptools.utils.align.SimpleTaggedTextReader;
import fr.lipn.nlptools.utils.align.TagPositionConsumer;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * This AE encapsulates TagEN, a Named Entities Recognizer (with default configs for French and English).
 * TagEN 2.0 script is called using an {@link fr.lipn.nlptools.utils.externprog.ExternalProgram} object, and TagEN output is processed using the
 *  {@link fr.lipn.nlptools.utils.align} package (to recover NEs positions).<br/>
 *  
 *  Errors are carefully processed, though bugs are always possible (sic!): detailed UIMA exceptions are thrown whenever possible,
 *  even when the underlying TagEN process fails (in this case stderr is copied in the exception message).<br/>
 *  
 *  A time out option is provided, in order to kill the underlying process after a certain amount of time. Of course it should be
 *  used with care, since the time needed to process a document depends on a lot of parameters (set to 0 by default, meaning no limit).<br/>
 *  Temporary files/directory are created "in the safer way", using Java specs mechanism to manage location and filenames. TagEN 2.0 is also
 *  safe about temporary files, therefore no conflict should arise. An option is also provided to force temporary data location. Finally a
 *  debug option is provided, which is also transmitted to TagEN, and permits to keep temporary files. It can be used to observe the 
 *  intermediate steps of the process. 
 *  
 * 
 * Information: currently this AE requires that the text does not contain any "&lt" character, because it is the opening tag mark.
 * "&gt" are allowed. 
 * 
 * @author moreau
 *
 */
public class TagenAE extends LipnExternalProgramGenericAnnotator  {


	/**
	 * 
	 * TagEN encoding (UTF-16 Little Endian with BOM).
	 * 
	 * Information about encoding: TagEN relies on Unitex, which requires Unicode UTF-16 Little Endian encoding, WITH a mandatory Byte Order Mark (BOM)... But this very
	 * precise requirement is quite unusual for the current Java Specs: Java "New I/O" package (java.nio) is not even able to write such encoding 
	 * (though it is able to read it). However the "old" java.io and java.lang packages can handle this encoding, using the canonical name
	 * <code>UnicodeLittle</code> (which will write a BOM, contrary to UTF16-LE).<br/>
	 * Notice that the "iconv" utility seems to always write a BOM when using UTF-16 (I did not check precisely).<br/>
	 * <br/>  
	 * Related links: 
	 * <ul>
	 * <li> about Unicode standards and Byte Order Mark: 
	 * <li>{@link <a href="http://unicode.org/faq/utf_bom.html">http://unicode.org/faq/utf_bom.html</a>}</li>
	 * <li>{@link <a href="http://unicode.org/faq/utf_bom.html#BOM">http://unicode.org/faq/utf_bom.html#BOM</a>}</li>
	 * </li>
	 * <li> about Java Spec charset encodings:
	 * <li>{@link <a href="http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html">http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html</a>}</li>
	 * <li>{@link <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/Charset.html">http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/Charset.html</a>}</li>
	 * </li>
	 * </li>
	 * </ul>
	 */
	public static final String TAGEN_ENCODING = "UnicodeLittle";
	public static final String EXTERNAL_PROGRAM_NAME = "TagEN";
	public static final String TAGEN_CATEGORY_SEPARATOR = "/";
	
	static final String DIR_SEPARATOR = System.getProperty("file.separator");
	static final String TAGEN_EXEC_NAME = "tagen";
	
	static final String TAGEN_SILENT_OPTION = "-s";
	static final String TAGEN_DEBUG_OPTION = "-d";
	static final String TAGEN_TEMPDIR_OPTION = "-w";
	static final String TAGEN_UNITEXDIR_OPTION = "-u";
	static final String TAGEN_TAGENDIR_OPTION = "-t";
	static final String TAGEN_RESOURCESDIR_OPTION = "-r";

	public static final Map<String, String> TAGEN_LANGUAGE_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("fr", "fr");
        aMap.put("en", "eng");
        TAGEN_LANGUAGE_MAP = Collections.unmodifiableMap(aMap);
    }

	File unitexDir;
	File tagenConfig;
	File resourcesDir;
	boolean debugMode = false;
	File tempDir;
	String lang;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		initCommonParameters(context, "TagenDir", TAGEN_EXEC_NAME, EXTERNAL_PROGRAM_NAME);


		// get optional parameters
		if (context.getConfigParameterValue("TagenConfig") != null) {
			tagenConfig = new File((String) context.getConfigParameterValue("TagenConfig"));
		}
		if (context.getConfigParameterValue("UnitexDir") != null) {
		    unitexDir = new File((String) context.getConfigParameterValue("UnitexDir"));
		}
		if (context.getConfigParameterValue("TagenResourcesDir") != null) {
			resourcesDir = new File((String) context.getConfigParameterValue("TagenResourcesDir"));
		}
		if (context.getConfigParameterValue("DebugMode") != null) {
			debugMode = ((Boolean) context.getConfigParameterValue("DebugMode")).booleanValue();
		}
		if (context.getConfigParameterValue("TempDir") != null) {
			tempDir = new File((String) context.getConfigParameterValue("TempDir"));
		}
		if (context.getConfigParameterValue("Language") != null) {
			lang = (String) context.getConfigParameterValue("Language");
			if (!TAGEN_LANGUAGE_MAP.containsKey(lang)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{lang, "Language", TAGEN_LANGUAGE_MAP.keySet().toString()});
			}
		}

		// check directories
		if ((unitexDir != null) && (!unitexDir.isDirectory())) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{unitexDir.getName()});
		}
		if ((resourcesDir != null) && (!resourcesDir.isDirectory())) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{resourcesDir.getName()});
		}
		if ((tempDir != null) && (!tempDir.isDirectory())) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{tempDir.getName()});
		}
		if ((tagenConfig != null) && !tagenConfig.exists()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{tagenConfig.getName()});
		}

	}
	
	@Override
	public void process(JCas aJCas)  throws AnalysisEngineProcessException  {

		// ensure language is set
		String language = ensureLanguage(lang, aJCas, TAGEN_LANGUAGE_MAP);
		
		// prints current CAS text to a temporary file
		File tempInputFile, tempOutputFile;
		try {
			tempInputFile = File.createTempFile("TagenAE_Input", null, tempDir);  // NB: if tempDir is null the default system temp dir is used
			tempOutputFile = File.createTempFile("TagenAE_Output", null, tempDir);
			OutputStreamWriter out = new OutputStreamWriter(new java.io.FileOutputStream(tempInputFile), TAGEN_ENCODING);
 	    	out.write(aJCas.getDocumentText());
	    	out.close();
		} catch (java.io.IOException e) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_write_temp_file", null, e);			
		}
		
		// build TagEN command
		ExternalProgram tagenProgram = initProgram();
		List<String> tagenCommand = tagenProgram.getCommand();
		try {
			tagenCommand.add(TAGEN_TAGENDIR_OPTION);
			tagenCommand.add(getProgramDir().getCanonicalPath());
			if (unitexDir != null) {
			    tagenCommand.add(TAGEN_UNITEXDIR_OPTION);
			    tagenCommand.add(unitexDir.getCanonicalPath());
			}
			if (tempDir != null) {
				tagenCommand.add(TAGEN_TEMPDIR_OPTION);
				tagenCommand.add(tempDir.getCanonicalPath());			
			}
			if (resourcesDir != null) {
				tagenCommand.add(TAGEN_RESOURCESDIR_OPTION);
				tagenCommand.add(resourcesDir.getCanonicalPath());
			}
			if (debugMode) {
				tagenCommand.add(TAGEN_DEBUG_OPTION);
			}
			tagenCommand.add(TAGEN_SILENT_OPTION);
			if (tagenConfig != null) {
				tagenCommand.add(tagenConfig.getCanonicalPath());
			} else  {
				tagenCommand.add(language);
			}
			tagenCommand.add(tempInputFile.getCanonicalPath());
			tagenCommand.add(tempOutputFile.getCanonicalPath());
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}

		// run program and deal with error
		// stdout is ignored (should be empty because of -s option)
		runProgram(aJCas, null, null);
		
		// parse the resulting file to add annotations
		try {
			SimpleTaggedTextReader aligner = new SimpleTaggedTextReader(new InputStreamReader(new FileInputStream(tempOutputFile.getCanonicalPath()), TAGEN_ENCODING));
			aligner.setAcceptClosingTagCharAlone(true); // little quite dirty point: allows isolated '>' characters, since TagEN does not care about that. 
			aligner.align(new SimpleInputReader(new java.io.StringReader(aJCas.getDocumentText())), new TagPositionConsumer(new tagenAnnotReceiver(aJCas)));
		} catch (java.io.IOException ioe) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, ioe);
		} catch (AlignmentException ae) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "alignment_error", new Object[]{EXTERNAL_PROGRAM_NAME}, ae);
		} finally {
			// remove temporary files
			if (!debugMode) {
				tempInputFile.delete();
				tempOutputFile.delete();
			} else {
				getContext().getLogger().log(Level.INFO, "DEBUG MODE: temporary input file '"+tempInputFile.getAbsolutePath()+"' not deleted.");
				getContext().getLogger().log(Level.INFO, "DEBUG MODE: temporary output file '"+tempOutputFile.getAbsolutePath()+"' not deleted.");
			}
		}			
		
		
	}


	protected class tagenAnnotReceiver implements TagPositionConsumer.AnnotationReceiver {
		
		JCas aJCas;
		ArrayList<String> prevAnnots;
		long prevStart, prevEnd = 0;
		
		public tagenAnnotReceiver(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void initPositionConsumer() throws AlignmentException {
			prevAnnots = new ArrayList<String>();
		}

		/**
		 * Called by the position consumer (final step of the re-alignment process)
		 * Important: for some configs (e.g. fr), TagEN sets two tags for each NE (a general one and a detailed one),
		 *  and we don't want to set several different annotations to each NE. That is why <code>prevAnnots</code> is used:
		 *  consecutive annotations concerning the same pair of positions are assigned to only one "UIMA annotation".
		 */
		public void addAnnotation(String content, long start, long end) throws AlignmentException {
//			System.out.println("debug: receiving '"+content+"', pos "+start+"-"+end);
			if (prevAnnots.isEmpty()) {
				prevAnnots.add(content);
				prevStart = start;
				prevEnd = end;
			} else {
				if ((start == prevStart) && (end == prevEnd)) { // check whether the position concerns the same text as previous annotation(s)
					prevAnnots.add(content);
				} else {
					NamedEntity ne = new NamedEntity(aJCas);
					setGenericAttributes(ne, (int) prevStart, (int) prevEnd); 
//					ne.setCategory(prevAnnots.get(0));
					StringBuilder allTags = new StringBuilder(prevAnnots.get(0));
					for (int i=1; i<prevAnnots.size(); i++) {
						allTags.insert(0, prevAnnots.get(i)+TAGEN_CATEGORY_SEPARATOR);
					}
					ne.setValue(allTags.toString());
					
					ne.addToIndexes();
					prevAnnots.clear();
					prevAnnots.add(content);
					prevStart = start;
					prevEnd = end;
				}
			}
		}
		
		public void addUnannotatedToken(String inputToken, String annotatedToken, long start, long end) throws AlignmentException {
		}
		

		public void closePositionConsumer() throws AlignmentException {
			NamedEntity ne = new NamedEntity(aJCas);
			setGenericAttributes(ne, (int) prevStart, (int) prevEnd); 
			StringBuilder allTags = new StringBuilder(prevAnnots.get(0));
			for (int i=1; i<prevAnnots.size(); i++) {
				allTags.insert(0, prevAnnots.get(i)+TAGEN_CATEGORY_SEPARATOR);
			}
			ne.setValue(allTags.toString());
			ne.addToIndexes();			
		}
		

	}



}
