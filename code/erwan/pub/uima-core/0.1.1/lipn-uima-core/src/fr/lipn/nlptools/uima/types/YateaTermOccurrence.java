

/* First created by JCasGen Mon Oct 04 13:31:18 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** adds the Yatea flag "MNP" to a term occurrence ( whether term occurrence is a Maximal Noun Phrase)

 * Updated by JCasGen Mon Oct 04 15:13:13 GMT 2010
 * XML source: /users/moreau/wip/uima/svn/UIMAv00maq/erwan/uima-in-progress/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class YateaTermOccurrence extends TermOccurrence {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(YateaTermOccurrence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected YateaTermOccurrence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public YateaTermOccurrence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public YateaTermOccurrence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public YateaTermOccurrence(JCas jcas, int begin, int end) {
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
  //* Feature: MaximalNounPhrase

  /** getter for MaximalNounPhrase - gets 
   * @generated */
  public String getMaximalNounPhrase() {
    if (YateaTermOccurrence_Type.featOkTst && ((YateaTermOccurrence_Type)jcasType).casFeat_MaximalNounPhrase == null)
      jcasType.jcas.throwFeatMissing("MaximalNounPhrase", "fr.lipn.nlptools.uima.types.YateaTermOccurrence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((YateaTermOccurrence_Type)jcasType).casFeatCode_MaximalNounPhrase);}
    
  /** setter for MaximalNounPhrase - sets  
   * @generated */
  public void setMaximalNounPhrase(String v) {
    if (YateaTermOccurrence_Type.featOkTst && ((YateaTermOccurrence_Type)jcasType).casFeat_MaximalNounPhrase == null)
      jcasType.jcas.throwFeatMissing("MaximalNounPhrase", "fr.lipn.nlptools.uima.types.YateaTermOccurrence");
    jcasType.ll_cas.ll_setStringValue(addr, ((YateaTermOccurrence_Type)jcasType).casFeatCode_MaximalNounPhrase, v);}    
  }

    