
/* First created by JCasGen Sun Nov 07 17:32:17 CET 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** 
An Interpretation which consists in a pair PartOfSpeech + Lemma
 * Updated by JCasGen Sun Nov 07 17:32:17 CET 2010
 * @generated */
public class POSLemmaPairInterpretation_Type extends Interpretation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (POSLemmaPairInterpretation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = POSLemmaPairInterpretation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new POSLemmaPairInterpretation(addr, POSLemmaPairInterpretation_Type.this);
  			   POSLemmaPairInterpretation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new POSLemmaPairInterpretation(addr, POSLemmaPairInterpretation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = POSLemmaPairInterpretation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.POSLemmaPairInterpretation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public POSLemmaPairInterpretation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    