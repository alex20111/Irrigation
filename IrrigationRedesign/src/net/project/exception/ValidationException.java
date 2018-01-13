package net.project.exception;


public class ValidationException extends Exception 
{
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. Sets a generic error message.
	 */
	public ValidationException()
	{
		super( "Data Integrity Exception while executing an action." );
	}
	
	/**
	 * Constructor that accepts a error message to provide in the Exception.
	 * 
	 * @param msg
	 * 			Error message to set in the Exception
	 */
	public ValidationException(String msg)
	{
		super( msg );
	}

}
