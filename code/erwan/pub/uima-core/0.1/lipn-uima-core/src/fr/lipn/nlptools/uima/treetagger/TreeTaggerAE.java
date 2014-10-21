package fr.lipn.nlptools.uima.treetagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import fr.lipn.nlptools.uima.common.BasicPositionedTextTokenIterator;
import fr.lipn.nlptools.uima.common.BasicTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.types.Interpretation;
import fr.lipn.nlptools.uima.types.Lemma;
import fr.lipn.nlptools.uima.types.PartOfSpeech;
import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.utils.misc.PositionedText;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.AnnotatedTextReader;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.align.TokenIteratorInputReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder;

/**
 * TODO doc
 *
 *  TODO : lemma = unknown => pas d'annot, à mettre dans la doc!
 * 
 * @author moreau
 *
 */
public class TreeTaggerAE extends LipnExternalProgramGenericAnnotator {
	
	public final String AE_NAME = getDefaultComponentId();

	public static final String TT_DEFAULT_ENCODING = "UTF-8";
	public static final String TT_EXEC_NAME = "bin/tree-tagger";


	public static final String TREETAGGER_LEXICON_OPTION = "-lex";
	public static final String TREETAGGER_LEMMA_OPTION = "-lemma";
	public static final String TREETAGGER_HYPHEN_HEURISTICS_OPTION = "-hyphen-heuristics";
	public static final String TREETAGGER_PROB_OPTION = "-prob";
	public static final String TREETAGGER_TOKEN_OPTION = "-token";
	public static final String TREETAGGER_THRESHOLD_OPTION = "-threshold";
	public static final String TREETAGGER_QUIET_OPTION = "-quiet";
	public static final String TREETAGGER_UNKNOWN_LEMMA_TAG = "<unknown>";
	public static final String TREETAGGER_CAP_HEURISTICS = "-cap-heuristics";
	
	public static final String TREETAGGER_END_SENTENCE_MARK_POS = "SENT";
	
	/**
	 * A regexp to remove all characters that are not valid in a token for TT. List found in "TreeTagger for Java" package (tt4j) 
	 *  by Richard Eckart de Castilho.
	 * 
	 */
	public static final String TREETAGGER_INVALID_CHARS_REGEXP = "[\\n\\t\\r\\u200E\\u200F\\u2028\\u2029]+";

	public static final Map<String, String> TREETAGGER_LANGUAGE_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("fr", "french");
        aMap.put("en", "english");
        aMap.put("it", "italian");
        aMap.put("gr", "greek");
        aMap.put("de", "german");
        aMap.put("es", "madrid");
        aMap.put("bg", "bulgarian");
        aMap.put("du", "dutch");
        TREETAGGER_LANGUAGE_MAP = Collections.unmodifiableMap(aMap);
    }
    public static final String PARAMETER_FILE_PREFIX = "lib/";
    public static final String PARAMETER_FILE_SUFFIX_LATIN1 = ".par";
    public static final String PARAMETER_FILE_SUFFIX_UTF8 = "-utf8.par";
    public static final boolean TRY_OTHER_ENCODING_IF_PARAM_FILE_DOES_NOT_EXIST = true;
    public static final String TREETAGGER_COLUMN_SEPARATOR = "\t";
    public static final char TREETAGGER_TAGS_VERSIONS_SEPARATOR = ' ';  // this separator is used between a set of "items", e.g. POS_TAG [LEMMA_TAG] PROBA
    
    public static final Map<String, String> CONVERSE_ENCODING_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("UTF-8", "ISO-8859-1");
        aMap.put("ISO-8859-1", "UTF-8");
        CONVERSE_ENCODING_MAP = Collections.unmodifiableMap(aMap);
    }

    
	File parameterFile;
	String lang;
	String encoding = TT_DEFAULT_ENCODING;
	boolean lemmatize;
	float multiTagsProbThreshold = -1;
	boolean capHeuristics;
	boolean hyphenHeuristics;
	File lexiconFile;
	boolean doNotCompareTokensInOutput;

	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initCommonParameters(context, "TreeTaggerDir", TT_EXEC_NAME, AE_NAME);
		
		if (((Boolean) context.getConfigParameterValue("UseLatin1EncVersion")).booleanValue()) {
			encoding = "ISO-8859-1";
		}

		lemmatize = ((Boolean) context.getConfigParameterValue("Lemmatize")).booleanValue(); 
		hyphenHeuristics = ((Boolean) context.getConfigParameterValue("HyphenHeuristics")).booleanValue(); 
		capHeuristics = ((Boolean) context.getConfigParameterValue("CapHeuristics")).booleanValue(); 
		doNotCompareTokensInOutput  = ((Boolean) context.getConfigParameterValue("DoNotCompareTokensInOutput")).booleanValue();
		if (context.getConfigParameterValue("Language") != null) {
			lang = (String) context.getConfigParameterValue("Language");
			if (!TREETAGGER_LANGUAGE_MAP.containsKey(lang)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{lang, AE_NAME, TREETAGGER_LANGUAGE_MAP.keySet().toString()});
			}
		}
		if (context.getConfigParameterValue("ParameterFile") != null) {
			parameterFile = new File((String) context.getConfigParameterValue("ParameterFile"));
		}
		if (context.getConfigParameterValue("LexiconFile") != null) {
			lexiconFile = new File((String) context.getConfigParameterValue("LexiconFile"));
		}
		if (context.getConfigParameterValue("MultiTagsProbThreshold") != null) {
			multiTagsProbThreshold = ((Float) context.getConfigParameterValue("MultiTagsProbThreshold")).floatValue();
			if ((multiTagsProbThreshold < 0) || (multiTagsProbThreshold > 1)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{multiTagsProbThreshold, "MultiTagProbThreshold", "must be in [0,1]"});
			}
			if (multiTagsProbThreshold == 0) {
				multiTagsProbThreshold = Float.MIN_VALUE;
			}
		}
		

		// check file(s)
		if ((parameterFile != null) && !parameterFile.exists()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{parameterFile.getName()});
		}		
		if ((lexiconFile != null) && !lexiconFile.exists()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "file_does_not_exist", new Object[]{lexiconFile.getName()});
		}		

		
		
	}
	
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		// ensure language is set
		String language = ensureLanguage(lang, aJCas);
		if (!TREETAGGER_LANGUAGE_MAP.containsKey(language)) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{language, AE_NAME, TREETAGGER_LANGUAGE_MAP.keySet().toString()});
		}
		
		// build command
		ExternalProgram program = initProgram(null, encoding);
		List<String> command = program.getCommand();
		try {
			if (parameterFile == null) {
				
				parameterFile = new File(getProgramDir().getCanonicalPath()+DIR_SEPARATOR+PARAMETER_FILE_PREFIX+TREETAGGER_LANGUAGE_MAP.get(language)+(encoding=="UTF-8"?PARAMETER_FILE_SUFFIX_UTF8:PARAMETER_FILE_SUFFIX_LATIN1));
				if (TRY_OTHER_ENCODING_IF_PARAM_FILE_DOES_NOT_EXIST && (!parameterFile.exists())) {
					File tryOtherEncParamFile = new File(getProgramDir().getCanonicalPath()+DIR_SEPARATOR+PARAMETER_FILE_PREFIX+TREETAGGER_LANGUAGE_MAP.get(language)+(encoding!="UTF-8"?PARAMETER_FILE_SUFFIX_UTF8:PARAMETER_FILE_SUFFIX_LATIN1));
					if (tryOtherEncParamFile.exists()) {
						getContext().getLogger().log(Level.WARNING, "No parameter file for language '+language+' encoded as '"+encoding+"', using '"+CONVERSE_ENCODING_MAP.get(encoding)+"' (file is '"+tryOtherEncParamFile.getCanonicalPath()+"').");
						encoding = CONVERSE_ENCODING_MAP.get(encoding);
						parameterFile = tryOtherEncParamFile;
					}
				}
				if (!parameterFile.exists()) {
					throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "no_config_file_found", new Object[]{AE_NAME, parameterFile.getCanonicalPath()});
				}
			}
			command.add(TREETAGGER_QUIET_OPTION);
			command.add(TREETAGGER_TOKEN_OPTION);
			if (lemmatize) {
				command.add(TREETAGGER_LEMMA_OPTION);
			}
			if (hyphenHeuristics) {
				command.add(TREETAGGER_HYPHEN_HEURISTICS_OPTION);
			}
			if (capHeuristics) {
				command.add(TREETAGGER_CAP_HEURISTICS);
			}
			command.add(TREETAGGER_PROB_OPTION);
			command.add(TREETAGGER_THRESHOLD_OPTION);
			if (multiTagsProbThreshold != -1) {
				command.add(""+multiTagsProbThreshold);
			} else {
				// the option is set in order to get the associated probability with the single (best) tag, the other ones will be ignored
				command.add(""+Float.MIN_VALUE);
			}
			if (lexiconFile != null) {
				command.add(TREETAGGER_LEXICON_OPTION);
				command.add(lexiconFile.getCanonicalPath());
			}
			command.add(parameterFile.getCanonicalPath());
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		
		// run program and deal with errors
		WriterFeeder ttWriterFeeder = new TokenByLineWriterFeeder(new BasicTokenPOSLemmaIterator(aJCas, false, false, new String[]{ TREETAGGER_INVALID_CHARS_REGEXP }, null, true), TREETAGGER_COLUMN_SEPARATOR);
		runProgram(aJCas, ttWriterFeeder, new TreeTaggerAnnotReader(aJCas));


	}


	
	
	protected class TreeTaggerAnnotReader implements ExternalProgram.ReaderConsumer {

		JCas aJCas;

		public TreeTaggerAnnotReader(JCas aJCas) {
			this.aJCas = aJCas;
		}
		
		public void consumeReader(Reader reader) throws ExternalProgramException {
			try {
				AnnotatedTextReader aligner = new SimpleTokenByLineReader(new BufferedReader(reader), TREETAGGER_COLUMN_SEPARATOR);
				Iterator<PositionedText> tokenIterator = new BasicPositionedTextTokenIterator(aJCas, new String[]{ TREETAGGER_INVALID_CHARS_REGEXP }, null, true);
				aligner.align(new TokenIteratorInputReader(tokenIterator, doNotCompareTokensInOutput,
						              (isCodingErrorActionReplace()?new char[]{getCodingErrorReplacementValue()}:null)
						           ),
						      new TreeTaggerConsumer(aJCas));
			} catch (AlignmentException ale) {
				throw new ExternalProgramException("An alignment error occured (see details in the stack trace).", ale);
			}
		}
		
	}


	
	protected class TreeTaggerConsumer implements AlignerConsumer {
		
		JCas aJCas;
		ArrayList<Annotation> annotsToAdd;
		Pattern separatingPattern;
		int currentSentenceStart = -1;
		int lastTokenEnd = -1;
		
		public TreeTaggerConsumer(JCas aJCas) {
			this.aJCas = aJCas;
			annotsToAdd = new ArrayList<Annotation>();
		}
				
		public void initConsumer() throws AlignmentException {
			separatingPattern = Pattern.compile(TREETAGGER_COLUMN_SEPARATOR);

		}
		
		public void closeConsumer() throws AlignmentException {
			// "closing" the last sentence
			// - if it was not already done when the last token was received (a SENT)
			// - and at least one token has been found (sic)
			if ((currentSentenceStart != -1) && (lastTokenEnd != -1)) {
				Sentence s = new Sentence(aJCas);
				setGenericAttributes(s, currentSentenceStart, (int) lastTokenEnd);
				annotsToAdd.add(s);
			}
			for (Annotation e: annotsToAdd) {
				e.addToIndexes();
			}
			getContext().getLogger().log(Level.INFO,""+annotsToAdd.size()+" annotations have been written.");
		}


		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			getContext().getLogger().log(Level.FINEST,"Receiving token '"+inputToken+"' and annotation '"+annotation+"' ("+startPos+"-"+endPos+")");
			if (annotation != null) {
				lastTokenEnd = (int) endPos;
				if (currentSentenceStart == -1) { // starting a sentence
					currentSentenceStart = (int) startPos;
				}
				String[] versions = separatingPattern.split(annotation);
				int nbVersions = versions.length;
				if (nbVersions == 0) {
					throw new AlignmentException("Can not parse the annotation found: '"+annotation+"' (for token '"+inputToken+"')");
				}
				// if the user did not ask for the "multiTagProbThreshold" option, take only into account the better prob annotation (first one)
				if (multiTagsProbThreshold == -1) {
					nbVersions = 1;
				}
				for (int i = 0; i < nbVersions; i++) {
					Interpretation interp = null;
					PartOfSpeech pos = null;
					Lemma l = null;
					if (nbVersions > 1) {
						synchronized (aJCas) {
							interp = new Interpretation(aJCas);
							interp.setSerie(new FSArray(aJCas, 2));
						}
					}
					int indexProb = versions[i].lastIndexOf(TREETAGGER_TAGS_VERSIONS_SEPARATOR) +1;
					if (indexProb == -1) {
						throw new AlignmentException("Error parsing annotation '"+annotation+"' (for token '"+inputToken+"'): element '"+versions[i]+"' is not valid.");						
					}
					float prob = Float.parseFloat(versions[i].substring(indexProb));
					int indexEndPos = versions[i].indexOf(TREETAGGER_TAGS_VERSIONS_SEPARATOR); // can not be -1: at least one such char exists, otherwise an exception has already been thrown
					String posValue = versions[i].substring(0, indexEndPos);
					synchronized (aJCas) {
						pos = new PartOfSpeech(aJCas);
						setGenericAttributes(pos, (int) startPos, (int) endPos, prob);
						pos.setValue(posValue);
						if (posValue.equals(TREETAGGER_END_SENTENCE_MARK_POS)) {
							Sentence s = new Sentence(aJCas);
							setGenericAttributes(s, currentSentenceStart, (int) lastTokenEnd);
							annotsToAdd.add(s);
							getContext().getLogger().log(Level.FINEST,"new Sentence (receiving token '"+currentSentenceStart+"') at "+startPos+"-"+lastTokenEnd+".");
							currentSentenceStart = -1;
						}
						getContext().getLogger().log(Level.FINEST,"new POS for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+posValue+"'");
						annotsToAdd.add(pos);
						if (interp != null) {
							interp.setSerie(0, pos);
						}
					}
					if (lemmatize) { // option "lemmatize" is set, and lemma is not "unknown"
						if (indexEndPos == indexProb-1) {
							throw new AlignmentException("Error parsing annotation '"+annotation+"' (for token '"+inputToken+"'): element '"+versions[i]+"' is not valid (expecting 3 items).");						
						}
						String lemmaValue = versions[i].substring(indexEndPos+1, indexProb -1);
						if (!lemmaValue.equals(TREETAGGER_UNKNOWN_LEMMA_TAG)) {
							synchronized (aJCas) {
								l = new Lemma(aJCas); 
								setGenericAttributes(l, (int) startPos, (int) endPos, prob);
								l.setValue(lemmaValue);
								getContext().getLogger().log(Level.FINEST,"new Lemma for token '"+inputToken+"' ("+startPos+"-"+endPos+"): '"+lemmaValue+"'");
							}
							annotsToAdd.add(l);
							if (interp != null) {
								interp.setSerie(1, l);
							}
						}
					} else {  // (a quite useless test: checks whether there are no other item between POS and probability)
						if (indexEndPos != indexProb-1) {
							throw new AlignmentException("Error parsing annotation '"+annotation+"' (for token '"+inputToken+"'): element '"+versions[i]+"' is not valid (expecting only 2 items).");						
						}						
					}
					if (interp != null) {
						setGenericAttributes(interp, (int) startPos, (int) endPos, prob);
						annotsToAdd.add(interp);
						getContext().getLogger().log(Level.FINEST,"new Interpretation (pos/lemma) for token '"+inputToken+"' ("+startPos+"-"+endPos+"): ("+((interp.getSerie(0)==null)?"null":((PartOfSpeech) interp.getSerie(0)).getValue())+"/"+((interp.getSerie(1)==null)?"null":((Lemma) interp.getSerie(1)).getValue())+")");
					}
				}
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
