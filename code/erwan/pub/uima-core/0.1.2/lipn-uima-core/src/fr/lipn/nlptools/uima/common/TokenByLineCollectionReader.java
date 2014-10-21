package fr.lipn.nlptools.uima.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.apache.uima.util.XMLInputSource;

import fr.lipn.nlptools.uima.types.Lemma;
import fr.lipn.nlptools.uima.types.PartOfSpeech;
import fr.lipn.nlptools.uima.types.Sentence;
import fr.lipn.nlptools.uima.types.Token;
import fr.lipn.nlptools.utils.align.AlignerConsumer;
import fr.lipn.nlptools.utils.align.AlignmentException;
import fr.lipn.nlptools.utils.align.AnnotatedTextReader;
import fr.lipn.nlptools.utils.align.SimpleTokenByLineReader;
import fr.lipn.nlptools.utils.align.VoidInputReader;


/**
 * A collection reader that reads 'token by line' formatted documents from a directory in the filesystem.
 * A 'token by line' document is a text file containing lines of the form:
 * &lt;token&gt; [ part_of_speech [ lemma ] ] 
 * 
 * Notice that this format does not allow to use a TreeTagger output obtained with the 'multiple tags' option enabled. 
 * 
 *  It can be configured with the following parameters:
 * <ul>
 * <li><code>InputDirectory</code> - path to directory containing files</li>
 * <li><code>Encoding</code> (optional) - character encoding of the input files</li>
 * <li><code>Language</code> (optional) - language of the input documents</li>
 * <li><code>Separator</code> (optional) - columns separator (default is tab)</li>
 * <li><code>EndOfSentencesLabel</code> (optional) - if null or empty, do not annotate sentences. Otherwise use this label as the end of sentence mark</li>
 * </ul>  
 * 
 */
public class TokenByLineCollectionReader extends CollectionReader_ImplBase {

	public static final String TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID = TokenByLineCollectionReader.class.getCanonicalName();
	
	public static final String DEFAULT_COLUMN_SEPARATOR = "\t";
	public static final String TOKENS_SEPARATOR = LipnExternalProgramGenericAnnotator.LINE_SEPARATOR;
	public static final String ANY_BLANK_SEPARATOR = "\\s+";
	ArrayList<File> files;
	String encoding;
	String language;
	String separator;
	String endOfSentenceLabel;
	String userDefinedRunId;
	float userDefinedConfidence;
	boolean annotateSentences;
	int currentIndex;
	

	public void initialize() throws ResourceInitializationException {
		File directory = new File(((String) getConfigParameterValue("InputDirectory")).trim());
		if (getConfigParameterValue("Encoding") != null) {
			encoding = (String) getConfigParameterValue("Encoding");
		} else {
			encoding = LipnExternalProgramGenericAnnotator.DEFAULT_ENCODING;
		}
		language = (String) getConfigParameterValue("Language");
		if (getConfigParameterValue("Separator") != null) {
			separator = (String) getConfigParameterValue("Separator");
		} else {
			separator = DEFAULT_COLUMN_SEPARATOR;
		}
		annotateSentences = (Boolean) getConfigParameterValue("TagSentences");
		if (annotateSentences) {
			endOfSentenceLabel = (String) getConfigParameterValue("EndOfSentenceLabel");
		}
		userDefinedRunId = (String) getConfigParameterValue("RunId");
		if (getConfigParameterValue("Confidence") != null) {
			userDefinedConfidence = (Float) getConfigParameterValue("Confidence");
		} else {
			userDefinedConfidence = LipnExternalProgramGenericAnnotator.DEFAULT_CONFIDENCE;
		}
		currentIndex = 0;

		// if input directory does not exist or is not a directory, throw exception
		if (!directory.exists()) {
			throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
					new Object[] { "InputDirectory", this.getMetaData().getName(), directory.getPath() });
		}

		// get list of files (not subdirectories) in the specified directory
		files = new ArrayList<File>();
		File[] tempFiles = directory.listFiles();
		for (int i = 0; i < tempFiles.length; i++) {
			if (!tempFiles[i].isDirectory()) {
				files.add(tempFiles[i]);
			}
		}
	}


	/**
	 * @see org.apache.uima.collection.CollectionReader#hasNext()
	 */
	public boolean hasNext() {
		return currentIndex < files.size();
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
	 */
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}

		// open input stream to file
		File file = (File) files.get(currentIndex++);
		FileInputStream fis = new FileInputStream(file);
		try {
			try {
				AnnotatedTextReader aligner = new SimpleTokenByLineReader(new BufferedReader(new InputStreamReader(fis, encoding)), separator, null, null);
				aligner.align(new VoidInputReader(aligner),
						      new TokenByLineReceiver(jcas));			
			} catch (AlignmentException ae) {
				throw new IOException("An alignment error occured.", ae);
			}
		} finally {
			if (fis != null)
				fis.close();
		}

		// set language if it was explicitly specified as a configuration parameter
		if (language != null) {
			jcas.setDocumentLanguage(language);
		}

		// Also store location of source document in CAS. This information is critical
		// if CAS Consumers will need to know where the original document contents are located.
		// For example, the Semantic Search CAS Indexer writes this information into the
		// search index that it creates, which allows applications that use the search index to
		// locate the documents that satisfy their semantic queries.
		SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(jcas);
		srcDocInfo.setUri(file.toURI().toString());
		srcDocInfo.setOffsetInSource(0);
		srcDocInfo.setDocumentSize((int) file.length());
		srcDocInfo.setLastSegment(currentIndex == files.size());
		srcDocInfo.addToIndexes();
	}



	@Override
	public void close() throws IOException {

	}

	/**
	 * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
	 */
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, files.size(), Progress.ENTITIES) };
	}

	/**
	 * Gets the total number of documents that will be returned by this collection reader. This is not
	 * part of the general collection reader interface.
	 * 
	 * @return the number of documents in the collection
	 */
	public int getNumberOfDocuments() {
		return files.size();
	}

	/**
	 * Parses and returns the descriptor for this collection reader. The descriptor is stored in the
	 * uima.jar file and located using the ClassLoader.
	 * 
	 * @return an object containing all of the information parsed from the descriptor.
	 * 
	 * @throws InvalidXMLException
	 *           if the descriptor is invalid or missing
	 */
	public static CollectionReaderDescription getDescription() throws InvalidXMLException {
		InputStream descStream = TokenByLineCollectionReader.class
		.getResourceAsStream("TokenByLineCollectionReader");
		return UIMAFramework.getXMLParser().parseCollectionReaderDescription(
				new XMLInputSource(descStream, null));
	}

	public static URL getDescriptorURL() {
		return TokenByLineCollectionReader.class.getResource("TokenByLineCollectionReader");
	}

	protected class TokenByLineReceiver implements AlignerConsumer {
		
		JCas aJCas;
		StringBuilder content;
		int currentSentenceStart;
		int currentPos;
		Pattern separatorPattern;
		boolean foundSentence = false;
		
		public TokenByLineReceiver(JCas aJCas) {
			this.aJCas = aJCas;
		}

		@Override
		public void initConsumer() throws AlignmentException {
			content = new StringBuilder();
			currentSentenceStart = 0;
			currentPos = 0;
			separatorPattern = Pattern.compile(ANY_BLANK_SEPARATOR);
		}
		
		@Override
		public void closeConsumer() throws AlignmentException {
			// put document text in JCas
			aJCas.setDocumentText(content.toString());
			if ((annotateSentences) && (currentSentenceStart != currentPos)) { // the last sentence was not closed
				if (!foundSentence) {
					getUimaContext().getLogger().log(Level.WARNING, "No sentence found! end of sentence POS label was '"+endOfSentenceLabel+"'.");
				}
				Sentence s = new Sentence(aJCas);
				LipnExternalProgramGenericAnnotator.setGenericAttributes(s, currentSentenceStart, currentPos, TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID, s.getClass().getSimpleName(), userDefinedRunId, userDefinedConfidence);
				s.addToIndexes();
				getUimaContext().getLogger().log(Level.FINEST,"Adding Sentence (when closing) at "+currentSentenceStart+"-"+currentPos+".");
			}
		}

		
		@Override
		public void receiveTokenAndAnnotation(String annotatedToken, String inputToken, String annotation, String separator, long startPos, long endPos) throws AlignmentException {
			if (annotatedToken != null) {
				int start = currentPos;
				currentPos += annotatedToken.length();
				int end = currentPos;
				content.append(annotatedToken + TOKENS_SEPARATOR);
				currentPos += TOKENS_SEPARATOR.length();
				Token t = new Token(aJCas);
				LipnExternalProgramGenericAnnotator.setGenericAttributes(t, start, end, TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID, t.getClass().getSimpleName(), userDefinedRunId, userDefinedConfidence);
				t.addToIndexes();
				getUimaContext().getLogger().log(Level.FINEST,"Adding Token at "+start+"-"+end+".");
				if ((annotation != null) && (!annotation.isEmpty()) ) {
					String[] data = separatorPattern.split(annotation);
					if (data.length > 2) {
						throw new AlignmentException("Error: only two elements expected after token '"+annotatedToken+"' (read '"+annotation+"').");
					}
					PartOfSpeech pos = new PartOfSpeech(aJCas);
					LipnExternalProgramGenericAnnotator.setGenericAttributes(pos, start, end, TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID, pos.getClass().getSimpleName(), userDefinedRunId, userDefinedConfidence);
					pos.setValue(data[0]);
					pos.addToIndexes();
					getUimaContext().getLogger().log(Level.FINEST,"Adding PartOfSpeech '"+data[0]+"' at "+start+"-"+end+".");
					if (data.length > 1) {
						Lemma l = new Lemma(aJCas);
						LipnExternalProgramGenericAnnotator.setGenericAttributes(l, start, end, TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID, l.getClass().getSimpleName(), userDefinedRunId, userDefinedConfidence);
						l.setValue(data[1]);
						l.addToIndexes();
						getUimaContext().getLogger().log(Level.FINEST,"Adding Lemma '"+data[1]+"' at "+start+"-"+end+".");
					}
					if ((annotateSentences) && (data[0].equals(endOfSentenceLabel))) {
						foundSentence = true;
						Sentence s = new Sentence(aJCas);
						LipnExternalProgramGenericAnnotator.setGenericAttributes(s, currentSentenceStart, end, TOKEN_BY_LINE_COLLECTION_READER_COMPONENT_ID, s.getClass().getSimpleName(), userDefinedRunId, userDefinedConfidence);
						s.addToIndexes();
						getUimaContext().getLogger().log(Level.FINEST,"Adding Sentence at "+start+"-"+end+".");
						currentSentenceStart = currentPos;
					}
				}
			}
		}

		@Override
		public boolean requiresPosition() {
			return false;
		}

		@Override
		public boolean requiresSeparator() {
			return false;
		}

		@Override
		public boolean requiresUnannotatedTokens() {
			return true;
		}
		
	}


}
