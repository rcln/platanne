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
 * 
 * Library about manipulating <code>FSIterator</code> objets.
 * Provides some convenient methods to handle annotations with the LIPN approach.
 * In particular can create thread-safe iterators.
 * 
 * See also {@link ThreadSafeFSIterator}
 * 
 * @author erwan
 *
 */
public class FSIteratorFactory {
	
	public static final String COMPONENT_ID_FEATURE_NAME = "componentId";
	
	/**
	 * 
	 * Parameterized creation of an FSIterator object (possibly thread-safe)
	 * @param aJCas the CAS
	 * @param type the type of annotation (as obtained with <code>myAnnot.type</code>)
	 * @param subIteratorSource if not null, use this Annotation as the super-annotation for
	 *        this iterator: in other words, creates a sub-iterator of this annotation.
	 * @param start if positive, iterates only on annotations A such that A.getBegin() >= start 
	 * @param end if positive, iterates only on annotations A such that A.getEnd() <= end 
	 * @param threadSafe if true, the resulting FSIterator is thread-safe (it can be used
	 *        in different threads running in the same instance inside an AE)
	 * @param constraint if not null, an FSMatchConstraint which can be used to filter
	 *        the annotations this iterator will return.
	 *        
	 * @return the FSIterator
	 */
	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, Annotation subIteratorSource, int start, int end, boolean threadSafe, FSMatchConstraint constraint) {
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
		ConstraintFactory factory = aJCas.getConstraintFactory();
		if ((start >= 0) || (end >= 0)) {
			FSMatchConstraint c = windowConstraint(aJCas, start, end, false);
			if (constraint == null) {
				constraint = c;
			} else {
				constraint = factory.and(constraint, c);
			}
		}
		if (constraint != null) {
			i = aJCas.createFilteredIterator(i, constraint);
		}
		return i;
	}


	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, Annotation subIteratorSource, boolean threadSafe, FSMatchConstraint constraint) {
		return createFSIterator(aJCas, type, subIteratorSource, -1, -1, threadSafe, constraint);
	}

	
	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, Annotation subIteratorSource, boolean threadSafe) {
		return createFSIterator(aJCas, type, subIteratorSource, -1, -1, threadSafe, null);
	}

	public static FSIterator<Annotation> createFSIterator(JCas aJCas, int type, boolean threadSafe) {
		return createFSIterator(aJCas, type, null, -1, -1, threadSafe, null);
	}
	
	
	/*
	 * Returns an iterator satisfying a "window" constraint, i.e. the condition for an annotation A is: 
	 * A.getBegin() &gt;= begin and end &lt;= A.getEnd().
	 * 
	 * Notice that this is different from a sub-iterator: if using a sub-iterator in the case where two annotations 
	 * A and B have the exact same position, the order which can be defined in the AE descriptor 
	 * (see 'priority lists' in UIMA doc) determines whether A < B or A > B. That means that
	 * either a sub-iterator of A will not return B, or the converse.
	 * 
	 * This method uses constraints in order to overcome this limitation of
	 * sub-iterators (but it is also less efficient).
	 * 
	 * @param aJCas the CAS
	 * @param type the type
	 * @param threadSafe whether thread-safe or not
	 * @param windowStart start position
	 * @param windowEnd end position
	 * @param exactMatch if true, the condition becomes A.getBegin() == begin and end == A.getEnd()
	 * @return the FSIterator
	 */
	/*
	public static FSIterator<Annotation> createWindowFSIterator(JCas aJCas, int type, boolean threadSafe, int windowStart, int windowEnd, boolean exactMatch) {
		return createFSIterator(aJCas, type, null, threadSafe, windowConstraint(aJCas, windowStart, windowEnd, exactMatch));
	}
*/	

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
		FSMatchConstraint c = null;
		if (begin >= 0) {
			c = numericFeatureConstraint(aJCas, factory, begin, exactMatch, false, "begin");
		}
		if (end >= 0) {
			FSMatchConstraint c2 = numericFeatureConstraint(aJCas, factory, end, exactMatch, true, "end");
			if (c == null) {
				return c2;
			} else {
				return factory.and(c, c2);
			}
		}
		return c;
	}

	protected static FSMatchConstraint numericFeatureConstraint(JCas aJCas, ConstraintFactory factory, int value, boolean equalOnly, boolean lower, String featureName) {
		FSIntConstraint c = factory.createIntConstraint();
		if (equalOnly) {
			c.eq(value);
		} else {
			if (lower) {
				c.leq(value);
			} else {
				c.geq(value);
			}
		}
		FeaturePath path = aJCas.createFeaturePath();
		path.addFeature(aJCas.getCasType(Annotation.type).getFeatureByBaseName(featureName));
		return factory.embedConstraint(path, c);
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
	 * 
	 * Applies only to 
	 * @param aJCas
	 * @param value
	 * @param threadSafe
	 * @return the constraint
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

	
	/**
	 * 
	 * @param aJCas
	 * @param type
	 * @param threadSafe
	 * @return a constraint such that any annotation must have either be of type t or of type Interpretation (see LIPN TS)
	 */
	public static FSMatchConstraint createTypeOrInterpretationConstraint(JCas aJCas, int type, boolean threadSafe) {
		return createMultiTypesContraint(aJCas, new int[]{type, Interpretation.type}, threadSafe);
	}

	/**
	 * Creates a constraint such that  the annotation A must satisfy <code>A.getFeatureName == featureValue </code>
	 * for all pairs (featureNames[i], featureValues[i]).
	 * The arrays must have the same size, and the annotation type must include all featuresNames.  
	 * 
	 * @param featureNames the list of features names to be compared
	 * @param featureValues the corresponding list of values
	 * @return the resulting constraint, or null if the arrays were empty
	 */
	public static FSMatchConstraint createMultiStringConstraint(JCas aJCas, int type, String [] featureNames, String[] featureValues, boolean threadSafe) {
		ConstraintFactory factory = aJCas.getConstraintFactory();
		if (featureNames.length == 0) {
			return null;
		}
		FSMatchConstraint c = createStringFeatureValueConstraint(aJCas, type, featureNames[0], featureValues[0], threadSafe);
		for (int i=1; i<featureNames.length; i++) {
			c= factory.and(c, createStringFeatureValueConstraint(aJCas, type, featureNames[i], featureValues[i], threadSafe));
		}
		return c;
	}
	
}
