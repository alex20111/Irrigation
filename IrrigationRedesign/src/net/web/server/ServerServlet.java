package net.web.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerServlet  extends HttpServlet{

	private Log log = LogFactory.getLog(getClass());
	
	private static final long serialVersionUID = 3014851331115944863L;
	
	private boolean restart = false;	

	//param
	private String serverParam = "";
	private String user = null;
	private String password = null;
	private String wait = "1000";	
	
	//server context
	private Server server;
	private WebAppContext webApp;

	public ServerServlet(Server server , WebAppContext webApp){
		this.server = server;
		this.webApp = webApp;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		getParameters(req.getParameterMap());	

		response.setContentType("text/html;charset=utf-8");
		if (serverParam.equals(ServerConst.RESTART)){
			restart = true;
			boolean success = stopServer(req,response);
			if (!success){
				response.setStatus(HttpStatus.UNAUTHORIZED_401);
				response.getWriter().println("Error in restarting server" );
			}
		}
		else if (serverParam.equals(ServerConst.STOP)){
			restart = false;
			boolean success = stopServer(req,response);
			if (!success){
				response.setStatus(HttpStatus.UNAUTHORIZED_401);
				response.getWriter().println("Error in stopping server" );
			}
		}
		else{		
			response.setStatus(HttpStatus.UNAUTHORIZED_401);
			response.getWriter().println("Error, wrong request" );
		}
	}

	private boolean stopServer(HttpServletRequest req,HttpServletResponse response) throws IOException {
		
		boolean canDo = false;
		try {
			canDo = verifyCredentials(req);
		} catch (ConfigurationException e) {
			log.error("Error when verifying credentials", e);
		}
		
		if (canDo){
			response.setStatus(HttpStatus.ACCEPTED_202);
			response.setContentType("text/html");
			ServletOutputStream os = response.getOutputStream();
			if (restart){		
				log.info("trying to restart server " );
				
				String address = req.getScheme() + "://localhost:" + req.getServerPort() +  "/";
				int time = 20;
				
				os.println("<html><head>" +
						"<meta http-equiv=\"refresh\" content=\"" + time + ";url="+ address +"\" />" +
						"<script>var seconds_left = " + time + ";" +
						" var interval = setInterval(function() { " +
						"document.getElementById('timer_div').innerHTML = \"Restarting in :\" + --seconds_left + \" seconds\"; " +
						" if (seconds_left <= 0) { clearInterval(interval); } },1000);" +
						"</script>"+
						" </head>" +
						"<body><div id=\"timer_div\"></div> </body> <br> If not redirected, click here : <a href=\"" + address + "\" > Home </a></html>");

			}else{
				log.info("Shutting down server");
				os.println("Shutting down. Bye Bye");
			}
			os.close();
			response.flushBuffer();
			try {
				// Stop the server.
				new Thread() {

					@Override
					public void run() {
						try {

							if (wait == null || wait.length() == 0)
							{
								Thread.sleep(2000);
							}else{
								Thread.sleep(Integer.parseInt(wait));
							}								
							
							if (restart){
								log.info("Restarting server.");
								webApp.stop();
								Thread.sleep(1000);
								webApp.start();
								log.info("Server Restarted.");
							}
							else
							{
								log.info("Shutting down server");
								server.stop();
								log.info("Server has stopped.");								
							}

						} catch (Exception ex) {
							log.error("Error when stopping Server: ", ex);
						}
					}
				}.start();
			} catch (Exception ex) {
				log.error("Unable to stop Server: ",ex);
				return false;
			}
		}
		return canDo;
	}

	private void getParameters(Map<String, String[]> param){
		if (param != null && param.size() > 0){
			for(Map.Entry<String, String[]> p : param.entrySet()){

				if ("server".equals(p.getKey().toLowerCase())){
					for(String str: p.getValue()){
						this.serverParam = str.trim().toLowerCase();
					}
				} else if ("user".equals(p.getKey().toLowerCase())){
					for(String str: p.getValue()){
						this.user = str.trim();
					}
				}  else if ("password".equals(p.getKey().toLowerCase())){
					for(String str: p.getValue()){
						this.password = str.trim();
					}
				} else if (ServerConst.WAITTIME.equals(p.getKey().toLowerCase())){
					for(String str: p.getValue()){
						this.wait = str.trim();
					}
				}
			}
		}
	}

	private boolean verifyCredentials(HttpServletRequest req) throws ConfigurationException{	

		if (user != null && user.length() > 0 && password != null && password.length() > 0){

			int tries = 0;
			
			if (req.getServletContext().getAttribute("nbrTriesOptions") != null){
				tries = (Integer)req.getServletContext().getAttribute("nbrTriesOptions");
			}		

			if (tries < 15){
				
				if (user.equals(ServerConst.sslUser) && password.equals(ServerConst.sslPass)){
					req.getServletContext().setAttribute("nbrTriesOptions", 0);
					return true;
				}
				tries++;
				log.info("User or password do not match. User: " + this.user + " Password: " + this.password);
				req.getServletContext().setAttribute("nbrTriesOptions", tries);				
				
			}else{
				log.info("Number of tries excedded. ");
				return false;
			}
		}	
		return false;
	}
}
