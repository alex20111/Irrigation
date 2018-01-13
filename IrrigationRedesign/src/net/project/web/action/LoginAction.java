package net.project.web.action;

import home.crypto.Encryptor;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.project.common.Constants;
import net.project.common.SendEmail;
import net.project.common.Utils;
import net.project.db.entities.Config;
import net.project.db.entities.User;
import net.project.db.manager.ConfigManager;
import net.project.db.manager.UserManager;
import net.project.exception.ValidationException;
import net.project.security.PassHash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;


import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware, ServletRequestAware, ServletResponseAware{ 

	private Log log = LogFactory.getLog(getClass());
	
	private static final long serialVersionUID = 1L;
	
	private final String  USER_NAME_COOKIE 	= "user";
	private final String  PASSWORD_COOKIE 	= "secret";
	private final String  REMEMBER_COOKIE 	= "remember";

	private String userName = "";
	private String password = "";
	private Boolean remember = false;
	
	private Map<String, Object> session;

	private HttpServletRequest request;
	private HttpServletResponse response;	
	
	private UserManager userManager;
	
	private String newPassword = "";
	private String confirmPassword = "";

	public LoginAction(){
		userManager = new UserManager();
	}

	
	public String loadLoginPage(){

		try{
			boolean cookieRemember = false;
			//look for the cookie
			Cookie rememberC = getCookie(REMEMBER_COOKIE);

			if (rememberC != null){
				cookieRemember = ("true".equals(rememberC.getValue()) ? true : false ) ;
				setRemember(cookieRemember);
			}
			
			if (cookieRemember){
				//look for the cookie
				Cookie userNameC = getCookie(USER_NAME_COOKIE);

				if (userNameC != null){
					setUserName(userNameC.getValue());
				}
				//look for the cookie
				Cookie passCookie = getCookie(PASSWORD_COOKIE);
				
				if (passCookie != null){
					setPassword(Encryptor.decryptString(passCookie.getValue(), Constants.cookieKey.toCharArray()));
				}				
			}
		}catch (Exception ex){
			log.error("Error in login" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return SUCCESS;
	}
	
	public String login(){

		String retVal = "login";

		try{
			
			log.info("User " + getUserName() + " login from : " + request.getRemoteAddr());
			
			if (getUserName().trim().length() == 0){
				addFieldError("userName", "Please enter a user name");
			}
			if (getPassword().trim().length() == 0){
				addFieldError("password", "Please enter a password");
			}

			if (!hasFieldErrors()){
				
				if (remember){
					//cookie for userName
					Cookie cookieUserName = new Cookie(USER_NAME_COOKIE, getUserName());
					cookieUserName.setMaxAge(60*60*24*365); // Make the cookie last a year!
					response.addCookie(cookieUserName);			
					
					//cookie for remember
					Cookie cookieRemember = new Cookie(REMEMBER_COOKIE, ( remember? "true" : "false" ) );
					cookieRemember.setMaxAge(60*60*24*365);				
					response.addCookie(cookieRemember);	
					
					//cookie for remember
					Cookie cookiePass = new Cookie(PASSWORD_COOKIE, Encryptor.encryptString(getPassword(), Constants.cookieKey.toCharArray()) );
					cookiePass.setMaxAge(60*60*24*365);				
					response.addCookie(cookiePass);	
					
				}else{
					//remove cookies
					Cookie userNameC = getCookie(USER_NAME_COOKIE);
					if (userNameC != null){
						userNameC.setMaxAge(0);
					}
					Cookie rememberC = getCookie(REMEMBER_COOKIE);
					if (rememberC != null){
						rememberC.setMaxAge(0);
					}
					Cookie cookiePass = getCookie(PASSWORD_COOKIE);
					if (cookiePass != null){
						cookiePass.setMaxAge(0);
					}
				}
				
				//check login
				User user = userManager.loginUser(getUserName(), getPassword());

				if (user != null){				
					session.put(Constants.USER, user);	
					
					Config c = new ConfigManager().loadConfig();
					
					if (c != null && c.isActivateWarning() && c.isNotiUserLogin()){
						StringBuilder sb = new StringBuilder("<html>");
						sb.append("<body>");
						sb.append("<p> <strong>User: </strong>" +  user.getUserName() + " Has logged in </p>");
						sb.append("<p> <strong>Date/Time: " + new Date() + " </p>");
						sb.append("</body>");
						sb.append("</html>");
						try {
							SendEmail.send("User : " +  user.getUserName() + " Logged in.",sb.toString(), c);
						} catch (Exception e) {
							log.error("Error sending mail for User Login: " + e);
						}
					}
					
					
					retVal = "loggedIn";
				}else{
					addActionError("Invalid user or password");
				}
			}
		} catch (ValidationException e) {

			if(e.getMessage().equals("Password empty")){
				addActionError("Please enter password");
			}
			if(e.getMessage().equals("userName empty")){
				addFieldError("userName","Please enter a User Name");	
			}
			if(e.getMessage().equals("Invalid password")){
				addFieldError("password", "Wrong Password");
			}
			if(e.getMessage().equals("User over maximum number of tries")){
				addActionError("Exceeded maximum number of tries. Please contact Administrator.");
			}

		}catch(Exception ex){
			log.error("Error in login action: ", ex);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}

	public String logout()
	{
		if (session instanceof SessionMap)
		{
			((SessionMap<String,Object>)session).invalidate();
		}
		
		return "success";
	}
	public String changePass(){
		
		User user = (User) session.get(Constants.USER);
		
		try {
			
			if (getNewPassword().trim().length() == 0){
				addFieldError("newpass","Please enter a new password");
			}
			if (getConfirmPassword().trim().length() == 0){
				addFieldError("confpass","Please confirm the new password");
			}
			
			if (getNewPassword().trim().length() > 0 && getConfirmPassword().trim().length() > 0 &&
					!getConfirmPassword().trim().equals(getNewPassword().trim()))
			{
				addFieldError("newpass","The new password and the confirmation password do not match");
			}
			
			if (!hasFieldErrors()){
				user.setPassword(PassHash.hashPassword(getNewPassword().trim()));
				userManager.updateUser(user);
				addActionMessage("Save successful");
			}
			
			
		} catch (Exception e) {
			log.error("Error in changePass: ", e);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(e); //send error message if requested.
		}
				
		return SUCCESS;
	}
	
	private Cookie getCookie(String cookie){
		Cookie cookies [] = request.getCookies ();  
		Cookie myCookie = null;  
		if (cookies != null)  
		{  
			for (int i = 0; i < cookies.length; i++)  
			{  
				if (cookies [i].getName().equals (cookie))  
				{  
					myCookie = cookies[i];  
					break;  
				}  
			}  
		}  
		return myCookie;  
	}
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;	
	}
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;		
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
		
	}
	public Boolean getRemember() {
		return remember;
	}
	public void setRemember(Boolean remember) {
		this.remember = remember;
	}
}
