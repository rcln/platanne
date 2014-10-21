
/* First created by JCasGen Mon Oct 04 13:31:18 GMT 2010 */
package fr.lipn.nlptools.uima.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** adds the Yatea flag "MNP" to a term occurrence ( whether term occurrence is a Maximal Noun Phrase)
 * Updated by JCasGen Wed Jun 22 01:01:32 IST 2011
 * @generated */
public class YateaTermOccurrence_Type extends TermOccurrence_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (YateaTermOccurrence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = YateaTermOccurrence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new YateaTermOccurrence(addr, YateaTermOccurrence_Type.this);
  			   YateaTermOccurrence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new YateaTermOccurrence(addr, YateaTermOccurrence_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = YateaTermOccurrence.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.YateaTermOccurrence");



  /** @generated */
  final Feature casFeat_MaximalNounPhrase;
  /** @generated */
  final int     casFeatCode_MaximalNounPhrase;
  /** @generated */ 
  public String getMaximalNounPhrase(int addr) {
        if (featOkTst && casFeat_MaximalNounPhrase == null)
      jcas.throwFeatMissing("MaximalNounPhrase", "fr.lipn.nlptools.uima.types.YateaTermOccurrence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_MaximalNounPhrase);
  }
  /** @generated */    
  public void setMaximalNounPhrase(int addr, String v) {
        if (featOkTst && casFeat_MaximalNounPhrase == null)
      jcas.throwFeatMissing("MaximalNounPhrase", "fr.lipn.nlptools.uima.types.YateaTermOccurrence");
    ll_cas.ll_setStringValue(addr, casFeatCode_MaximalNounPhrase, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public YateaTermOccurrence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_MaximalNounPhrase = jcas.getRequiredFeatureDE(casType, "MaximalNounPhrase", "uima.cas.String", featOkTst);
    casFeatCode_MaximalNounPhrase  = (null == casFeat_MaximalNounPhrase) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_MaximalNounPhrase).getCode();

  }
}



    