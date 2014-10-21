

/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Standard annotation.
 * Updated by JCasGen Wed Jun 22 01:01:31 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class AnnotationTag extends GenericAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(AnnotationTag.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AnnotationTag() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public AnnotationTag(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public AnnotationTag(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public AnnotationTag(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets Detailed information (of any kind) about this annotation.
   * @generated */
  public String getValue() {
    if (AnnotationTag_Type.featOkTst && ((AnnotationTag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "fr.lipn.nlptools.uima.types.AnnotationTag");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AnnotationTag_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets Detailed information (of any kind) about this annotation. 
   * @generated */
  public void setValue(String v) {
    if (AnnotationTag_Type.featOkTst && ((AnnotationTag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "fr.lipn.nlptools.uima.types.AnnotationTag");
    jcasType.ll_cas.ll_setStringValue(addr, ((AnnotationTag_Type)jcasType).casFeatCode_value, v);}    
  }

    