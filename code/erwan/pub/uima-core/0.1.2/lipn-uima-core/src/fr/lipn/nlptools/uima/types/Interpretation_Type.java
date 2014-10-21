
/* First created by JCasGen Sat Oct 09 20:28:13 CEST 2010 */
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

/** Indicates ambiguity/concurrency/different possible interpretations of annotations: this annotations contains a serie of annotations which make sense together, as opposed to possible other Interpretations, each containing another such serie of annotations.
 * Updated by JCasGen Mon Nov 08 16:54:17 GMT 2010
 * @generated */
public class Interpretation_Type extends GenericAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Interpretation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Interpretation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Interpretation(addr, Interpretation_Type.this);
  			   Interpretation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Interpretation(addr, Interpretation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Interpretation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.Interpretation");
 
  /** @generated */
  final Feature casFeat_serie;
  /** @generated */
  final int     casFeatCode_serie;
  /** @generated */ 
  public int getSerie(int addr) {
        if (featOkTst && casFeat_serie == null)
      jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_serie);
  }
  /** @generated */    
  public void setSerie(int addr, int v) {
        if (featOkTst && casFeat_serie == null)
      jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    ll_cas.ll_setRefValue(addr, casFeatCode_serie, v);}
    
   /** @generated */
  public int getSerie(int addr, int i) {
        if (featOkTst && casFeat_serie == null)
      jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i);
  }
   
  /** @generated */ 
  public void setSerie(int addr, int i, int v) {
        if (featOkTst && casFeat_serie == null)
      jcas.throwFeatMissing("serie", "fr.lipn.nlptools.uima.types.Interpretation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_serie), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Interpretation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_serie = jcas.getRequiredFeatureDE(casType, "serie", "uima.cas.FSArray", featOkTst);
    casFeatCode_serie  = (null == casFeat_serie) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_serie).getCode();

  }
}



    