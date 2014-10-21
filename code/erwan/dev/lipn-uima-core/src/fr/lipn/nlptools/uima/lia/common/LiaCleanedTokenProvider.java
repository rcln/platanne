package fr.lipn.nlptools.uima.lia.common;

import java.util.Iterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.common.FSIteratorFactory;
import fr.lipn.nlptools.uima.types.LiaCleaned;

/**
 * 
 * This class is intended only to specialize the behaviour of a {@link fr.lipn.nlptools.uima.common.GenericTokenIterator} 
 * to the LIA case: the specificity is that when a <code>Cleaned</code> annotation exists 
 * for a given token, the token text to return is not the covered text but the data contained
 * in this annotation.
 * 
 * @author moreau
 *
 */
public class LiaCleanedTokenProvider {

	boolean cleanedVersion;
	boolean threadSafe;
	
	public LiaCleanedTokenProvider(boolean cleanedVersion, boolean threadSafe) {
		this.cleanedVersion = cleanedVersion;
		this.threadSafe = threadSafe;
	}
	
	public String getTokenText(JCas aJCas, Annotation token) {
		if (cleanedVersion) {
			Iterator<Annotation> iter = FSIteratorFactory.createFSIterator(aJCas, LiaCleaned.type, token, threadSafe);
			if (iter.hasNext()) {
				return ((LiaCleaned) iter.next()).getValue();
			} else {
				return token.getCoveredText();
			}
		} else {
			return token.getCoveredText();
		}

	}
	
}
