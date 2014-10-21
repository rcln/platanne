package fr.lipn.nlptools.utils.externprog;

/**
 * 
 * A special case of ExternalProgramException.
 * Intended to be caught if necessary.
 * 
 * @author moreau
 *
 */
public class TimeOutException extends ExternalProgramException {
	
	static final long serialVersionUID = 0;

	public TimeOutException(String msg) {
		super(msg);
	}

}
