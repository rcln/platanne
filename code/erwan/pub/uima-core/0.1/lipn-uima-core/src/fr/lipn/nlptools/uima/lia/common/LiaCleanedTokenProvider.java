package fr.lipn.nlptools.uima.lia.common;

import java.util.Iterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import fr.lipn.nlptools.uima.common.FSIteratorFactory;
import fr.lipn.nlptools.uima.types.LiaCleaned;

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
