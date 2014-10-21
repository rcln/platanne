

/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Ancestor for all LIPN types
 * Updated by JCasGen Mon Nov 08 16:54:17 GMT 2010
 * XML source: /users/moreau/wip/uima/svn/UIMAv00maq/erwan/dev/uima-core/desc/fr/lipn/nlptools/uima/common/lipn-base-TS.xml
 * @generated */
public class GenericAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(GenericAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected GenericAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public GenericAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public GenericAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public GenericAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: componentId

  /** getter for componentId - gets Identifies the annotator which has added this annotation.
   * @generated */
  public String getComponentId() {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_componentId == null)
      jcasType.jcas.throwFeatMissing("componentId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_componentId);}
    
  /** setter for componentId - sets Identifies the annotator which has added this annotation. 
   * @generated */
  public void setComponentId(String v) {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_componentId == null)
      jcasType.jcas.throwFeatMissing("componentId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_componentId, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets Confidence level
   * @generated */
  public double getConfidence() {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets Confidence level 
   * @generated */
  public void setConfidence(double v) {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_confidence, v);}    
   
    
  //*--------------*
  //* Feature: typeId

  /** getter for typeId - gets 
   * @generated */
  public String getTypeId() {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_typeId == null)
      jcasType.jcas.throwFeatMissing("typeId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_typeId);}
    
  /** setter for typeId - sets  
   * @generated */
  public void setTypeId(String v) {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_typeId == null)
      jcasType.jcas.throwFeatMissing("typeId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_typeId, v);}    
   
    
  //*--------------*
  //* Feature: runId

  /** getter for runId - gets Can contain a user-defined identifier. This id is (usually) written by the AE which creates the annotation, in order to distinguish between different sets of concurrent annotations.
It is especially useful if the same component is used several times (with different parameters for example).
   * @generated */
  public String getRunId() {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_runId == null)
      jcasType.jcas.throwFeatMissing("runId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_runId);}
    
  /** setter for runId - sets Can contain a user-defined identifier. This id is (usually) written by the AE which creates the annotation, in order to distinguish between different sets of concurrent annotations.
It is especially useful if the same component is used several times (with different parameters for example). 
   * @generated */
  public void setRunId(String v) {
    if (GenericAnnotation_Type.featOkTst && ((GenericAnnotation_Type)jcasType).casFeat_runId == null)
      jcasType.jcas.throwFeatMissing("runId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((GenericAnnotation_Type)jcasType).casFeatCode_runId, v);}    
  }

    