package net.project.web.interceptors;

import java.util.Map;

import net.project.common.Constants;

import org.apache.struts2.dispatcher.SessionMap;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * @title LoginInterceptor.java
 * 
 * @description
 * 
 *		Interceptor that will verify that the user is logged
 *		in for actions that require the user to be logged in
 *		to execute.
 */
public class LoginInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Method executed by Struts when the interceptor is invoked.
	 * 
	 * @param invocation
	 * 			Object containing attributes related to the action invocation
	 * 
	 * @return Name of result to execute
	 * 
	 */
	@Override
	public String intercept(ActionInvocation invocation) throws Exception
	{	
		//
		// Get the action context from the invocation so we can access
		// the session information
		//
		final ActionContext context = invocation.getInvocationContext ();
		
		Map<String, Object> session = context.getSession();	
		
		String actionName = invocation.getProxy().getActionName();
		
		// Check if there is a user information object in the session
		Object user = session.get( Constants.USER );
		if (user == null && Constants.mustLogin)
		{	
			// The user has not logged in yet.

			// Verify if the user is attempting to log in, by
			// verifying if the name of the action being processed
			// is the "login" action.
			//
			if ( ( actionName != null ) && ( actionName.equals( "loginUser" ) ) )
			{
				//before logging in, invalidate any session that the user might have.				
				((SessionMap<String,Object>)session).invalidate();
				//
				// The user is attempting to log in.
				// Process the user's login attempt.
				return invocation.invoke();
			}	

			//
			// Either the login attempt failed or the user hasn't tried to login yet, 
			// and we need to send the login form.
			//
			return "login";
		}
		else
		{			
			//
			// User is logged in, so proceed with the execution
			//
			return invocation.invoke();
		}
	}
}
