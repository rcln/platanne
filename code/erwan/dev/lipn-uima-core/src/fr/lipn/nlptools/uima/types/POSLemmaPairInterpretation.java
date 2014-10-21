

/* First created by JCasGen Sun Nov 07 17:32:17 CET 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** An Interpretation which consists in a pair PartOfSpeech + Lemma
 * Updated by JCasGen Wed Jun 22 01:01:31 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class POSLemmaPairInterpretation extends Interpretation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(POSLemmaPairInterpretation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected POSLemmaPairInterpretation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public POSLemmaPairInterpretation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public POSLemmaPairInterpretation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public POSLemmaPairInterpretation(JCas jcas, int begin, int end) {
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
     
}

    