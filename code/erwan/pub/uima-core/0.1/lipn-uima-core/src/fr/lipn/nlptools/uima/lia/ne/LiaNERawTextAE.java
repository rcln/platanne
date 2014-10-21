package fr.lipn.nlptools.uima.lia.ne;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.StringWriterFeeder;

public class LiaNERawTextAE extends LipnLiaExternalProgramGenericAnnotator {

	public static final String LIA_NE_RAW_TEXT_COMPONENT_ID = LiaNERawTextAE.class.getName();
	public static final String LIA_NE_ENCODING = LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENCODING;
	public static final String LIA_NE_LANG="french";
	public static final String ALLOWED_LANGUAGE="fr";
	public static final double LIA_NE_CONFIDENCE = LipnLiaExternalProgramGenericAnnotator.DEFAULT_LIA_CONFIDENCE;
	public static final String EXTERNAL_PROGRAM_NAME = "LIA_NE";
	
	static final String LIA_NE_EXEC_NAME = "script/ne_tagg.csh";

	File liaTaggDir;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		super.initCommonParameters(context, LIA_NE_DIR_PARAM_NAME, LIA_NE_EXEC_NAME, EXTERNAL_PROGRAM_NAME);
		
		liaTaggDir = new File((String) context.getConfigParameterValue(LIA_TAGG_DIR_PARAM_NAME));			
		if (!liaTaggDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{liaTaggDir.getAbsolutePath()});
		}		

	}
	
	
	public void process(JCas aJCas)  throws AnalysisEngineProcessException  {

		String language = ensureLanguage(null, aJCas);
		if (!language.equals(ALLOWED_LANGUAGE)) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{LIA_NE_RAW_TEXT_COMPONENT_ID, language, ALLOWED_LANGUAGE});
		}
		
		ExternalProgram p = initProgram(aJCas);
		try {
			p.environment().put(LipnLiaExternalProgramGenericAnnotator.LIA_NE_ENVVAR, getProgramDir().getCanonicalPath());
			p.environment().put(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENVVAR, liaTaggDir.getCanonicalPath());
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}

		
		// main call
		runProgram(aJCas, new StringWriterFeeder(aJCas.getDocumentText()), new LiaNeAnnotatedReader(aJCas, this));
	}
	
	

}
