

/* First created by JCasGen Mon Oct 04 10:53:25 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** contains the term lemma
 * Updated by JCasGen Mon Oct 04 15:13:13 GMT 2010
 * XML source: /users/moreau/wip/uima/svn/UIMAv00maq/erwan/uima-in-progress/desc/fr/lipn/nlptools/uima/yatea/Yatea-TS.xml
 * @generated */
public class TermOccurrence extends AnnotationTag {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(TermOccurrence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TermOccurrence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TermOccurrence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TermOccurrence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TermOccurrence(JCas jcas, int begin, int end) {
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

    