

/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Simple type for Part-of-Speech annotation.
 * Updated by JCasGen Wed Jun 22 01:01:31 IST 2011
 * XML source: /home/erwan/wip/work/uima-lipn/UIMAv00maq/erwan/dev/lipn-uima-core/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class PartOfSpeech extends AnnotationTag {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PartOfSpeech.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PartOfSpeech() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PartOfSpeech(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PartOfSpeech(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PartOfSpeech(JCas jcas, int begin, int end) {
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

    