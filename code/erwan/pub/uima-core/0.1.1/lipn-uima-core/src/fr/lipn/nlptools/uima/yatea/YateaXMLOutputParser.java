package fr.lipn.nlptools.uima.yatea;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

/**
 * TODO doc 
 * built using yatea dtd, with some observations about processed sample data.
 * IMPORTANT: there are quite a lot of tags in yatea output DTD which are ignored in this parser because they seem to be unused by yatea:
 *            it has been checked for some of them that there are no reference to it in yatea code (a fortiori no writing in the xml output).
 *            In particular but not exclusively: LIST_WORDS and WORD tags, SYNTACTIC_ANALYSIS_COORD and sub-elements, and a lot of morpho-syntactic details.
 *  attribut BEFORE/AFTER ignoré actuellement
 *
 * @author erwan
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


	JCas aJCas;
	boolean occurrencesOnly;
	boolean detailedTerms;
	LipnExternalProgramGenericAnnotator ae;
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

	// temporary data receivers (SAX parsing)
	// 

	CharArrayWriter content;
	String currentId;
	String currentConfidence;
	String currentLemma;
	String currentHead;
	ArrayList<TermOccurrence> currentOccurrences;
	String currentTermSyntCat;
	ArrayList<String> currentReliableAnchorsTargets;
	String currentSyntAnaHead;
	String currentSyntAnaModifier;
	String currentPrep;
	String currentDet;


	public YateaXMLOutputParser(JCas aJCas, XMLReader parser, LipnExternalProgramGenericAnnotator ae, Logger logger, boolean tagOccurrencesOnly, boolean detailedTerms) throws AnalysisEngineProcessException {
		super();
		this.aJCas = aJCas;
		this.parser = parser;
		locator = new LocatorImpl();
		tokenIndexMapping = YateaAE.initTokenIndexMapping(aJCas);
		localParser = new GenericLocalElementParser(this, parser);
		this.ae = ae;
		this.logger = logger;
		this.occurrencesOnly = tagOccurrencesOnly;
		this.detailedTerms = detailedTerms;
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
			currentLemma = null;
			currentHead = null;
			currentTermSyntCat = null;
			currentReliableAnchorsTargets = null;
			currentSyntAnaHead = null;
			currentSyntAnaModifier = null;
			currentPrep = null;
			currentDet = null;
		} else if (qName.equals(RELIABLE_ANCHOR_TAGNAME) ||
				qName.equals(MORPHOSYNTACTIC_FEATURES_TAGNAME) ||
				qName.equals(SYNTACTIC_ANALYSIS_TAGNAME) ||
				qName.equals(OCCURRENCE_TAGNAME) ) {
			localParser.startLocalParser(qName);
		} else {  // simple element (ID, LEMMA, HEAD, etc)
			content.reset();
		}
	}




	public void receiveLocalParserResult(String elementName, Map<String, String> data, Map<String, Attributes> attrs) throws SAXException {
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
			Double confidence = (data.get(OCCURRENCE_CONFIDENCE_TAGNAME) != null)?Double.valueOf(data.get(OCCURRENCE_CONFIDENCE_TAGNAME)):YateaAE.DEFAULT_CONFIDENCE;
			ae.setGenericAttributes(o, tokenIndexMapping.get(sentenceId).get(start).getBegin(), tokenIndexMapping.get(sentenceId).get(end).getEnd(), ae.getDefaultComponentId(), TermOccurrence.class.getSimpleName(), confidence);
			//System.out.println("Looking for indexes "+start+","+end+" in sentence "+sentenceId);
			currentOccurrences.add(o);
		} else if (elementName.equals(MORPHOSYNTACTIC_FEATURES_TAGNAME)) {
			currentTermSyntCat = data.get(SYNTACTIC_CATEGORY_TAGNAME);
		} else if (elementName.equals(RELIABLE_ANCHOR_TAGNAME)) {
			if (currentReliableAnchorsTargets == null) { // to save space
				currentReliableAnchorsTargets = new ArrayList<String>();
			}
			currentReliableAnchorsTargets.add(data.get(ID_TAGNAME));
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


	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals(TERM_CANDIDATE_TAGNAME)) {
			if (!occurrencesOnly) {
				Term currentTerm = getOrCreateTerm(currentId); // because of forward references
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
						((YateaTerm) currentTerm).setHead(getOrCreateTerm(currentHead));
						((YateaTerm) currentTerm).setSyntacticAnalysisDet(currentDet);
						((YateaTerm) currentTerm).setSyntacticAnalysisHead(getOrCreateTerm(currentSyntAnaHead));
						((YateaTerm) currentTerm).setSyntacticAnalysisModifier(getOrCreateTerm(currentSyntAnaModifier));
						((YateaTerm) currentTerm).setSyntacticAnalysisPrep(currentPrep);
						((YateaTerm) currentTerm).setSyntacticCategory(currentTermSyntCat);
						if (currentReliableAnchorsTargets != null) {
							((YateaTerm) currentTerm).setReliableAnchors(new FSArray(aJCas, currentReliableAnchorsTargets.size()));
							for (int j=0; j < currentReliableAnchorsTargets.size(); j++) {
								((YateaTerm) currentTerm).setReliableAnchors(j, getOrCreateTerm(currentReliableAnchorsTargets.get(j)));
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
		} else if (qName.equals(HEAD_TAGNAME)) {
			currentHead = content.toString().trim();
		} else if (qName.equals(TERM_CONFIDENCE_TAGNAME)) {
			currentConfidence = content.toString().trim();
		} // otherwise don't care about this element

	}



	public void characters( char[] ch, int start, int length ) {
		content.write(ch, start, length);
	}


	
	protected Term getOrCreateTerm(String targetId) {
		Term target;
		if (targetId == null) {
			return null;
		} else {
			if (terms.get(targetId) != null) {
				target = terms.get(targetId);
			} else {
				logger.log(Level.FINE, "Get/create: creating term id="+targetId+"' (current term id="+currentId+")");
				target = detailedTerms?new YateaTerm(aJCas):new Term(aJCas);
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
