package org.ray.pack.xml;

/**
 * Parsing exception.
 * 
 * @author jeroenvs
 * @date 16-03-2009
 */
public class ParsingException extends Exception {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = -1038195033481197114L;

	public ParsingException(String message) {
		super(message);
	}
	
	public ParsingException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public ParsingException(Throwable throwable) {
		super(throwable);
	}
	
}
