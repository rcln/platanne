

/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



import org.apache.uima.jcas.cas.FSArray;


/** Detailed Yatea term containing additional features
 * Updated by JCasGen Wed Jun 22 01:01:32 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class YateaTerm extends Term {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(YateaTerm.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected YateaTerm() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public YateaTerm(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public YateaTerm(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public YateaTerm(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: SyntacticAnalysisDet

  /** getter for SyntacticAnalysisDet - gets 
   * @generated */
  public String getSyntacticAnalysisDet() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisDet == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisDet", "fr.lipn.nlptools.uima.types.YateaTerm");
    return jcasType.ll_cas.ll_getStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisDet);}
    
  /** setter for SyntacticAnalysisDet - sets  
   * @generated */
  public void setSyntacticAnalysisDet(String v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisDet == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisDet", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisDet, v);}    
   
    
  //*--------------*
  //* Feature: SyntacticAnalysisHead

  /** getter for SyntacticAnalysisHead - gets 
   * @generated */
  public Term getSyntacticAnalysisHead() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisHead == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisHead", "fr.lipn.nlptools.uima.types.YateaTerm");
    return (Term)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisHead)));}
    
  /** setter for SyntacticAnalysisHead - sets  
   * @generated */
  public void setSyntacticAnalysisHead(Term v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisHead == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisHead", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisHead, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: SyntacticAnalysisModifier

  /** getter for SyntacticAnalysisModifier - gets 
   * @generated */
  public Term getSyntacticAnalysisModifier() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisModifier == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisModifier", "fr.lipn.nlptools.uima.types.YateaTerm");
    return (Term)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisModifier)));}
    
  /** setter for SyntacticAnalysisModifier - sets  
   * @generated */
  public void setSyntacticAnalysisModifier(Term v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisModifier == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisModifier", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisModifier, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: SyntacticCategory

  /** getter for SyntacticCategory - gets 
   * @generated */
  public String getSyntacticCategory() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticCategory == null)
      jcasType.jcas.throwFeatMissing("SyntacticCategory", "fr.lipn.nlptools.uima.types.YateaTerm");
    return jcasType.ll_cas.ll_getStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticCategory);}
    
  /** setter for SyntacticCategory - sets  
   * @generated */
  public void setSyntacticCategory(String v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticCategory == null)
      jcasType.jcas.throwFeatMissing("SyntacticCategory", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticCategory, v);}    
   
    
  //*--------------*
  //* Feature: SyntacticAnalysisPrep

  /** getter for SyntacticAnalysisPrep - gets 
   * @generated */
  public String getSyntacticAnalysisPrep() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisPrep == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisPrep", "fr.lipn.nlptools.uima.types.YateaTerm");
    return jcasType.ll_cas.ll_getStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisPrep);}
    
  /** setter for SyntacticAnalysisPrep - sets  
   * @generated */
  public void setSyntacticAnalysisPrep(String v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_SyntacticAnalysisPrep == null)
      jcasType.jcas.throwFeatMissing("SyntacticAnalysisPrep", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setStringValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_SyntacticAnalysisPrep, v);}    
   
    
  //*--------------*
  //* Feature: ReliableAnchors

  /** getter for ReliableAnchors - gets 
   * @generated */
  public FSArray getReliableAnchors() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_ReliableAnchors == null)
      jcasType.jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors)));}
    
  /** setter for ReliableAnchors - sets  
   * @generated */
  public void setReliableAnchors(FSArray v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_ReliableAnchors == null)
      jcasType.jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for ReliableAnchors - gets an indexed value - 
   * @generated */
  public Term getReliableAnchors(int i) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_ReliableAnchors == null)
      jcasType.jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors), i);
    return (Term)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors), i)));}

  /** indexed setter for ReliableAnchors - sets an indexed value - 
   * @generated */
  public void setReliableAnchors(int i, Term v) { 
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_ReliableAnchors == null)
      jcasType.jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_ReliableAnchors), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: Head

  /** getter for Head - gets 
   * @generated */
  public Term getHead() {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_Head == null)
      jcasType.jcas.throwFeatMissing("Head", "fr.lipn.nlptools.uima.types.YateaTerm");
    return (Term)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_Head)));}
    
  /** setter for Head - sets  
   * @generated */
  public void setHead(Term v) {
    if (YateaTerm_Type.featOkTst && ((YateaTerm_Type)jcasType).casFeat_Head == null)
      jcasType.jcas.throwFeatMissing("Head", "fr.lipn.nlptools.uima.types.YateaTerm");
    jcasType.ll_cas.ll_setRefValue(addr, ((YateaTerm_Type)jcasType).casFeatCode_Head, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    