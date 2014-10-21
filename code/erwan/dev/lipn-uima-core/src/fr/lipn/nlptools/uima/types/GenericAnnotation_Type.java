
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Ancestor for all LIPN types
 * Updated by JCasGen Wed Jun 22 01:01:31 IST 2011
 * @generated */
public class GenericAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (GenericAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = GenericAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new GenericAnnotation(addr, GenericAnnotation_Type.this);
  			   GenericAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new GenericAnnotation(addr, GenericAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = GenericAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.lipn.nlptools.uima.types.GenericAnnotation");
 
  /** @generated */
  final Feature casFeat_componentId;
  /** @generated */
  final int     casFeatCode_componentId;
  /** @generated */ 
  public String getComponentId(int addr) {
        if (featOkTst && casFeat_componentId == null)
      jcas.throwFeatMissing("componentId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_componentId);
  }
  /** @generated */    
  public void setComponentId(int addr, String v) {
        if (featOkTst && casFeat_componentId == null)
      jcas.throwFeatMissing("componentId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_componentId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  
 
  /** @generated */
  final Feature casFeat_typeId;
  /** @generated */
  final int     casFeatCode_typeId;
  /** @generated */ 
  public String getTypeId(int addr) {
        if (featOkTst && casFeat_typeId == null)
      jcas.throwFeatMissing("typeId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_typeId);
  }
  /** @generated */    
  public void setTypeId(int addr, String v) {
        if (featOkTst && casFeat_typeId == null)
      jcas.throwFeatMissing("typeId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_typeId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_runId;
  /** @generated */
  final int     casFeatCode_runId;
  /** @generated */ 
  public String getRunId(int addr) {
        if (featOkTst && casFeat_runId == null)
      jcas.throwFeatMissing("runId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_runId);
  }
  /** @generated */    
  public void setRunId(int addr, String v) {
        if (featOkTst && casFeat_runId == null)
      jcas.throwFeatMissing("runId", "fr.lipn.nlptools.uima.types.GenericAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_runId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public GenericAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_componentId = jcas.getRequiredFeatureDE(casType, "componentId", "uima.cas.String", featOkTst);
    casFeatCode_componentId  = (null == casFeat_componentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_componentId).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

 
    casFeat_typeId = jcas.getRequiredFeatureDE(casType, "typeId", "uima.cas.String", featOkTst);
    casFeatCode_typeId  = (null == casFeat_typeId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_typeId).getCode();

 
    casFeat_runId = jcas.getRequiredFeatureDE(casType, "runId", "uima.cas.String", featOkTst);
    casFeatCode_runId  = (null == casFeat_runId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_runId).getCode();

  }
}



    