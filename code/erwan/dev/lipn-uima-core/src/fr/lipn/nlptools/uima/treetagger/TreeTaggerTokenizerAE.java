package fr.lipn.nlptools.uima.treetagger;
        
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.Token;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.AnnotatedTextReader;
import fr.lipn.nlptools.utils.align.SimpleInputReader;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ReaderConsumer;
import fr.lipn.nlptools.utils.externprog.StringWriterFeeder;

/**
 * This AE is a wrapper for the TreeTagger standard tokenizer. French, English, Italian.
 * Actually the tokenization performed is often quite poor. The <code>DontSplitTokensFile</code>
 * option can be used to tell the tokenizer not to break some sequences of words.
 * 
 * 
 * @author moreau
 *
 */
public class TreeTaggerTokenizerAE extends LipnExternalProgramGenericAnnotator {
	
	public static final String TT_TOKENIZER_COMPONENT_ID = TreeTaggerTokenizerAE.class.getName();

	public static final float TT_TOKENIZER_CONFIDENCE =  LipnExternalProgramGenericAnnotator.DEFAULT_CONFIDENCE;
	public static final String EXTERNAL_PROGRAM_NAME = "TreeTagger tokenizer";
	
	public static final String TT_DEFAULT_ENCODING = "UTF-8";
	static final String TT_EXEC_NAME_LATIN1 = "cmd/tokenize.pl";
	static final String TT_EXEC_NAME_UTF8 = "cmd/utf8-tokenize.perl";
	
	static final String TT_DONTSPLITTOKENSFILE_OPTION = "-a";

	public static final Map<String, String> TT_TOKENIZER_LANGUAGE_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("fr", "-f");
        aMap.put("en", "-e");
        aMap.put("it", "-i");
        TT_TOKENIZER_LANGUAGE_MAP = Collections.unmodifiableMap(aMap);
    }

    
    
	File dontSplitTokensFile = null;
	String lang;
	String encoding = TT_DEFAULT_ENCODING;
	String execName = TT_EXEC_NAME_UTF8;
	boolean looseMatchingAligner;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		if (((Boolean) context.getConfigParameterValue("UseLatin1EncVersion")).booleanValue()) {
			encoding = "ISO-8859-1";
			execName = TT_EXEC_NAME_LATIN1;
		}
		initCommonParameters(context, "TreeTaggerDir", execName, EXTERNAL_PROGRAM_NAME);

		// get mandatory parameters
		if (context.getConfigParameterValue("DontSplitTokensFile") != null) {
			dontSplitTokensFile = new File((String) context.getConfigParameterValue("DontSplitTotkensFile"));
		}
		looseMatchingAligner = ((Boolean) context.getConfigParameterValue("LooseMatchingAligner")).booleanValue();
		// get optional parameters
		if (context.getConfigParameterValue("Language") != null) {
			lang = (String) context.getConfigParameterValue("Language");
			if (!TT_TOKENIZER_LANGUAGE_MAP.containsKey(lang)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{lang, EXTERNAL_PROGRAM_NAME, TT_TOKENIZER_LANGUAGE_MAP.keySet().toString()});
			}
		}

		// check file
		if ((dontSplitTokensFile != null) && !dontSplitTokensFile.exists()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{dontSplitTokensFile.getName()});
		}		
		
	}
	
	public void process(JCas aJCas)  throws AnalysisEngineProcessException  {

		// ensure language is set
		String language = ensureLanguage(lang, aJCas, null);
		if (TT_TOKENIZER_LANGUAGE_MAP.containsKey(language)) {
			language = TT_TOKENIZER_LANGUAGE_MAP.get(language);
		} else {
		//	throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{language, EXTERNAL_PROGRAM_NAME, TT_TOKENIZER_LANGUAGE_MAP.keySet().toString()});
			getContext().getLogger().log(Level.WARNING, "No specific option for language '"+language+"' in TreeTagger tokenizer.");
			language = null;
		}
		
		// build command
		ExternalProgram program = initProgram(null, encoding);
		List<String> command = program.getCommand();
		try {
			if (language != null) {
				command.add(language);
			}
			if (dontSplitTokensFile != null) {
				command.add(TT_DONTSPLITTOKENSFILE_OPTION);
				command.add(dontSplitTokensFile.getCanonicalPath());
				
			}
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}

		// run program and deal with errors
		runProgram(aJCas, new StringWriterFeeder(aJCas.getDocumentText()), new TTTokenizerAnnotReader(aJCas));
		
	}


	protected class TTTokenizerAnnotReader implements ReaderConsumer {

		JCas aJCas;
		
		public TTTokenizerAnnotReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			try {
				AnnotatedTextReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), TreeTaggerAE.TREETAGGER_COLUMN_SEPARATOR);
				SimpleInputReader inputReader = new SimpleInputReader(new java.io.StringReader(aJCas.getDocumentText()), null, 
						(isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null), -1, looseMatchingAligner );
				inputReader.setStoreWarnings(true);
				aligner.align(inputReader, 
						new TTTokenizerConsumer(aJCas));
				// obtain warnings about skipped chars if any
				if (looseMatchingAligner) {
					for (String w : inputReader.getWarnings()) {
						getContext().getLogger().log(Level.WARNING, w);
					}
				}
			} catch (AlignmentException ae) {
				throw new ExternalProgramException("An alignment error occured.", ae);
			}

		}
		
	}


	
	protected class TTTokenizerConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<Annotation> annotsToAdd;
		
		public TTTokenizerConsumer(JCas aJCas) {
			this.aJCas = aJCas;
			annotsToAdd = new ArrayList<Annotation>();
		}
				
		public void initConsumer() throws AlignmentException {
		}
		
		public void closeConsumer() throws AlignmentException {
			for (Annotation e: annotsToAdd) {
				e.addToIndexes();
			}
			getContext().getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");
		}


		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if (annotation != null) { //should never happen 
				throw new AlignmentException("Aligner consumer for "+EXTERNAL_PROGRAM_NAME+" receives unexpected data as annotation: "+annotation+" (maybe some special token containing whitespaces?)");
			} else { // simple tokens
				Token t = new Token(aJCas);
				setGenericAttributes(t, (int) startPos, (int) endPos);
				getContext().getLogger().log(Level.FINEST,"new Token '"+inputToken+"' ("+startPos+"-"+endPos+")");
				annotsToAdd.add(t);				
			}
		}

		public boolean requiresPosition() {
			return true;
		}

		public boolean requiresSeparator() {
			return false;
		}

		public boolean requiresUnannotatedTokens() {
			return true;
		}

	}


}
