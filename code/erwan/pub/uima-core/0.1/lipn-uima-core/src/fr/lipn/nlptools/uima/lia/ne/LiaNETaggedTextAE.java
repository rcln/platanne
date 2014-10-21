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
import fr.lipn.nlptools.utils.externprog.ExternalProgram.WriterFeeder;

public class LiaNETaggedTextAE extends LipnLiaExternalProgramGenericAnnotator {
	
	public final String AE_NAME = getDefaultComponentId();
	public static final String LIA_NE_ENCODING = LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENCODING;
	public static final String LIA_NE_LANG="french";
	public static final String ALLOWED_LANGUAGE="fr";
	public static final double LIA_NE_CONFIDENCE = LipnLiaExternalProgramGenericAnnotator.DEFAULT_LIA_CONFIDENCE;
	public static final String EXTERNAL_PROGRAM_NAME = "LIA_NE";
	public static final String LIA_NE_SCRIPT_COMMANDS = "#!/bin/bash\nbin/fmt4crf | CRF++-0.41/crf_test -m data/model4.bin | bin/tagg2text | bin/postprocess_ne -lex data/lex_ALL_NP.code\n";
	public static final boolean deleteTempFilesAfterProcess = true;
	
	public static final String[] POSSIBLE_SCRIPT_INTERPRETERS = { "/bin/bash", "/bin/sh" };
	static final char[] ignoredChars = { '.', '_' };

	File liaNeDir;
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
		super.initCommonParameters(context, null,   POSSIBLE_SCRIPT_INTERPRETERS[i], EXTERNAL_PROGRAM_NAME, false);

		liaNeDir = new File((String) context.getConfigParameterValue(LIA_NE_DIR_PARAM_NAME));			
		if (!liaNeDir.isDirectory()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "is_not_dir", new Object[]{liaNeDir.getAbsolutePath()});
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
			out.write(LIA_NE_SCRIPT_COMMANDS);
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
		
		ExternalProgram p = initProgram(aJCas);
		try {
			p.environment().put(LipnLiaExternalProgramGenericAnnotator.LIA_TAGG_ENVVAR, getProgramDir().getCanonicalPath());
			p.environment().put(LipnLiaExternalProgramGenericAnnotator.LIA_NE_ENVVAR, liaNeDir.getCanonicalPath());
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
