

/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Sun Nov 07 17:32:16 CET 2010
 * XML source: /home/erwan/wip/svn_uima/UIMAv00maq/erwan/dev/uima-core/desc/fr/lipn/nlptools/uima/common/lipn-standard-TS.xml
 * @generated */
public class DocumentDivision extends Segment {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocumentDivision.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentDivision() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentDivision(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentDivision(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentDivision(JCas jcas, int begin, int end) {
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

    