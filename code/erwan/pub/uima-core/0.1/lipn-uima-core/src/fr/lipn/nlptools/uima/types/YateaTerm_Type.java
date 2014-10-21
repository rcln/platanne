
/* First created by JCasGen Mon Oct 04 10:51:55 GMT 2010 */
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

/** Detailed Yatea term containing additional features
 * Updated by JCasGen Mon Oct 04 15:13:13 GMT 2010
 * @generated */
public class YateaTerm_Type extends Term_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (YateaTerm_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = YateaTerm_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new YateaTerm(addr, YateaTerm_Type.this);
  			   YateaTerm_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new YateaTerm(addr, YateaTerm_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = YateaTerm.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.YateaTerm");
 
  /** @generated */
  final Feature casFeat_SyntacticAnalysisDet;
  /** @generated */
  final int     casFeatCode_SyntacticAnalysisDet;
  /** @generated */ 
  public String getSyntacticAnalysisDet(int addr) {
        if (featOkTst && casFeat_SyntacticAnalysisDet == null)
      jcas.throwFeatMissing("SyntacticAnalysisDet", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getStringValue(addr, casFeatCode_SyntacticAnalysisDet);
  }
  /** @generated */    
  public void setSyntacticAnalysisDet(int addr, String v) {
        if (featOkTst && casFeat_SyntacticAnalysisDet == null)
      jcas.throwFeatMissing("SyntacticAnalysisDet", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setStringValue(addr, casFeatCode_SyntacticAnalysisDet, v);}
    
  
 
  /** @generated */
  final Feature casFeat_SyntacticAnalysisHead;
  /** @generated */
  final int     casFeatCode_SyntacticAnalysisHead;
  /** @generated */ 
  public int getSyntacticAnalysisHead(int addr) {
        if (featOkTst && casFeat_SyntacticAnalysisHead == null)
      jcas.throwFeatMissing("SyntacticAnalysisHead", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getRefValue(addr, casFeatCode_SyntacticAnalysisHead);
  }
  /** @generated */    
  public void setSyntacticAnalysisHead(int addr, int v) {
        if (featOkTst && casFeat_SyntacticAnalysisHead == null)
      jcas.throwFeatMissing("SyntacticAnalysisHead", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setRefValue(addr, casFeatCode_SyntacticAnalysisHead, v);}
    
  
 
  /** @generated */
  final Feature casFeat_SyntacticAnalysisModifier;
  /** @generated */
  final int     casFeatCode_SyntacticAnalysisModifier;
  /** @generated */ 
  public int getSyntacticAnalysisModifier(int addr) {
        if (featOkTst && casFeat_SyntacticAnalysisModifier == null)
      jcas.throwFeatMissing("SyntacticAnalysisModifier", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getRefValue(addr, casFeatCode_SyntacticAnalysisModifier);
  }
  /** @generated */    
  public void setSyntacticAnalysisModifier(int addr, int v) {
        if (featOkTst && casFeat_SyntacticAnalysisModifier == null)
      jcas.throwFeatMissing("SyntacticAnalysisModifier", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setRefValue(addr, casFeatCode_SyntacticAnalysisModifier, v);}
    
  
 
  /** @generated */
  final Feature casFeat_SyntacticCategory;
  /** @generated */
  final int     casFeatCode_SyntacticCategory;
  /** @generated */ 
  public String getSyntacticCategory(int addr) {
        if (featOkTst && casFeat_SyntacticCategory == null)
      jcas.throwFeatMissing("SyntacticCategory", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getStringValue(addr, casFeatCode_SyntacticCategory);
  }
  /** @generated */    
  public void setSyntacticCategory(int addr, String v) {
        if (featOkTst && casFeat_SyntacticCategory == null)
      jcas.throwFeatMissing("SyntacticCategory", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setStringValue(addr, casFeatCode_SyntacticCategory, v);}
    
  
 
  /** @generated */
  final Feature casFeat_SyntacticAnalysisPrep;
  /** @generated */
  final int     casFeatCode_SyntacticAnalysisPrep;
  /** @generated */ 
  public String getSyntacticAnalysisPrep(int addr) {
        if (featOkTst && casFeat_SyntacticAnalysisPrep == null)
      jcas.throwFeatMissing("SyntacticAnalysisPrep", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getStringValue(addr, casFeatCode_SyntacticAnalysisPrep);
  }
  /** @generated */    
  public void setSyntacticAnalysisPrep(int addr, String v) {
        if (featOkTst && casFeat_SyntacticAnalysisPrep == null)
      jcas.throwFeatMissing("SyntacticAnalysisPrep", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setStringValue(addr, casFeatCode_SyntacticAnalysisPrep, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ReliableAnchors;
  /** @generated */
  final int     casFeatCode_ReliableAnchors;
  /** @generated */ 
  public int getReliableAnchors(int addr) {
        if (featOkTst && casFeat_ReliableAnchors == null)
      jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors);
  }
  /** @generated */    
  public void setReliableAnchors(int addr, int v) {
        if (featOkTst && casFeat_ReliableAnchors == null)
      jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setRefValue(addr, casFeatCode_ReliableAnchors, v);}
    
   /** @generated */
  public int getReliableAnchors(int addr, int i) {
        if (featOkTst && casFeat_ReliableAnchors == null)
      jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i);
  }
   
  /** @generated */ 
  public void setReliableAnchors(int addr, int i, int v) {
        if (featOkTst && casFeat_ReliableAnchors == null)
      jcas.throwFeatMissing("ReliableAnchors", "fr.lipn.nlptools.uima.types.YateaTerm");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ReliableAnchors), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_Head;
  /** @generated */
  final int     casFeatCode_Head;
  /** @generated */ 
  public int getHead(int addr) {
        if (featOkTst && casFeat_Head == null)
      jcas.throwFeatMissing("Head", "fr.lipn.nlptools.uima.types.YateaTerm");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Head);
  }
  /** @generated */    
  public void setHead(int addr, int v) {
        if (featOkTst && casFeat_Head == null)
      jcas.throwFeatMissing("Head", "fr.lipn.nlptools.uima.types.YateaTerm");
    ll_cas.ll_setRefValue(addr, casFeatCode_Head, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public YateaTerm_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_SyntacticAnalysisDet = jcas.getRequiredFeatureDE(casType, "SyntacticAnalysisDet", "uima.cas.String", featOkTst);
    casFeatCode_SyntacticAnalysisDet  = (null == casFeat_SyntacticAnalysisDet) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SyntacticAnalysisDet).getCode();

 
    casFeat_SyntacticAnalysisHead = jcas.getRequiredFeatureDE(casType, "SyntacticAnalysisHead", "fr.lipn.nlptools.uima.types.Term", featOkTst);
    casFeatCode_SyntacticAnalysisHead  = (null == casFeat_SyntacticAnalysisHead) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SyntacticAnalysisHead).getCode();

 
    casFeat_SyntacticAnalysisModifier = jcas.getRequiredFeatureDE(casType, "SyntacticAnalysisModifier", "fr.lipn.nlptools.uima.types.Term", featOkTst);
    casFeatCode_SyntacticAnalysisModifier  = (null == casFeat_SyntacticAnalysisModifier) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SyntacticAnalysisModifier).getCode();

 
    casFeat_SyntacticCategory = jcas.getRequiredFeatureDE(casType, "SyntacticCategory", "uima.cas.String", featOkTst);
    casFeatCode_SyntacticCategory  = (null == casFeat_SyntacticCategory) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SyntacticCategory).getCode();

 
    casFeat_SyntacticAnalysisPrep = jcas.getRequiredFeatureDE(casType, "SyntacticAnalysisPrep", "uima.cas.String", featOkTst);
    casFeatCode_SyntacticAnalysisPrep  = (null == casFeat_SyntacticAnalysisPrep) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SyntacticAnalysisPrep).getCode();

 
    casFeat_ReliableAnchors = jcas.getRequiredFeatureDE(casType, "ReliableAnchors", "uima.cas.FSArray", featOkTst);
    casFeatCode_ReliableAnchors  = (null == casFeat_ReliableAnchors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ReliableAnchors).getCode();

 
    casFeat_Head = jcas.getRequiredFeatureDE(casType, "Head", "fr.lipn.nlptools.uima.types.Term", featOkTst);
    casFeatCode_Head  = (null == casFeat_Head) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Head).getCode();

  }
}



    