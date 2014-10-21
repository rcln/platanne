package fr.lipn.nlptools.uima.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import fr.lipn.nlptools.uima.treetagger.TreeTaggerAE;
import fr.lipn.nlptools.utils.externprog.ExternalProgramException;
import fr.lipn.nlptools.utils.externprog.WriterFeeder;

/**
 * 
 * CAS Consumer component (actually in the AE style), which writes a 'token by line' output.
 * it is the standard format used for example by TreeTagger:
 * <code>
 * &lt;token&gt; &lt;POS tag&gt; &lt;Lemma&gt; 
 * </code>
 * 
 * This consumer can writes only tokens, or tokens + POS, or tokens + POS + Lemmas.
 * 
 * Notice that this consumer reads the annotations in the CAS. Another way to obtain
 * such kind of output is to save the program output (say TreeTagger) in a view (using the
 * suitable parameter) and then to run the {@link DocumentTextFileWriterCasConsumerAE} CAS
 * consumer. 
 * 
 * Parameters: output directory, encoding, output file(s) extension.
 * 
 * @author moreau
 *
 */
public class TokenByLineCasConsumerAE extends LipnExternalProgramGenericAnnotator {

	public final static String DEFAULT_COLUMN_SEPARATOR = "\t"; 
	public final static String DEFAULT_EXTENSION = "txt";
	File outputDir;
	String encoding;
	String extension;
	private int docNum;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		outputDir = new File((String) context.getConfigParameterValue("OutputDir"));
		if (context.getConfigParameterValue("Encoding") != null) {
			encoding = (String) context.getConfigParameterValue("Encoding");
		} else {
			encoding = DEFAULT_ENCODING;
		}
		if (context.getConfigParameterValue("Extension") != null) {
			extension = (String) context.getConfigParameterValue("Extension");
		} else {
			extension = DEFAULT_EXTENSION;
		}
		
		// check / possibly create dir
		if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
			throw new ResourceInitializationException(LipnExternalProgramGenericAnnotator.LIPN_MESSAGE_DIGEST, "unable_create_dir", new Object[]{outputDir.getAbsoluteFile()});
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	    // retrieve the filename of the input file from the CAS
	    FSIterator<Annotation> it = FSIteratorFactory.createFSIterator(aJCas, SourceDocumentInformation.type, false);
	    File outFile = null;
	    if (it.hasNext()) {
	      SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
	      File inFile;
	      try {
	        inFile = new File(new URL(fileLoc.getUri()).getPath());
	        String outFileName = inFile.getName();
	        if (fileLoc.getOffsetInSource() > 0) {
	          outFileName += ("_" + fileLoc.getOffsetInSource());
	        }
	        outFileName += "."+ extension;
	        outFile = new File(outputDir, outFileName);
	      } catch (MalformedURLException e1) {
	        // invalid URL, use default processing below
	      }
	    }
	    if (outFile == null) {
	      outFile = new File(outputDir, "doc" + docNum++ + "." + extension);
	    }
	    try {
	    	OutputStreamWriter output = new OutputStreamWriter(new java.io.FileOutputStream(outFile), encoding);
	    	WriterFeeder writerFeeder = new TokenByLineWriterFeeder(new BasicTokenPOSLemmaIterator(aJCas, true, true, false), LipnExternalProgramGenericAnnotator.LINE_SEPARATOR, DEFAULT_COLUMN_SEPARATOR, TreeTaggerAE.TREETAGGER_UNKNOWN_LEMMA_TAG);
	    	writerFeeder.feedWriter(output);
	    } catch (UnsupportedEncodingException ee) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unknown_encoding", new Object[]{encoding}, ee);
	    } catch (FileNotFoundException fe) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unable_write_file", new Object[]{outFile.getAbsolutePath()}, fe);
	    } catch (ExternalProgramException epe) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unable_write_file", new Object[]{outFile.getAbsolutePath()}, epe);
	    }
	}

}
