package fr.lipn.nlptools.uima.lia.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.utils.align.FilterTokenAndAnnot;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;


/**
 * 
 * This class is intended to be a super-class for all LIA components.  
 * It simply provides a few protected methods that these componants should always call in their own methods, as well as 
 * a few constants which they should refer to whenever needed.
 *  
 * @author moreau
 *
 */
public abstract class LipnLiaExternalProgramGenericAnnotator extends LipnExternalProgramGenericAnnotator {
	
	public static final String LIA_NE_ENVVAR = "LIA_NE";
	public static final String LIA_TAGG_ENVVAR = "LIA_TAGG";
	public static final String LIA_TAGG_LANG_ENVVAR = "LIA_TAGG_LANG";
	/**
	 * Warning: LIA tools use ISO-8859-1 as charset encoding, thus errors can occur if
	 * the data contains unmappable characters for this encoding.
	 * 
	 */
	public static final String LIA_TAGG_ENCODING = "ISO-8859-1";
	public static final double DEFAULT_LIA_CONFIDENCE = LipnExternalProgramGenericAnnotator.DEFAULT_CONFIDENCE;
	
	static final int DEFAULT_CHARBUFFER_SIZE = 1024;
	

	public static final String LIA_TAGG_DIR_PARAM_NAME = "LiaTaggDir";
	public static final String LIA_NE_DIR_PARAM_NAME = "LiaNEDir";
	public static final String LIA_TAGG_SENTENCE_TAG_NAME="s";
	public static final String LIA_TAGG_SENTENCE_OPEN_TAG="<"+LIA_TAGG_SENTENCE_TAG_NAME+">";
	public static final String LIA_TAGG_SENTENCE_CLOSE_TAG="</"+LIA_TAGG_SENTENCE_TAG_NAME+">";
	public static final String LIA_TAGG_TAGS_SEPARATOR = " ";
	public static final String LIA_SENTENCE_SEPARATOR_POS_TAG = "ZTRM";
	public static final int LIA_MULTIWORDS_CHAR = '_';
	public static final String[] LIA_INVALID_CHARS_REGEXPS = null;
	public static final String LIA_TOKENIZED_TEXT_SEPARATOR_REGEX = "\\s+";

	
	public static final int LEXI_RESOURCE_ID = 0;
	public static final int LISTE_CHIF_RESOURCE_ID = 1;
	public static final int TRIGRAM_RESOURCE_ID = 2;
	public static final int MORPHO_RESOURCE_ID = 3;
	public static final int LEXITAB_RESOURCE_ID= 4;
	public static final int LEXTAG_RESOURCE_ID = 5;
	public static final int LEXGRAPH_RESOURCE_ID = 6;
	public static final int NB_RESOURCES_NAMES = 7;
	
	
	public static final Map<String, String> LIA_LANGUAGE_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("fr", "french");
        aMap.put("en", "english");
        LIA_LANGUAGE_MAP = Collections.unmodifiableMap(aMap);
    }

	
	String lang = null;
	boolean guessOption = true;
	String[] resourcesNames = new String[NB_RESOURCES_NAMES];
	

	/**
	 * Common initializations.
	 * if null, the programDirParamName will be set set to LIA_TAGG_DIR_PARAM_NAME. 
	 * 
	 */
	protected void initCommonParameters(UimaContext context, String programDirParamName, String programExecRelPath, String liaProgramUserFriendlyName, boolean execPathIsRelativeToProgramDir) throws ResourceInitializationException {

		super.initCommonParameters(context, (programDirParamName != null)?programDirParamName:LIA_TAGG_DIR_PARAM_NAME, programExecRelPath, liaProgramUserFriendlyName, execPathIsRelativeToProgramDir);
		
		// get optional parameters
		if (context.getConfigParameterValue("Language") != null) {
			lang = (String) context.getConfigParameterValue("Language");
			if (!LIA_LANGUAGE_MAP.containsKey(lang)) {
				throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{lang, "Language", "must be in :"+LIA_LANGUAGE_MAP.keySet().toString()});
			}
		}
						
	}
	

	protected void initCommonParameters(UimaContext context, String programDirParamName, String programExecRelPath, String liaProgramUserFriendlyName) throws ResourceInitializationException {
		initCommonParameters(context, programDirParamName, programExecRelPath, liaProgramUserFriendlyName, true);
	}
	
	
	/**
	 * if the LIA component can have the "-guess" option, use this method to take its value into account if possible (can be used only for french, will be simply ignored otherwise)
	 * The default variable value is true.
	 * @param context
	 */
	protected void setGuessOption(UimaContext context) {
		if (context.getConfigParameterValue("UseGuessOptionIfPossible") != null) {
			guessOption = ((Boolean) context.getConfigParameterValue("UseGuessOptionIfPossible")).booleanValue();
		}
	}
	
	
	protected ExternalProgram initProgram(JCas aJCas) throws AnalysisEngineProcessException {
		ExternalProgram program;
		
		String language = ensureLanguage(lang, aJCas);
		try {
			if (!setResourcesNames(language)) {
				throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_parameter", new Object[]{language, "Language", "must be in :"+LIA_LANGUAGE_MAP.keySet().toString()});
			}
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		
		try {
			program = initProgram(null, LIA_TAGG_ENCODING);
			// set required environment variables
			Map<String, String> env = program.environment();
			// WARNING: wrong for LIA NE, must be corrected after calling this method
			env.put(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENVVAR, getProgramDir().getCanonicalPath());
			env.put(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_LANG_ENVVAR, LIA_LANGUAGE_MAP.get(language));
		} catch (java.io.IOException e) { // should not happen since all directories/files have been tested.
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		return program;

	}
	


	/**
	 * NB: LIA TAGG '-guess' option is set by default for french.
	 * @return true normally, false if an error occured (unrecognized language)
	 */
	protected boolean setResourcesNames(String lang) throws java.io.IOException {
		resourcesNames[MORPHO_RESOURCE_ID] = "NULL";
		if (lang.equals("fr")) {
			resourcesNames[LEXI_RESOURCE_ID] = "data/lex80k.fr";
			resourcesNames[LISTE_CHIF_RESOURCE_ID] = "data/list_chif_virgule.fr.tab";
			resourcesNames[TRIGRAM_RESOURCE_ID] = "data/lm3class.fr.arpa";
			if (guessOption) {
				resourcesNames[MORPHO_RESOURCE_ID] = "data/model_morpho.fr";
			}
		} else if (lang.equals("en")) {
			resourcesNames[LEXI_RESOURCE_ID] = "data/lex150k.en";
			resourcesNames[LISTE_CHIF_RESOURCE_ID] = "data/list_chif_virgule.en.tab";			
			resourcesNames[TRIGRAM_RESOURCE_ID] = "data/lm3class.en.arpa";
		} else {
			return false;
		}
		resourcesNames[LEXITAB_RESOURCE_ID] = resourcesNames[LEXI_RESOURCE_ID]+".tab";
		resourcesNames[LEXTAG_RESOURCE_ID] = resourcesNames[TRIGRAM_RESOURCE_ID]+".sirlex";
		resourcesNames[LEXGRAPH_RESOURCE_ID] = resourcesNames[LEXI_RESOURCE_ID]+".sirlex";
		for (int i=0; i<resourcesNames.length; i++) {
			if (!resourcesNames[i].equals("NULL")) { // caution: special value "NULL", no prefix !
				resourcesNames[i] = getProgramDir().getCanonicalPath()+DIR_SEPARATOR+resourcesNames[i];
			}
		}
		return true;	
	}
	
	public String getResourceName(int idResource) {
		if ((resourcesNames != null) && (idResource < resourcesNames.length)) {
			return resourcesNames[idResource];
		} else {
			return null;
		}
	}
	
	/**
	 * This filter can be used with {@link SimpleTokenByLineReader} to deal with the special
	 * case of sentences start/end tags: when such a token is encoutered, it is transformed
	 * into an annotation.
	 * 
	 * @author moreau
	 *
	 */
	public static class LiaInputFilter implements FilterTokenAndAnnot {
		
		public String filteredToken(String token, String annot) {
			if ((token != null) && (token.equals(LIA_TAGG_SENTENCE_OPEN_TAG) || token.equals(LIA_TAGG_SENTENCE_CLOSE_TAG))) {
				return null;
			} else {
				return token;
			}
		}

		public String filteredAnnot(String token, String annot) {
			if ((token != null) && (token.equals(LIA_TAGG_SENTENCE_OPEN_TAG) || token.equals(LIA_TAGG_SENTENCE_CLOSE_TAG))) {
				return token;
			} else {
				return annot;
			}
		}
		
		public boolean sendTokenAndAnnot() {
			return true;
		}
		
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String language) {
		this.lang = language;
	}

	

}