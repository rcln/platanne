package fr.lipn.nlptools.uima.common;

import fr.lipn.nlptools.uima.common.ThreadSafeFSIterator;
import fr.lipn.nlptools.uima.types.GenericAnnotation;
import fr.lipn.nlptools.uima.types.Interpretation;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FSStringConstraint;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * TODO doc
 * @author erwan
 *
 */
public class FSIteratorFactory {
	
	public static final String COMPONENT_ID_FEATURE_NAME = "componentId";
	
	
	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, Annotation subIteratorSource, boolean threadSafe, FSMatchConstraint constraint) {
		FSIterator<Annotation> i;
		if (threadSafe) {
			i = new ThreadSafeFSIterator(aJCas, type, subIteratorSource);
		} else {
			if (subIteratorSource == null) {
				i = aJCas.getAnnotationIndex(type).iterator();
			} else {
				i = aJCas.getAnnotationIndex(type).subiterator(subIteratorSource);
			}
		}
		if (constraint == null) {
			return i;
		} else {
			return aJCas.createFilteredIterator(i, constraint);
		}
	}

	
	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, Annotation subIteratorSource, boolean threadSafe) {
		return createFSIterator(aJCas, type, subIteratorSource, threadSafe, null);
	}

	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, boolean threadSafe) {
		return createFSIterator(aJCas, type, null, threadSafe, null);
	}
	
	/**
	 * Returns an iterator satisfying a "window" constraint, i.e. the condition for an annotation A is: 
	 * A.getBegin() &gt;= begin and end &lt;= A.getEnd().
	 * 
	 * @param aJCas
	 * @param type
	 * @param threadSafe
	 * @param windowStart
	 * @param windowEnd
	 * @param exactMatch if true, the condition becomes A.getBegin() == begin and end == A.getEnd()
	 * @return
	 */
	public static FSIterator<Annotation> createWindowFSIterator(JCas aJCas, int type, boolean threadSafe, int windowStart, int windowEnd, boolean exactMatch) {
		return createFSIterator(aJCas, type, null, threadSafe, windowConstraint(aJCas, windowStart, windowEnd, exactMatch));
	}
	

	/**
	 * Creates a "window" constraint, i.e. the condition for an annotation A is: <code>A.getBegin() &gt;= begin and end &lt;= A.getEnd().</code>
	 *   
	 * @param aJCas
	 * @param begin
	 * @param end
	 * @param exactMatch if true, the condition becomes <code>A.getBegin() == begin and end == A.getEnd()</code>
	 * @return the resulting constraint
	 */
	protected static FSMatchConstraint windowConstraint(JCas aJCas, int begin, int end, boolean exactMatch) {
		ConstraintFactory factory = aJCas.getConstraintFactory();
		FSIntConstraint beginConstraint = factory.createIntConstraint();
		FSIntConstraint endConstraint = factory.createIntConstraint();
		if (exactMatch) {
			beginConstraint.eq(begin);
			endConstraint.eq(end);
		} else {
			beginConstraint.geq(begin);
			endConstraint.leq(end);
		}
		FeaturePath beginPath = aJCas.createFeaturePath();
		FeaturePath endPath = aJCas.createFeaturePath();
		beginPath.addFeature(aJCas.getCasType(Annotation.type).getFeatureByBaseName("begin"));
		endPath.addFeature(aJCas.getCasType(Annotation.type).getFeatureByBaseName("end"));
		return factory.and(factory.embedConstraint(beginPath, beginConstraint), factory.embedConstraint(endPath, endConstraint));
	}

	/**
	 * Creates a "window" constraint, i.e. the condition for an annotation A is: <code>A.getBegin() &gt;= begin and end &lt;= A.getEnd().</code>
	 *   
	 * @param aJCas
	 * @param begin
	 * @param end
	 * @param exactMatch if true, the condition becomes <code>A.getBegin() == begin and end == A.getEnd()</code>
	 * @param threadSafe
	 * @return the resulting constraint
	 */
	public static FSMatchConstraint createWindowConstraint(JCas aJCas, int begin, int end, boolean exactMatch, boolean threadSafe) {
		if (threadSafe) {
			synchronized(aJCas) {
				return windowConstraint(aJCas, begin, end, exactMatch);
			}
		} else {
			return windowConstraint(aJCas, begin, end, exactMatch);
		}
	}


	/**
	 * Creates a String filtering constraint using feature <code>featureName</code>: the annotation A must satisfy
	 * <code>A.getFeatureName == value </code>
	 * 
	 * @param aJCas
	 * @param type the type on which constraints will be applied. Must have a featureName feature.
	 * @param featureName the feature name, which must be valid for the type.
	 * @param value the value that the feature must match.
	 * @return the resulting constraint
	 */
	protected static FSMatchConstraint stringFeatureValueConstraint(JCas aJCas, int type, String featureName, String value) {
		ConstraintFactory factory = aJCas.getConstraintFactory();
		FSStringConstraint stringConstraint = factory.createStringConstraint();
		stringConstraint.equals(value);
		FeaturePath path = aJCas.createFeaturePath();
		path.addFeature(aJCas.getCasType(type).getFeatureByBaseName(featureName));
		return factory.embedConstraint(path, stringConstraint);
	}

	/**
	 * Creates a String filtering constraint using feature <code>featureName</code>: the annotation A must satisfy
	 * <code>A.getFeatureName == value </code>
	 * 
	 * @param aJCas
	 * @param type the type on which constraints will be applied. Must have a featureName feature.
	 * @param featureName the feature name, which must be valid for the type.
	 * @param value the value that the feature must match.
	 * @param threadSafe
	 * @return the resulting constraint
	 */
	public static FSMatchConstraint createStringFeatureValueConstraint(JCas aJCas, int type, String featureName, String value, boolean threadSafe) {
		if (threadSafe) {
			synchronized(aJCas) {
				return stringFeatureValueConstraint(aJCas, type, featureName, value);
			}
		} else {
			return stringFeatureValueConstraint(aJCas, type, featureName, value);
		}
	}


	/**
	 * Filter constraint: feature <code>componentId</code> must be equal to <code>value</code>
	 * Applies only to 
	 * @param aJCas
	 * @param value
	 * @param threadSafe
	 * @return
	 */
	public static FSMatchConstraint createComponentIdFilterConstraint(JCas aJCas, String value, boolean threadSafe) {
		return createStringFeatureValueConstraint(aJCas, GenericAnnotation.type, COMPONENT_ID_FEATURE_NAME, value, threadSafe);
	}

	/**
	 * 
	 * @param aJCas
	 * @param types
	 * @return returns a constraint that will match any annotation of type t, t in <code>types</code>
	 */
	protected static FSMatchConstraint multiTypesContraint(JCas aJCas, int[] types) {
		ConstraintFactory factory = aJCas.getConstraintFactory();
		FSTypeConstraint typeConstraint = factory.createTypeConstraint();
		for (int i=0; i < types.length; i++) {
			typeConstraint.add(aJCas.getCasType(types[i]));
		}
		return typeConstraint;
	}

	/**
	 * 
	 * @param aJCas
	 * @param types
	 * @return returns a constraint that will match any annotation of type t, t in <code>types</code>
	 */
	public static FSMatchConstraint createMultiTypesContraint(JCas aJCas, int[] types, boolean threadSafe) {
		if (threadSafe) {
			synchronized(aJCas) {
				return multiTypesContraint(aJCas, types);
			}
		} else {
			return multiTypesContraint(aJCas, types);
		}
	}

	
	public static FSMatchConstraint createTypeOrInterpretationConstraint(JCas aJCas, int type, boolean threadSafe) {
		return createMultiTypesContraint(aJCas, new int[]{type, Interpretation.type}, threadSafe);
	}

}
