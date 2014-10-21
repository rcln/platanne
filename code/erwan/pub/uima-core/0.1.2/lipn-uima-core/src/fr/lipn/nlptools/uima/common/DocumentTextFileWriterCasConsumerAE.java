package fr.lipn.nlptools.uima.common;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;


public class DocumentTextFileWriterCasConsumerAE extends LipnExternalProgramGenericAnnotator {

	public final static String DEFAULT_EXTENSION = "txt";
	File outputDir;
	String encoding;
	String extension;
	String viewName;
	private int docNum;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		outputDir = new File((String) context.getConfigParameterValue("OutputDir"));
		viewName = (String) context.getConfigParameterValue("ViewName");
		
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
	    	output.write(aJCas.getView(viewName).getDocumentText());
	    	output.close();
	    } catch (UnsupportedEncodingException ee) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unknown_encoding", new Object[]{encoding}, ee);
	    } catch (CASException ce) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "view_not_found", new Object[]{viewName}, ce);
		} catch (IOException ioe) {
	    	throw new AnalysisEngineProcessException(LIPN_MESSAGE_DIGEST, "unable_write_file", new Object[]{outFile.getAbsolutePath()}, ioe);
		}
	}

}
