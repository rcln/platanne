package fr.lipn.nlptools.utils.externprog;

public class ExternalProgramException extends Exception {
	/**
	 * Simple exception class for classes related to ExternalProgram.
	 * 
	 * @author moreau
	 *
	 */

	static final long serialVersionUID = 0;

	/**
	 * Basic constructor.
	 * @param msg
	 */
	public ExternalProgramException(String msg) {
		super(msg);
	}

	/**
	 * Constructor with exception stack: catch any other exception and use this constructor with the exception as a cause. 
	 * @param msg
	 * @param cause
	 */
	public ExternalProgramException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

