package fr.lipn.nlptools.uima.yatea;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

import fr.lipn.nlptools.uima.common.LipnExternalProgramGenericAnnotator;
import fr.lipn.nlptools.uima.types.Term;
import fr.lipn.nlptools.uima.types.TermOccurrence;
import fr.lipn.nlptools.uima.types.YateaTerm;
import fr.lipn.nlptools.uima.types.YateaTermOccurrence;
import fr.lipn.nlptools.uima.types.YateaTestifiedTerm;

/**
 * 
 * This class implements an XML SAX parser for YaTeA output. (SAX is clearly a better choice since the Yatea XML output can be very large).
 * It has been buit based on yatea official DTD, but in fact mainly by observing examples with real data.<br/>
 * 
 * IMPORTANT: there are quite a lot of tags in yatea output DTD which are ignored in this parser because they seem to be unused by yatea:
 *            it has been checked for some of them that there are no reference to it in yatea code (a fortiori no writing in the xml output).
 *            In particular but not exclusively: LIST_WORDS and WORD tags, SYNTACTIC_ANALYSIS_COORD and sub-elements, and a lot of morpho-syntactic details.<br/>
 *            
 *  		The BEFORE and AFTER attributes are currently ignored.
 *
 * @author moreau
 *
 */
public class YateaXMLOutputParser extends DefaultHandler {

	public static final boolean ADD_ANNOTATIONS_AT_THE_END = false;

	public static final String TERM_CANDIDATE_TAGNAME = "TERM_CANDIDATE";
	public static final String ID_TAGNAME = "ID";
	public static final String FORM_TAGNAME = "FORM";
	public static final String LEMMA_TAGNAME = "LEMMA";
	public static final String MORPHOSYNTACTIC_FEATURES_TAGNAME = "MORPHOSYNTACTIC_FEATURES";
	public static final String SYNTACTIC_CATEGORY_TAGNAME = "SYNTACTIC_CATEGORY";
	public static final String HEAD_TAGNAME = "HEAD";
	public static final String LIST_OCCURRENCES_TAGNAME = "LIST_OCCURRENCES";
	public static final String TERM_CONFIDENCE_TAGNAME = "TERM_CONFIDENCE";
	public static final String OCCURRENCE_TAGNAME = "OCCURRENCE";
	public static final String SENTENCE_TAGNAME = "SENTENCE";
	public static final String START_POSITION_TAGNAME = "START_POSITION";
	public static final String END_POSITION_TAGNAME = "END_POSITION";
	public static final String MNP_TAGNAME = "MNP";
	public static final String LIST_RELIABLE_ANCHORS_TAGNAME = "LIST_RELIABLE_ANCHORS";
	public static final String RELIABLE_ANCHOR_TAGNAME = "RELIABLE_ANCHOR";
	public static final String SYNTACTIC_ANALYSIS_TAGNAME = "SYNTACTIC_ANALYSIS";
	public static final String PREP_TAGNAME = "PREP"; 
	public static final String DETERMINER_TAGNAME = "DETERMINER";
	public static final String MODIFIER_TAGNAME = "MODIFIER";
	public static final String OCCURRENCE_CONFIDENCE_TAGNAME = "OCCURRENCE_CONFIDENCE";
	public static final String ORIGIN_TAGNAME = "ORIGIN";
	public static final String YATEA_TESTIFIED_TERM_ID_PREFIX = "testified";

	public static final String YATEA_FORM_TOKENS_SEPARATOR = " ";
	
	JCas aJCas;
	boolean occurrencesOnly;
	boolean detailedTerms;
	boolean checkOccurrenceForm;
	boolean correctYateaPosition;
	YateaAE ae;
	XMLReader parser;
	GenericLocalElementParser localParser;
	Locator locator;
	ArrayList<Annotation> annotationsToAdd;
	int nbAnnotations;
	Logger logger;
	// start/end indexes in the xml output are computed from the 'token by line' format,
	// thus are generally different from the original indexes
	ArrayList<HashMap<Integer, Annotation>> tokenIndexMapping;
	HashMap<String, Term> terms;
	Pattern[] sourceToYateaPatterns = null;

	
	// temporary data receivers (SAX parsing)
	// 

	CharArrayWriter content;
	String currentId;
	String currentConfidence;
	String currentForm;
	String currentLemma;
	String currentHead;
	ArrayList<TermOccurrence> currentOccurrences;
	ArrayList<int []> currentOccurrencesYateaPosition; // necessary for post-processing (to correct the position in the case tokens have been shifted/swapped)
	ArrayList<Double> currentOccurrencesConfidence;
	String currentTermSyntCat;
	ArrayList<String> currentReliableAnchorsTargets;
	ArrayList<String> currentReliableAnchorsForms;
	String currentSyntAnaHead;
	String currentSyntAnaModifier;
	String currentPrep;
	String currentDet;


	public YateaXMLOutputParser(JCas aJCas, XMLReader parser, YateaAE ae, Logger logger, boolean tagOccurrencesOnly, boolean detailedTerms, boolean checkOccurrenceForm, boolean correctYateaPosition) throws AnalysisEngineProcessException {
		super();
		this.aJCas = aJCas;
		this.parser = parser;
		locator = new LocatorImpl();
		tokenIndexMapping = ae.initTokenIndexMapping(aJCas, logger);
		localParser = new GenericLocalElementParser(this, parser);
		this.ae = ae;
		this.logger = logger;
		this.occurrencesOnly = tagOccurrencesOnly;
		this.detailedTerms = detailedTerms;
		this.checkOccurrenceForm = checkOccurrenceForm;
		this.correctYateaPosition = correctYateaPosition;
		if (YateaAE.YATEA_CHARACTERS_TO_MAP != null) {
			sourceToYateaPatterns = new Pattern[YateaAE.YATEA_CHARACTERS_TO_MAP.length];
			for (int i=0; i< YateaAE.YATEA_CHARACTERS_TO_MAP.length; i++)  {
				sourceToYateaPatterns[i] = Pattern.compile(YateaAE.YATEA_CHARACTERS_TO_MAP[i]);
			}
		}

	}

	public void setDocumentLocator(Locator value) {
		locator =  value;
	}


	public void startDocument() throws SAXException {
		if (!occurrencesOnly) {
			terms = new HashMap<String, Term>();
		}
		content = new CharArrayWriter();
		if (ADD_ANNOTATIONS_AT_THE_END) {
			annotationsToAdd = new ArrayList<Annotation>();
		}
		nbAnnotations = 0;
	}

	
	public void endDocument() throws SAXException {
		logger.log(Level.FINEST, ""+nbAnnotations+" annotations have been written.");
		if (!occurrencesOnly) {
			for (String id : terms.keySet()) {
				if (terms.get(id).getArguments() == null) {
					logger.log(Level.WARNING, "end doc: empty list of occurrences for term id ="+id);
					throw new SAXException("Error: undefined terms references (ids) remaining at the end of the parsing process.");
				}
			}
		}
		if (ADD_ANNOTATIONS_AT_THE_END) {
			for (Annotation a: annotationsToAdd) {
				a.addToIndexes();
			}
		}
	}


	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		//System.out.println("STARTING TAG "+namespaceURI+","+localName+","+qName+".");
		if (qName.equals(TERM_CANDIDATE_TAGNAME)) {
			currentOccurrences = new ArrayList<TermOccurrence>();
			currentOccurrencesYateaPosition = new ArrayList<int[]>();
			currentOccurrencesConfidence = new ArrayList<Double>();
			currentLemma = null;
			currentHead = null;
			currentTermSyntCat = null;
			currentReliableAnchorsTargets = null;
			currentReliableAnchorsForms = null;
			currentSyntAnaHead = null;
			currentSyntAnaModifier = null;
			currentPrep = null;
			currentDet = null;
		} else if (qName.equals(RELIABLE_ANCHOR_TAGNAME) ||
				qName.equals(MORPHOSYNTACTIC_FEATURES_TAGNAME) ||
				qName.equals(SYNTACTIC_ANALYSIS_TAGNAME) ||
				qName.equals(OCCURRENCE_TAGNAME) ) {
			logger.log(Level.FINER, "Starting local parser inside '"+qName+"'");
			localParser.startLocalParser(qName);
		} else {  // simple element (ID, LEMMA, HEAD, etc)
			content.reset();
		}
	}




	public void receiveLocalParserResult(String elementName, Map<String, String> data, Map<String, Attributes> attrs) throws SAXException {
		logger.log(Level.FINE, "receiving localParser results for '"+elementName+"'");
		if (elementName.equals(OCCURRENCE_TAGNAME)) {
			TermOccurrence o;
			if (detailedTerms) {
				o = new YateaTermOccurrence(aJCas);
				((YateaTermOccurrence) o).setMaximalNounPhrase(data.get(MNP_TAGNAME));
			} else {
				o = new TermOccurrence(aJCas);
			}
			
//			String id = data.get(ID_TAGNAME);  // ignored because this id seems useless: it is never used as a reference, and there are never two occs with the same id
			Integer sentenceId = Integer.valueOf(data.get(SENTENCE_TAGNAME));
			Integer start = Integer.valueOf(data.get(START_POSITION_TAGNAME));
			Integer end = Integer.valueOf(data.get(END_POSITION_TAGNAME));
			currentOccurrences.add(o);
			currentOccurrencesYateaPosition.add(new int[]{sentenceId, start, end});
			if (data.get(OCCURRENCE_CONFIDENCE_TAGNAME) != null) {
				currentOccurrencesConfidence.add(Double.valueOf(data.get(OCCURRENCE_CONFIDENCE_TAGNAME)));
			} else {
				currentOccurrencesConfidence.add(Double.valueOf((double) YateaAE.DEFAULT_CONFIDENCE));
			}
		} else if (elementName.equals(MORPHOSYNTACTIC_FEATURES_TAGNAME)) {
			currentTermSyntCat = data.get(SYNTACTIC_CATEGORY_TAGNAME);
		} else if (elementName.equals(RELIABLE_ANCHOR_TAGNAME)) {
			if (currentReliableAnchorsTargets == null) { // to save space
				logger.log(Level.FINER, "new arrays for currentReliableAnchorsXXX");
				currentReliableAnchorsTargets = new ArrayList<String>();
				currentReliableAnchorsForms = new ArrayList<String>();
			}
			logger.log(Level.FINEST, "setting reliableAnchor params for index "+currentReliableAnchorsTargets.size()+": id="+data.get(ID_TAGNAME)+" ; form="+data.get(FORM_TAGNAME));
			currentReliableAnchorsTargets.add(data.get(ID_TAGNAME));
			currentReliableAnchorsForms.add(data.get(FORM_TAGNAME));
		} else if (elementName.equals(SYNTACTIC_ANALYSIS_TAGNAME)) {
			currentSyntAnaHead = data.get(HEAD_TAGNAME);
			currentSyntAnaModifier = data.get(MODIFIER_TAGNAME); // CAUTION: attribute is ignored !!
			if (data.get(PREP_TAGNAME) != null) {
				currentPrep = data.get(PREP_TAGNAME);
			}
			if (data.get(DETERMINER_TAGNAME) != null) {
				currentDet = data.get(DETERMINER_TAGNAME);
			}			 
		} else {
			throw new SAXException("Unexpected error! An unknown tag name has been returned by local parser, this is certainly a bug!");
		}
	}

	
	/**
	 * IMPORTANT: this method is intended to set the right begin/end positions for each term in the Yatea output. This is not trivial for several reasons:
	 * 1) Yatea uses a very special kind of position indexing, which is why a mapping tokenIndexMapping must have been computed before the parsing process.
	 * 2) Some bugs in Yatea causes it to sometimes assign a wrong position. see lipn-uima-core documentation for details.
	 * @throws SAXException
	 */
	protected void setPositionForCurrentOccurrences() throws SAXException {
		// setting position....
		
		String[] yateaFormTokens = null;
		if (checkOccurrenceForm || correctYateaPosition) {
			logger.log(Level.FINE, "Starting trying to match term '"+currentForm+"' against source data");
			yateaFormTokens = currentForm.split(YATEA_FORM_TOKENS_SEPARATOR);			
		}
		for (int i=0; i< currentOccurrences.size(); i++) {
			int sentenceId = currentOccurrencesYateaPosition.get(i)[0];
			int start = currentOccurrencesYateaPosition.get(i)[1];
			int end = currentOccurrencesYateaPosition.get(i)[2];
			int sourceStart = -1;
			int sourceEnd = -1;
			boolean wrongPos = false;
			double confidence = currentOccurrencesConfidence.get(i);
				if (tokenIndexMapping.get(sentenceId).get(start) == null) {
					if (correctYateaPosition) {
						wrongPos = true;
					} else {
						throw new SAXException("Error when parsing the XML YaTeA output file: impossible to match start index '"+start+"' in sentenceId '"+sentenceId+"' for occurrence id='"+currentForm+"'.");
					}
				} else if (tokenIndexMapping.get(sentenceId).get(end) == null) {
					if (correctYateaPosition) {
						wrongPos = true;
					} else {
						throw new SAXException("Error when parsing the XML YaTeA output file: impossible to match end index '"+end+"' in sentenceId '"+sentenceId+"' for occurrence form='"+currentForm+"'.");
					}
				} else {
					sourceStart = tokenIndexMapping.get(sentenceId).get(start).getBegin();
					sourceEnd = tokenIndexMapping.get(sentenceId).get(end).getEnd();
					if (checkOccurrenceForm) {
						if (yateaFormTokens.length == 1) { // optimization for simple case of 1 token
							String [] sourceToken = getSourceTokenAsArray(tokenIndexMapping.get(sentenceId).get(start).getCoveredText(), sourceToYateaPatterns);
							logger.log(Level.FINER,"Looking between yatea indexes "+start+"-"+end+" for TermOccurrence "+i+" in source indexes "+sourceStart+"-"+sourceEnd+" single token: comparing '"+yateaFormTokens[0]+"' and '"+sourceToken[0]+((sourceToken.length>1)?("'..'"+sourceToken[sourceToken.length-1]):"")+"'");
							if (!originalTokenMatchesYateaTokens(sourceToken, yateaFormTokens, 0)) {
								if (correctYateaPosition) {
									wrongPos = true;
								} else {
									throw new SAXException("Error when parsing the XML YaTeA output file: no match between YaTeA form and corresponding position in source text for form '"+currentForm+"' when comparing token '"+yateaFormTokens[0]+"' to source '"+sourceToken+"' at position "+sourceStart+".");
								}
							}
						} else {
							// iterate over (sorted) indexes obtained in the sorted subset of the hash indexes corresponding to this range start-end
							// (sorry, that's a complex one ;)
							Iterator<Integer> mappingIndexesIter = ((new TreeSet<Integer>(tokenIndexMapping.get(sentenceId).keySet())).subSet(start, true, end, true)).iterator();
							logger.log(Level.FINER,"Looking between yatea indexes "+start+"-"+end+" for TermOccurrence "+i+" in source indexes "+sourceStart+"-"+sourceEnd);
							int yateaFormTokensIndex = 0;
							while (!wrongPos && (yateaFormTokensIndex <yateaFormTokens.length)) {
								if (mappingIndexesIter.hasNext()) {
									int expectedTokenStart = mappingIndexesIter.next();
									String [] sourceToken = getSourceTokenAsArray(tokenIndexMapping.get(sentenceId).get(expectedTokenStart).getCoveredText(), sourceToYateaPatterns);
									if (mappingIndexesIter.hasNext()) {
										mappingIndexesIter.next(); // skip the end index, which (hopefully) corresponds to the same Token
									} else {
										logger.log(Level.WARNING,"Did not find the end index (no more indexes) in the subset indexes as expected, something is wrong (probably a bug)");		
									}
									logger.log(Level.FINEST,"Testing token(s) starting at "+yateaFormTokensIndex+ "='"+yateaFormTokens[yateaFormTokensIndex]+"' against source '"+sourceToken[0]+((sourceToken.length>1)?("'..'"+sourceToken[sourceToken.length-1]):"")+"' at "+expectedTokenStart);
									if (!originalTokenMatchesYateaTokens(sourceToken, yateaFormTokens, yateaFormTokensIndex)) {
										if (correctYateaPosition) {
											wrongPos = true;
										} else {
											throw new SAXException("Error when parsing the XML YaTeA output file: no match between YaTeA form and corresponding position in source text for form '"+currentForm+"' when comparing token(s) starting at '"+yateaFormTokens[yateaFormTokensIndex]+"' to source '"+sourceToken+"' at position "+expectedTokenStart+".");
										}
									} else {
										yateaFormTokensIndex += sourceToken.length;
									}
								} else {
									if (correctYateaPosition) {
										wrongPos = true;
									} else {
										throw new SAXException("Error when parsing the XML YaTeA output file: no match between YaTeA form and corresponding position in source text for form '"+currentForm+"' when comparing token(s) starting at '"+yateaFormTokens[yateaFormTokensIndex]+"': fewer tokens in source text at position "+sourceStart+"-"+sourceEnd+".");
									}
								}
							}
							if (yateaFormTokensIndex < yateaFormTokens.length) {
								if (correctYateaPosition) {
									wrongPos = true;
								} else {
									throw new SAXException("Error when parsing the XML YaTeA output file: no match between YaTeA form and corresponding position in source text for form '"+currentForm+"' when comparing token(s): fewer tokens in source text at position "+sourceStart+"-"+sourceEnd+" (yatea tokens remaining) .");
								}
							}
						}
					}
				}
				if (wrongPos) {  // can be true only if correctYateaPosition is true
					logger.log(Level.WARNING, "Term '"+currentForm+"' not found in source at expected position "+start+"-"+end+"  in sentence "+sentenceId+", trying to recover the right position... ");
					int targetYateaPos = -1;  // will be the supposed closest position of the token: the token is not here because wrongPos, but we ll try to find it close to this position
					if (tokenIndexMapping.get(sentenceId).get(start) != null) {
						targetYateaPos = start;
					} else if (tokenIndexMapping.get(sentenceId).get(end) != null) {
						targetYateaPos = end;
					} else {
						for (int tokenPos : tokenIndexMapping.get(sentenceId).keySet()) { // if no exact match, find closest yatea position
							if (targetYateaPos == -1) {
								targetYateaPos = tokenPos;
							} else {
								if (Math.abs(targetYateaPos - start) > Math.abs(tokenPos - start)) {
									targetYateaPos = tokenPos;
								}
							}
						}
					}
//					int[] sourcePos = new int[yateaFormTokens.length];
//					Arrays.fill(sourcePos, -1);
					int prevSourcePos = -1;
					int yateaFormTokensIndex = 0;
					while (yateaFormTokensIndex <yateaFormTokens.length) {
						int thisSourcePos = -1;
						String[] thisSourceToken = null;
						logger.log(Level.FINER,"Heuristic testing token(s) starting at "+yateaFormTokensIndex+ "='"+yateaFormTokens[yateaFormTokensIndex]+"'");
						Iterator<Integer> tokenPosIterator = new TreeSet<Integer>(tokenIndexMapping.get(sentenceId).keySet()).iterator();
						while (tokenPosIterator.hasNext()) {
							int tokenPos  = tokenPosIterator.next();
							logger.log(Level.FINEST,"testing against  '"+tokenIndexMapping.get(sentenceId).get(tokenPos).getCoveredText()+"' at "+tokenPos);
							String [] originalToken = getSourceTokenAsArray(tokenIndexMapping.get(sentenceId).get(tokenPos).getCoveredText(), sourceToYateaPatterns);
							if (originalTokenMatchesYateaTokens(originalToken, yateaFormTokens, yateaFormTokensIndex)) {
								if (thisSourcePos == -1) { // first occurrence found
									thisSourcePos = tokenPos;
									thisSourceToken = originalToken;
								} else {
									if ((yateaFormTokensIndex > 0) && (thisSourcePos < prevSourcePos) && (tokenPos > prevSourcePos))  { // preferably choose a position AFTER the last one
										logger.log(Level.FINEST,"Match found at "+tokenPos);									
										thisSourcePos = tokenPos;
										thisSourceToken = originalToken;
									} else {  // otherwise look for position closest to target
										if (Math.abs(targetYateaPos - thisSourcePos) > Math.abs(targetYateaPos - tokenPos)) {
											logger.log(Level.FINEST,"Closer match found at "+tokenPos);									
											thisSourcePos = tokenPos;
											thisSourceToken = originalToken;
										}
									}
								}
							}
							if (tokenPosIterator.hasNext()) { // skip the second position, which (normally) corresponds to the end position for the same token
								tokenPosIterator.next(); 
							} else { // error: no end position??? (should never happen)
								throw new SAXException("Error: no position remaining in the list whereas the end position corresponding to the last token has not been found (this is a bug)");
							}
						}
						if (thisSourcePos == -1) {  // still -1 : token has not been found at all
							throw new SAXException("Error: token '"+yateaFormTokens[yateaFormTokensIndex]+"' in term '"+currentForm+"' has not been found at all in sentence '"+sentenceId+"' (start-end="+start+"-"+end+")");
						}
						if (sourceStart == -1) {
							sourceStart = tokenIndexMapping.get(sentenceId).get(thisSourcePos).getBegin();
							sourceEnd = tokenIndexMapping.get(sentenceId).get(thisSourcePos).getEnd();
						} else {
							if (sourceStart > tokenIndexMapping.get(sentenceId).get(thisSourcePos).getBegin()) {
								sourceStart = tokenIndexMapping.get(sentenceId).get(thisSourcePos).getBegin();
							}
							if (sourceEnd < tokenIndexMapping.get(sentenceId).get(thisSourcePos).getEnd()) {
								sourceEnd = tokenIndexMapping.get(sentenceId).get(thisSourcePos).getEnd();
							}
						}
						prevSourcePos = thisSourcePos;
						yateaFormTokensIndex += thisSourceToken.length;
						logger.log(Level.FINEST,"New index for the yatea tokens: "+yateaFormTokensIndex);									

					}
					logger.log(Level.INFO, "Term '"+currentForm+"' (expected position "+start+"-"+end+", sentence "+sentenceId+") found by heuristic at "+sourceStart+"-"+sourceEnd+".");
				}
				logger.log(Level.FINE, "Term '"+currentForm+"' (expected position "+start+"-"+end+", sentence "+sentenceId+") found at "+sourceStart+"-"+sourceEnd+".");

				ae.setGenericAttributes(currentOccurrences.get(i), 
					sourceStart, 
					sourceEnd, 
					ae.getDefaultComponentId(), 
					TermOccurrence.class.getSimpleName(), 
					confidence);
		}
				
	}

	
	protected String[] getSourceTokenAsArray(String tokenSeenByYatea, Pattern[] sourceToYateaPatterns) {
//		for (int k=0; k<sourceToYateaPatterns.length; k++) {
//			tokenSeenByYatea = sourceToYateaPatterns[k].matcher(tokenSeenByYatea).replaceAll((YateaAE.YATEA_CHARACTERS_MAPPING!=null)?YateaAE.YATEA_CHARACTERS_MAPPING[k]:"");
//		}
		return tokenSeenByYatea.split(YATEA_FORM_TOKENS_SEPARATOR);
	}

	protected boolean originalTokenMatchesYateaTokens(String[] originalToken, String[] yateaTokens, int yateaTokensIndexStart) {
		int j=0;
		logger.log(Level.FINEST, "debug information 1: original/yatea length="+originalToken.length+"/"+yateaTokens.length+"; yateaIndexStart="+yateaTokensIndexStart+"; now comparing with j="+j+", originalToken[j]='"+((j<originalToken.length)?originalToken[j]:"UNDEF")+"' with yateaTokens[yateaTokensIndexStart+j]='"+((yateaTokensIndexStart+j < yateaTokens.length)?yateaTokens[yateaTokensIndexStart+j]:"UNDEF")+"'");
		while ((j<originalToken.length) && (yateaTokensIndexStart+j < yateaTokens.length) && LipnExternalProgramGenericAnnotator.compareModuloWilcardChar(originalToken[j], yateaTokens[yateaTokensIndexStart+j],ae.getCodingErrorReplacementValue(),false)) {
			j++;
			logger.log(Level.FINEST, "debug information 1: original/yatea length="+originalToken.length+"/"+yateaTokens.length+"; yateaIndexStart="+yateaTokensIndexStart+"; now comparing with j="+j+", originalToken[j]='"+((j<originalToken.length)?originalToken[j]:"UNDEF")+"' with yateaTokens[yateaTokensIndexStart+j]='"+((yateaTokensIndexStart+j < yateaTokens.length)?yateaTokens[yateaTokensIndexStart+j]:"UNDEF")+"'");
		}
		if (j==originalToken.length) {
			logger.log(Level.FINEST, "Match found");
			return true;
		} else {
			logger.log(Level.FINEST, "Not matching!");
			return false;
		}
	}
		
	
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals(TERM_CANDIDATE_TAGNAME)) {

			setPositionForCurrentOccurrences();					
			if (!occurrencesOnly) {
				Term currentTerm = getOrCreateTerm(currentId, null); // because of forward references
				logger.log(Level.FINE, "Ending term id="+currentId);
				currentTerm.setArguments(new FSArray(aJCas, currentOccurrences.size()));
				int start= aJCas.getDocumentText().length()+1;
				int end = -1;
				for (int i=0; i< currentOccurrences.size(); i++) { // for each occurrence, add it to the list and see if it is the first/last one
					logger.log(Level.FINEST, "Adding  occurrence at pos "+currentOccurrences.get(i).getBegin()+" for term id="+currentId);
					currentTerm.setArguments(i, currentOccurrences.get(i));
					start=(currentOccurrences.get(i).getBegin()<start)?currentOccurrences.get(i).getBegin():start;
					end=(currentOccurrences.get(i).getEnd()>end)?currentOccurrences.get(i).getEnd():end;
					if (detailedTerms) {
						((YateaTerm) currentTerm).setHead(getOrCreateTerm(currentHead, null)); // hope a testified term can not appear here ???
						((YateaTerm) currentTerm).setSyntacticAnalysisDet(currentDet);
						((YateaTerm) currentTerm).setSyntacticAnalysisHead(getOrCreateTerm(currentSyntAnaHead, null));  // idem
						((YateaTerm) currentTerm).setSyntacticAnalysisModifier(getOrCreateTerm(currentSyntAnaModifier, null));  //idem
						((YateaTerm) currentTerm).setSyntacticAnalysisPrep(currentPrep);
						((YateaTerm) currentTerm).setSyntacticCategory(currentTermSyntCat);
						if (currentReliableAnchorsTargets != null) {
							((YateaTerm) currentTerm).setReliableAnchors(new FSArray(aJCas, currentReliableAnchorsTargets.size()));
							for (int j=0; j < currentReliableAnchorsTargets.size(); j++) {
								logger.log(Level.FINEST, "reading current reliableAnchor params for index "+currentReliableAnchorsTargets.size()+": id="+currentReliableAnchorsTargets.get(j)+"");
								((YateaTerm) currentTerm).setReliableAnchors(j, getOrCreateTerm(currentReliableAnchorsTargets.get(j), currentReliableAnchorsForms.get(j)));
							}
						}
					}
				}
				// NB: the "type" feature is set to "Term" even if it is a YateaTerm
				ae.setGenericAttributes(currentTerm, start, end, ae.getDefaultComponentId(), Term.class.getSimpleName(), Double.valueOf(currentConfidence).doubleValue());
				addAnnotation(currentTerm);
			} 
			for (int i=0; i< currentOccurrences.size(); i++) { // for each occurrence, set the term lemma and add the annotation
				currentOccurrences.get(i).setValue(currentLemma);
				addAnnotation(currentOccurrences.get(i));
			}
		} else if (qName.equals(ID_TAGNAME)) {
			currentId = content.toString().trim();
		} else if (qName.equals(LEMMA_TAGNAME)) {
			currentLemma = content.toString().trim();
		} else if (qName.equals(FORM_TAGNAME)) {
			currentForm = content.toString().trim();
		} else if (qName.equals(HEAD_TAGNAME)) {
			currentHead = content.toString().trim();
		} else if (qName.equals(TERM_CONFIDENCE_TAGNAME)) {
			currentConfidence = content.toString().trim();
		} // otherwise don't care about this element

	}



	public void characters( char[] ch, int start, int length ) {
		content.write(ch, start, length);
	}


	/**
	 * creates a new term if this term does not exist or return the existing one.
	 * If the id provided fulfills the "testified term" requirement then such a special term is created with
	 * the supplied form -> this is the only case where targetForm is used. 
	 * 
	 * @param targetId
	 * @param targetForm
	 * @return
	 */
	protected Term getOrCreateTerm(String targetId, String targetForm) {
		Term target;
		if (targetId == null) {
			return null;
		} else {
			if (terms.get(targetId) != null) {
				target = terms.get(targetId);
			} else {
				logger.log(Level.FINE, "Get/create: creating term id="+targetId+"' (current term id="+currentId+")");
				if (targetId.startsWith(YATEA_TESTIFIED_TERM_ID_PREFIX)) {  // can be found only if detailedTerms is true 
					logger.log(Level.FINE, "Get/create:  term id="+targetId+"' is a testified term with form '"+targetForm+"'");
					YateaTestifiedTerm testifiedTerm = new YateaTestifiedTerm(aJCas);
					testifiedTerm.setForm(targetForm);
					testifiedTerm.setArguments(new FSArray(aJCas, 0)); // no occurrences for a testified term
					ae.setGenericAttributes(testifiedTerm, 0, aJCas.getDocumentText().length(), 1.0);
					addAnnotation(testifiedTerm);
					target = testifiedTerm;
				} else {
					target = detailedTerms?new YateaTerm(aJCas):new Term(aJCas);
				}
				terms.put(targetId, target); // forward ref
			}
			return target;
		}
	}



	protected void addAnnotation(Annotation a) {
		if (ADD_ANNOTATIONS_AT_THE_END) {
			annotationsToAdd.add(a);
		} else {
			a.addToIndexes();
		}
		nbAnnotations++;
	}



	/*
	 * Simple generic SAX parser: element must be a 0-level structure only with
	 * at most one element for each possible element name.
	 */
	class GenericLocalElementParser extends DefaultHandler { 

		YateaXMLOutputParser parent;
		XMLReader parser;
		String endTagName;
		Map<String, String> data;
		Map<String, Attributes> attributes;
		CharArrayWriter content;

		/**
		 * parent and parser are supposed to be the same for all calls to 
		 * startLocalParser
		 */
		public GenericLocalElementParser(YateaXMLOutputParser parent, XMLReader parser) {
			super();
			this.parent = parent;
			this.parser = parser;
			data = new HashMap<String, String>();
			content = new CharArrayWriter();
		}

		public void startLocalParser(String endTagName) {
			this.endTagName = endTagName;
			data.clear();
			content.reset();
			parser.setContentHandler(this);
		}

		public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
			if (attrs.getLength() > 0) {
				if (attributes == null) { // not initialized before some attributes are found, because it's generally useless
					attributes = new HashMap<String, Attributes>();
				}
				attributes.put(qName, attrs);
			}
			content.reset();
		}

		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (qName.equals(endTagName)) {
				parent.receiveLocalParserResult(endTagName, data, attributes);
				parser.setContentHandler(parent);
			} else {
				data.put(qName, content.toString().trim());
			}
		}



		public void characters( char[] ch, int start, int length ) {
			content.write(ch, start, length);
		}


	}

}
