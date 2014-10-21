

/* First created by JCasGen Wed Jun 22 01:01:32 IST 2011 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Special kind of term to handle Yatea testified terms, which do not appear as normal terms.
These terms do not link to any TermOccurrence and are assigned position 0-0, but they can be linked to by other terms (through ReliableAnchors in YateaTerm)
 * Updated by JCasGen Wed Jun 22 01:01:32 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class YateaTestifiedTerm extends Term {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(YateaTestifiedTerm.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected YateaTestifiedTerm() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public YateaTestifiedTerm(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public YateaTestifiedTerm(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public YateaTestifiedTerm(JCas jcas, int begin, int end) {
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
  //* Feature: Form

  /** getter for Form - gets 
   * @generated */
  public String getForm() {
    if (YateaTestifiedTerm_Type.featOkTst && ((YateaTestifiedTerm_Type)jcasType).casFeat_Form == null)
      jcasType.jcas.throwFeatMissing("Form", "fr.lipn.nlptools.uima.types.YateaTestifiedTerm");
    return jcasType.ll_cas.ll_getStringValue(addr, ((YateaTestifiedTerm_Type)jcasType).casFeatCode_Form);}
    
  /** setter for Form - sets  
   * @generated */
  public void setForm(String v) {
    if (YateaTestifiedTerm_Type.featOkTst && ((YateaTestifiedTerm_Type)jcasType).casFeat_Form == null)
      jcasType.jcas.throwFeatMissing("Form", "fr.lipn.nlptools.uima.types.YateaTestifiedTerm");
    jcasType.ll_cas.ll_setStringValue(addr, ((YateaTestifiedTerm_Type)jcasType).casFeatCode_Form, v);}    
  }

    