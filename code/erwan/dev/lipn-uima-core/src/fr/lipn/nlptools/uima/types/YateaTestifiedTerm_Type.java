
/* First created by JCasGen Wed Jun 22 01:01:32 IST 2011 */
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

/** Special kind of term to handle Yatea testified terms, which do not appear as normal terms.
These terms do not link to any TermOccurrence and are assigned position 0-0, but they can be linked to by other terms (through ReliableAnchors in YateaTerm)
 * Updated by JCasGen Wed Jun 22 01:01:32 IST 2011
 * @generated */
public class YateaTestifiedTerm_Type extends Term_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (YateaTestifiedTerm_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = YateaTestifiedTerm_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new YateaTestifiedTerm(addr, YateaTestifiedTerm_Type.this);
  			   YateaTestifiedTerm_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new YateaTestifiedTerm(addr, YateaTestifiedTerm_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = YateaTestifiedTerm.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.YateaTestifiedTerm");
 
  /** @generated */
  final Feature casFeat_Form;
  /** @generated */
  final int     casFeatCode_Form;
  /** @generated */ 
  public String getForm(int addr) {
        if (featOkTst && casFeat_Form == null)
      jcas.throwFeatMissing("Form", "fr.lipn.nlptools.uima.types.YateaTestifiedTerm");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Form);
  }
  /** @generated */    
  public void setForm(int addr, String v) {
        if (featOkTst && casFeat_Form == null)
      jcas.throwFeatMissing("Form", "fr.lipn.nlptools.uima.types.YateaTestifiedTerm");
    ll_cas.ll_setStringValue(addr, casFeatCode_Form, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public YateaTestifiedTerm_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Form = jcas.getRequiredFeatureDE(casType, "Form", "uima.cas.String", featOkTst);
    casFeatCode_Form  = (null == casFeat_Form) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Form).getCode();

  }
}



    