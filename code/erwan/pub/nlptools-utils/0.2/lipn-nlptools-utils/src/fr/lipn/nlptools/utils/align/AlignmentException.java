package fr.lipn.nlptools.utils.align;

/**
 * Simple exception class for this package.
 * All classes in the package must send only this kind of exception: other exceptions can be put in the 
 * stack using the second form of the constructor.
 * 
 * @author moreau
 *
 */
public class AlignmentException extends Exception {
	
	static final long serialVersionUID = 0;
	
	/**
	 * Basic constructor.
	 * @param msg
	 */
	public AlignmentException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor with exception stack: catch any other exception and use this constructor with the exception as a cause. 
	 * @param msg
	 * @param cause
	 */
	public AlignmentException(String msg, Exception cause) {
		super(msg, cause);
	}

}
