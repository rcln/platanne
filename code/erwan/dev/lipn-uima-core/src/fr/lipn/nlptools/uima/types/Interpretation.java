

/* First created by JCasGen Sat Oct 09 20:28:13 CEST 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;


/** Indicates ambiguity/concurrency/different possible interpretations of annotations: this annotations contains a serie of annotations which make sense together, as opposed to possible other Interpretations, each containing another such serie of annotations.
 * Updated by JCasGen Wed Jun 22 01:01:31 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class Interpretation extends GenericAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Interpretation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Interpretation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Interpretation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Interpretation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Interpretation(JCas jcas, int begin, int end) {
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
  //* Feature: serie

  /** getter for serie - gets References to the annotations which belong to this serie/interpretation
   * @generated */
  public FSArray getSerie() {
    if (Interpretation_Type.featOkTst && ((Interpretation_Type)jcasType).casFeat_serie == null)
      jcasType.jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie)));}
    
  /** setter for serie - sets References to the annotations which belong to this serie/interpretation 
   * @generated */
  public void setSerie(FSArray v) {
    if (Interpretation_Type.featOkTst && ((Interpretation_Type)jcasType).casFeat_serie == null)
      jcasType.jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for serie - gets an indexed value - References to the annotations which belong to this serie/interpretation
   * @generated */
  public GenericAnnotation getSerie(int i) {
    if (Interpretation_Type.featOkTst && ((Interpretation_Type)jcasType).casFeat_serie == null)
      jcasType.jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie), i);
    return (GenericAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie), i)));}

  /** indexed setter for serie - sets an indexed value - References to the annotations which belong to this serie/interpretation
   * @generated */
  public void setSerie(int i, GenericAnnotation v) { 
    if (Interpretation_Type.featOkTst && ((Interpretation_Type)jcasType).casFeat_serie == null)
      jcasType.jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Interpretation_Type)jcasType).casFeatCode_serie), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    