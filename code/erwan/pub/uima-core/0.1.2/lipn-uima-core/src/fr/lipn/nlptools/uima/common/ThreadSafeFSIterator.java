package fr.lipn.nlptools.uima.common;


import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * 
 * This class implements a thread-safe FSIterator. 
 * It is intended to be used in the case where there are concurrent access to the CAS.
 * The UIMA standard CAS implementation IS NOT thread-safe, concurrency errors may happen and cause failure of the process.
 * Notice that you must also protect (synchronize) any other CAS access (e.g. creating/adding annotations)
 * Implementation information: this class actually simply wraps a standard FSIterator and protects (synchronize) any call to its methods.
 * 
 *  IMPORTANT: Use one of the methods in {@link FSIteratorFactory} to instantiate a new ThreadSafeFSIterator. 
 * 
 * @author moreau
 *
 */
public class ThreadSafeFSIterator implements FSIterator<Annotation> {
	
	JCas aJCas;
	protected FSIterator<Annotation> iterator;
	

	/**
	 * @param aJCas
	 * @param type type of annotation to iterate
	 * @param range if not null, a subiterator of this Annotation is returned
	 */
	protected ThreadSafeFSIterator(JCas aJCas, int type, Annotation range) {
		this.aJCas = aJCas;
		synchronized (aJCas) {
			if (range == null) {
				iterator = aJCas.getAnnotationIndex(type).iterator();
			} else {
				iterator = aJCas.getAnnotationIndex(type).subiterator(range);
			}
		}
	}

	/**
	 * @param aJCas
	 * @param type type of annotation to iterate
	 */
	protected ThreadSafeFSIterator(JCas aJCas, int type) {
		this(aJCas, type, null);
	}

	
	protected ThreadSafeFSIterator(FSIterator<Annotation> i) {
		iterator = i;
	}
	
	public boolean hasNext() {
		synchronized (aJCas) {
			return iterator.hasNext();
		}
	}
	
	public Annotation next() {
		synchronized (aJCas) {
			return iterator.next();
		}
	}
	
	public void moveToNext() {
		synchronized (aJCas) {
			iterator.moveToNext();
		}
	}
	
	public synchronized void moveToLast() {
		synchronized (aJCas) {
			iterator.moveToLast();
		}
	}

	public synchronized void moveToFirst() {
		synchronized (aJCas) {
			iterator.moveToFirst();
		}
	}

	public synchronized void moveToPrevious() {
		synchronized (aJCas) {
			iterator.moveToPrevious();
		}
	}
	
	public synchronized void moveTo(FeatureStructure fs) {
		synchronized (aJCas) {
			iterator.moveTo(fs);
		}
	}

	public synchronized boolean isValid() {
		synchronized (aJCas) {
			return iterator.isValid();
		}
	}

	public synchronized Annotation get() {
		synchronized (aJCas) {
			return iterator.get();
		}
	}
	
	public synchronized FSIterator<Annotation> copy() {
		synchronized (aJCas) {
			return new ThreadSafeFSIterator(iterator.copy());
		}
	}
	
	public synchronized void remove() {
		synchronized (aJCas) {
			iterator.remove();
		}
	}
}