package fr.lipn.nlptools.uima.lia.ne;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.common.TokenByLineWriterFeeder;
import fr.lipn.nlptools.uima.lia.common.LiaTokenPOSLemmaIterator;
import fr.lipn.nlptools.uima.lia.common.LipnLiaExternalProgramGenericAnnotator;
import fr.lipn.nlptools.utils.externprog.ExternalProgram;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

public class LiaNETaggedTextAE extends LipnLiaExternalProgramGenericAnnotator {
	
	public final String AE_NAME = getDefaultComponentId();
	public static final String LIA_NE_ENCODING = LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENCODING;
	public static final String LIA_NE_LANG="french";
	public static final String ALLOWED_LANGUAGE="fr";
	public static final double LIA_NE_CONFIDENCE = LipnLiaExternalProgramGenericAnnotator.DEFAULT_LIA_CONFIDENCE;
	public static final String EXTERNAL_PROGRAM_NAME = "LIA_NE";
	public static final boolean deleteTempFilesAfterProcess = true;
	
	public static final String[] POSSIBLE_SCRIPT_INTERPRETERS = { "/bin/bash", "/bin/sh" };
	static final char[] ignoredChars = { '.', '_' };

	File liaNeDir;
	File crfPPDir;
	File scriptTempFile;
	File tempDir;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		int i = 0;
		while ((i<POSSIBLE_SCRIPT_INTERPRETERS.length) && (!new File(POSSIBLE_SCRIPT_INTERPRETERS[i]).exists())) {
			i++;
		}
		if (i>=POSSIBLE_SCRIPT_INTERPRETERS.length) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "no_interpreter_found", new Object[]{AE_NAME, POSSIBLE_SCRIPT_INTERPRETERS});
		}
		initCommonParameters(context, null,   POSSIBLE_SCRIPT_INTERPRETERS[i], EXTERNAL_PROGRAM_NAME, false);

		liaNeDir = new File((String) context.getConfigParameterValue(LIA_NE_DIR_PARAM_NAME));			
		if (!liaNeDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{liaNeDir.getAbsolutePath()});
		}		
		crfPPDir = new File((String) context.getConfigParameterValue("CRFPlusPlusDir"));			
		if (!crfPPDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{crfPPDir.getAbsolutePath()});
		}		

		if (context.getConfigParameterValue("TempDir") != null) {
			tempDir = new File((String) context.getConfigParameterValue("TempDir"));
		}
		if ((tempDir != null) && (!tempDir.isDirectory())) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{tempDir.getName()});
		}

		try {
			scriptTempFile = File.createTempFile("Lia_NE_AE_temp_script", null, tempDir);
			BufferedWriter out = new BufferedWriter(new FileWriter(scriptTempFile));
			out.write("#!"+POSSIBLE_SCRIPT_INTERPRETERS[i]+LINE_SEPARATOR+LINE_SEPARATOR);
			out.write(liaNeDir.getCanonicalPath()+DIR_SEPARATOR+"bin/fmt4crf |"+LINE_SEPARATOR);
			out.write(crfPPDir.getCanonicalPath()+DIR_SEPARATOR+"bin/crf_test -m"+liaNeDir.getCanonicalPath()+"/crf_data/model_ne |"+LINE_SEPARATOR);
			out.write(liaNeDir.getCanonicalPath()+DIR_SEPARATOR+"bin/tagg2text"+LINE_SEPARATOR);
			out.close();
		} catch (IOException e) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_write_temp_file", new Object[]{});
		}

	}

	public void process(JCas aJCas)  throws AnalysisEngineProcessException  {

		String language = ensureLanguage(null, aJCas);
		if (!language.equals(ALLOWED_LANGUAGE)) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "invalid_language", new Object[]{EXTERNAL_PROGRAM_NAME, language, ALLOWED_LANGUAGE});
		}
		
		ExternalProgram p = initProgram(aJCas, getProgramDir(), liaNeDir);
		try {
			String currentPathEnvVar = p.environment().get("PATH");
			p.environment().put("PATH", currentPathEnvVar+":"+crfPPDir.getCanonicalPath()+"/bin");
			p.getCommand().add(scriptTempFile.getAbsolutePath());
			p.setWorkingDir(liaNeDir.getCanonicalPath());
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unexpected_io_init_error", null, e);			
		}
		
		// main call
		WriterFeeder writerFeeder = new TokenByLineWriterFeeder( new LiaTokenPOSLemmaIterator(aJCas, true, false, true), LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_TAGS_SEPARATOR); 
		runProgram(aJCas, writerFeeder, new LiaNeAnnotatedReader(aJCas, this));
	}
	

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		if (deleteTempFilesAfterProcess && !scriptTempFile.delete()) {
			throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unable_delete", new Object[]{tempDir.getAbsolutePath()});
		}
	}
	
}
