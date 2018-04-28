package net.project.web.action;

import home.crypto.Encryptor;
import home.inet.Connect;
import home.misc.Exec;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.project.common.Constants;
import net.project.common.SendEmail;
import net.project.common.Utils;
import net.project.db.entities.Config;
import net.project.db.entities.User;
import net.project.db.entities.Worker;
import net.project.db.manager.ConfigManager;
import net.project.db.manager.WorkerManager;
import net.project.enums.AccessEnum;
import net.project.enums.EmailProvider;
import net.project.scheduler.ScheduleManager;
import net.web.server.ServerConst;
import net.project.arduino.Arduino;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ApplicationAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class ServerOptionsAction extends ActionSupport implements SessionAware,ApplicationAware, ServletRequestAware {
	private Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;

	private Map<String, Object> session;
	private Map<String, Object> application;
	private HttpServletRequest request;
	private String page = ""; //the page that needs to be re-directed to.

	private boolean showPasswordBox = false;

	//buttons
	private String restartBtn 		= null;
	private String stopBtn 			= null;
	private String saveOptionsBtn 	= null;
	private String testEmailBtn 	= null;
	private String changeEmailBtn 	= null;
	private String newWarFileBtn 	= null;
	private String newJarsFileBtn 	= null;

	private File serverFile;
	private String serverFileFileName;
	
	private List<File> libraryFiles ;
	private List<String> libraryFilesFileName ;
	
	private ConfigManager mngr;
	private Config config;
	private List<Worker> workers;

	private InputStream serverStream = null;

	public ServerOptionsAction(){
		mngr = new ConfigManager();
	}

	public String loadOptions(){
		String retVal = Constants.ACCESS_DENIED;

		User user = (User)session.get(Constants.USER);

		try{

			if (page.length() == 0){ //no page to redirect to, then load options.

				if (user != null && user.getAccess() == AccessEnum.ADMIN){

					config = mngr.loadConfig();				

					retVal = "options";
				}else{
					retVal = Constants.ACCESS_DENIED;
				}

			}else if ("config".equalsIgnoreCase(page)){

				if (user != null && user.canModify()){
					config = mngr.loadConfig();
					workers = new WorkerManager().loadAllWorkersForPowerSaving();
					
					if (session.get("generatedTime") == null){

						Map<Integer, String> time = Utils.generateTime();
						session.put("generatedTime", time);
					}
					
					retVal = "config";
				}else{
					retVal = Constants.ACCESS_DENIED;
				}
			}else if ("notification".equalsIgnoreCase(page)){
				if (user != null && user.canModify()){
					config = mngr.loadConfig();

					//decide to show the password box or not.
					if (config != null && config.getEmailPassword() != null && config.getEmailPassword().length() > 0){
						showPasswordBox = false;
					}else{
						showPasswordBox = true;
					}					
					retVal = "notification";
				}else{
					retVal = Constants.ACCESS_DENIED;
				}
			}			
		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in loadOptions." , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}


		return retVal;
	}

	/**
	 * Save from the page Option.jsp
	 * @return
	 */
	public String saveServerOptions(){
		String retVal = SUCCESS;

		User user = (User)session.get(Constants.USER);

		try{
			if (user != null && user.getAccess() == AccessEnum.ADMIN){

				//option to start or stop server.
				if (getStopBtn() != null || getRestartBtn() != null){
					retVal = restartStopServer();
				}	
				loadOptions();
			}
			else
			{
				retVal = Constants.ACCESS_DENIED;
			}
		}catch (Exception ex){
			addActionError("Error while performing action. Please see logs or contact system administrator.");
			log.error("Error in saveServerOptions." , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	/**
	 * update server software and display.
	 * softUpdate.jsp
	 * @return
	 */
	public String updateServerSoftware(){

		String retVal = SUCCESS;
		User user = (User)session.get(Constants.USER);

		//you don't need to loadOptions since it does not load any options. It just update the software.
		if (user != null && user.canModify()){

			if(getNewWarFileBtn() != null){
				updateWarFile();
			}else if (getNewJarsFileBtn() != null){
				updateJars();
			}else{
				//if no buttons pressed, return to display page
				retVal = "displaySoftUpdatePage";
			}
		}
		else
		{
			retVal = Constants.ACCESS_DENIED;
		}

		return retVal;

	}
	/**
	 * config.jsp
	 * @return
	 */
	public String saveConfig(){

		String  retVal = SUCCESS;

		Config oldCfg = null;

		try{
			if(config.getSerialPort() == null || config.getSerialPort().length() == 0){
				addFieldError("serial","Please enter a value in the Serial port");
			}

			if(config.getApplicationTitle() == null || config.getApplicationTitle().length() == 0){
				addFieldError("appTitle","Please enter an Application title.");
			}
			if(config.getApplicationTitle() != null && config.getApplicationTitle().length() > 50){
				addFieldError("appTitle","Application title cannot be more than 50 characters.");
			}
			
			if(!hasActionErrors() && !hasFieldErrors())
			{
				//load old config for compare
				oldCfg = mngr.loadConfig();	
			}		

			//number of minutes the system must check the workers to see if they are alive.
			int checkWorkersMin = 0;
			if (config.getCheckWorkers() != null && config.getCheckWorkers() .length() > 0){
				
				try{
					checkWorkersMin = Integer.parseInt(config.getCheckWorkers());

					if (checkWorkersMin > 1440){
						addFieldError("checkWorker","The number of minutes to check if the worker is alive cannot be more than 24 hours");
					}else if (checkWorkersMin < 0){
						addFieldError("checkWorker","The number of minutes to check if the worker is alive must be greater than 0");
					}
				}catch(NumberFormatException nfe){
					addFieldError("checkWorker","The number of minutes to check if the worker is alive must be numeric");
				}

				if(!hasActionErrors() && !hasFieldErrors())	{			

					//verify if we need to restart the schedule
					if (!config.getCheckWorkers().equalsIgnoreCase(oldCfg.getCheckWorkers())){ 
						ScheduleManager.scheduleWorkerVerification(checkWorkersMin, 0);
					}				
				}			
			}

			List<Worker> toUpdatePS = new ArrayList<Worker>();
			WorkerManager wm = null;
			//power saving workers.
			if (workers != null && !workers.isEmpty()){
				wm = new WorkerManager();
				List<Worker> powerSavingWorker = wm.loadAllWorkers(true, false, false);
				//get power saving workers
				for(Worker worker : workers){
					log.debug("Power saving Update: " + worker.getStartSleepTime() + " Stop: " + worker.getStopSleepTime());
					//find it in the full list
					for(Worker fullWorker: powerSavingWorker){
						if (worker.getWorkId().equals(fullWorker.getWorkId())){
							
							if ( (worker.getStartSleepTime() > 0 && worker.getStopSleepTime() > 0 && worker.getStartSleepTime() != worker.getStopSleepTime() )
									|| (worker.getStartSleepTime() == -1 && worker.getStopSleepTime() == -1)){							 //TODO test reset sleep
								fullWorker.setStartSleepTime(worker.getStartSleepTime());
								fullWorker.setStopSleepTime(worker.getStopSleepTime());
								toUpdatePS.add(fullWorker);
							}
							else if( (worker.getStartSleepTime() > 0 && worker.getStopSleepTime() > 0 ) || 
									(worker.getStartSleepTime() == -1  && worker.getStopSleepTime() > 0) || //TODO test when one is on Select time and the other one is not. 
									 (worker.getStartSleepTime() > 0 && worker.getStopSleepTime() == -1)){
								
								
								
								addActionError("Time problem with Worker: " + fullWorker.getName() + ". Time cannot be Equals or both start and stop time needs to be on 'Select time' to reset");
							}
							
							break;
						}
					}		
				}	
				log.debug("Updating workers in config for power saving --->: " +toUpdatePS );
			}
			
			if(!hasActionErrors() && !hasFieldErrors()){
				
				//verify if we need to restart the port
				boolean restartSerialPort = false;
				if (config.getSerialPort() != null && !config.getSerialPort().equals(oldCfg.getSerialPort()) )
				{
					restartSerialPort = true;
				}
				
				boolean updateTitle = false;
				if (!oldCfg.getApplicationTitle().equals(config.getApplicationTitle())){
					updateTitle = true;
				}
				
				//add the values of the config to update
				oldCfg.setCheckWorkers(config.getCheckWorkers());
				oldCfg.setSerialPort(config.getSerialPort());
				oldCfg.setApplicationTitle(config.getApplicationTitle());	
				
				mngr.updateConfig(oldCfg);
				
				//save worker list if any for power saving mode
				//if wm is null this means we don't have any active power saving workers
				if (!toUpdatePS.isEmpty() && wm != null){
					for(Worker w: toUpdatePS){
						wm.updateWorker(w, false);
					}
				}
				
				addActionMessage("Save Successful");

				if (restartSerialPort)
				{
					log.debug("Restart serial");
					//check if the serial port has changed
					//restart serial
					Arduino.restart();
				}
				if (updateTitle){
					//reload title if changed.
					application.put(Constants.PAGE_TITLE, config.getApplicationTitle());
				}				
			}	

			loadOptions();
			
		}catch (Exception ex){
			log.error("Error in saveConfig" , ex);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	/**
	 * notification.jsp
	 * @return
	 */
	public String saveNotification(){

		String retVal = SUCCESS;
		try{

			if (getTestEmailBtn() != null){
				testEmail();
			}
			else if(getChangeEmailBtn() != null){
				//make password box appear					
				showPasswordBox = true;

				config = mngr.loadConfig();				
			}
			else{
				//SAVE 
				
				//load old config for compare
				Config oldCfg = mngr.loadConfig();	
				
				if (config.isActivateWarning()){
					if (config.getEmailProvider() == null || config.getEmailProvider() == EmailProvider.noSelected){
						addFieldError("provider","Please Select an e-mail provider.");
					}
					if (config.getEmailUserName() == null || config.getEmailUserName().length() == 0){
						addFieldError("emailUser","Please enter a Email user name");
					}
					
					//if password is null and old password is non existant, then enter an password.
					if (config.getEmailPassword() != null && config.getEmailPassword().length() == 0){
						addFieldError("emailPass","Please enter a password");
					}	
					
					
				}else{
					oldCfg.setEmailPassword(null);
					oldCfg.setEmailUserName(null);
					oldCfg.setEmailProvider(EmailProvider.noSelected);					
				}

				if (!hasFieldErrors()){
					
					if(config.getEmailPassword() != null && config.getEmailPassword().length() > 0){
						oldCfg.setEmailPassword(Encryptor.encryptString(config.getEmailPassword(), System.getProperty("email_key").toCharArray()));
					}
					if (config.getEmailUserName() != null && config.getEmailUserName().length() > 0){
						oldCfg.setEmailUserName(config.getEmailUserName());
					}
					if (config.getEmailProvider() != null){
						oldCfg.setEmailProvider(config.getEmailProvider() );	
					}
									
					oldCfg.setActivateWarning(config.isActivateWarning());
					oldCfg.setNotiBattLow(config.isNotiBattLow());
					oldCfg.setNotiErrors(config.isNotiErrors());
					oldCfg.setNotiRainNotWatering(config.isNotiRainNotWatering());
					oldCfg.setNotiUserLogin(config.isNotiUserLogin());

					mngr.updateConfig(oldCfg);
					
					addActionMessage("Save Successful");
					loadOptions();
				}
			}
		}catch(Exception ex){
			log.error("Error in saveNotification", ex);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		
		return retVal;
	}
	
	
	
	/**
	 * Restart or stop the server.
	 * @return
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	private String restartStopServer() throws KeyManagementException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, KeyStoreException, IOException{
		String retVal = SUCCESS;

		String url = request.getScheme() + "://localhost:" + request.getServerPort() +  "/server/options";

		Map<String, String> params = new HashMap<String, String>();
		params.put("user", ServerConst.sslUser);
		params.put("password", ServerConst.sslPass);
		params.put("wait", "1000");

		Connect con = new Connect();
		con.setUrl(url); 

		if (getStopBtn() != null){					

			params.put("server", "stop");					

			if (ServerConst.sslKeyFile.length() > 0){
				con.createCustomSSLSocket(ServerConst.sslKeyFile, ServerConst.sslKeyPass);
			}

			String result = con.connectToUrlUsingGET(params).getResultAsStr();
			serverStream = new ByteArrayInputStream(result.getBytes("UTF-8"));
			retVal = "restartStop";					

		}else if (getRestartBtn() != null){

			try{
				restart(1);

				addActionMessage("System will reboot in 1 minute, no need to save. Please wait");

				//			params.put("server", "restart");
				//
				//			if (ServerConst.sslKeyFile.length() > 0){
				//				con.createCustomSSLSocket(ServerConst.sslKeyFile, ServerConst.sslKeyPass);
				//			}
				//
				//			String result = con.connectToUrlUsingGET(params).getResultAsStr();
				//
				//			serverStream = new ByteArrayInputStream(result.getBytes("UTF-8"));
				retVal = "restartStop";
			}catch(Exception ex){
				Utils.sendErrorMessage(ex); //send error message if requested.
				ex.printStackTrace();
			}

		}
		return retVal;
	}

	private void testEmail() throws Exception {
		//test e-mail setting
		if (config.getEmailProvider() == null || config.getEmailProvider() == EmailProvider.noSelected){
			addFieldError("provider","Please Select an e-mail provider.");
		}
		if (config.getEmailUserName() == null || config.getEmailUserName().length() == 0){
			addFieldError("emailUser","Please enter a Email user name");
		}
		if (config.getEmailPassword() == null || config.getEmailPassword().length() == 0){
			addFieldError("emailPass","Please enter a Email password");
		}
		if (!hasActionErrors() && !hasFieldErrors()){
			try {
				SendEmail.sendTestMail(config.getEmailUserName(), config.getEmailPassword(), config.getEmailProvider());
				addActionMessage("E-mail sent");
			} catch (javax.mail.AuthenticationFailedException e) {
				log.error("Auth error", e);
				addActionError("Cannot authenticate. cause, wrong user password or server does not accept login from 3rd party app. You will need to Allow apps that use less secure sign in.");
			}						
			
		}
	}
	
	private void updateWarFile(){

		try {
			if (serverFileFileName == null || serverFileFileName.length() == 0){
				addActionError("Please add a file");
			}
			if (!"WebServer.war".equalsIgnoreCase(serverFileFileName)){
				addActionError("Wrong file.. need to be WebServer.war");
			}		

			if (!hasActionErrors()){
				
				//copy the file to the web server file
				File webServerFile = new File("/server/WebServer.war");
				FileUtils.copyFile(serverFile, webServerFile);
				
				restart(1);
			
				addActionMessage("System will reboot in 1 minute, no need to save. Please wait");
			}
		} catch (Exception  e) {
			log.error("Error in udateWarFile. ", e);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(e); //send error message if requested.
		}

	}
	private void updateJars(){


		try{
			if (libraryFilesFileName == null || libraryFilesFileName.size() == 0){
				addActionError("Please add a file");
			}

			//check if its a jar.
			if (libraryFilesFileName != null && libraryFilesFileName.size() > 0){

				Boolean notAJar = false;
				for(String fileName : libraryFilesFileName){

					if (!"jar".equalsIgnoreCase(fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()))){
						notAJar = true;
						break;
					}
				}
				if (notAJar){
					addActionError("All the files must be Jar files");
				}
			
				if (libraryFiles.size() > 0)
				{
					for(int i = 0 ; i <  libraryFiles.size() ; i ++){				
						//copy the file to the web server file
						File libFile = libraryFiles.get(i);
						File tempLibFile = new File("/server/webserver_lib/" + libraryFilesFileName.get(i));
						FileUtils.copyFile(libFile, tempLibFile);
					}
					restart(1);

					addActionMessage("System will reboot in 1 minute, no need to save. Please wait");
				}
			}
		}catch (Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error while updating software lib" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
	}
		
	private void restart(int time) throws ExecuteException, IOException, InterruptedException{
		
		Exec exec = new Exec();
		exec.addCommand("sudo");
		exec.addCommand("shutdown");
		exec.addCommand("-r");
		exec.addCommand("+"+time);

		 exec.run();		
	}
	
	public String getRestartBtn() {
		return restartBtn;
	}
	public void setRestartBtn(String restartBtn) {
		this.restartBtn = restartBtn;
	}
	public String getStopBtn() {
		return stopBtn;
	}
	public void setStopBtn(String stopBtn) {
		this.stopBtn = stopBtn;
	}
	public InputStream getServerStream() {
		return serverStream;
	}
	public void setServerStream(InputStream serverStream) {
		this.serverStream = serverStream;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;		
	}
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;		
	}
	public String getSaveOptionsBtn() {
		return saveOptionsBtn;
	}
	public void setSaveOptionsBtn(String saveOptionsBtn) {
		this.saveOptionsBtn = saveOptionsBtn;
	}

	public String getTestEmailBtn() {
		return testEmailBtn;
	}

	public void setTestEmailBtn(String testEmailBtn) {
		this.testEmailBtn = testEmailBtn;
	}

	public String getChangeEmailBtn() {
		return changeEmailBtn;
	}

	public void setChangeEmailBtn(String changeEmailBtn) {
		this.changeEmailBtn = changeEmailBtn;
	}

	public boolean isShowPasswordBox() {
		return showPasswordBox;
	}

	public void setShowPasswordBox(boolean showPasswordBox) {
		this.showPasswordBox = showPasswordBox;
	}
	public Config getConfig() {
		return config;
	}
	public void setConfig(Config config) {
		this.config = config;
	}
	public String getNewWarFileBtn() {
		return newWarFileBtn;
	}
	public void setNewWarFileBtn(String newWarFileBtn) {
		this.newWarFileBtn = newWarFileBtn;
	}
	public File getServerFile() {
		return serverFile;
	}
	public void setServerFile(File serverFile) {
		this.serverFile = serverFile;
	}
	public String getServerFileFileName() {
		return serverFileFileName;
	}
	public void setServerFileFileName(String serverFileFileName) {
		this.serverFileFileName = serverFileFileName;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getNewJarsFileBtn() {
		return newJarsFileBtn;
	}
	public void setNewJarsFileBtn(String newJarsFileBtn) {
		this.newJarsFileBtn = newJarsFileBtn;
	}
	public List<File> getLibraryFiles() {
		return libraryFiles;
	}
	public void setLibraryFiles(List<File> libraryFiles) {
		this.libraryFiles = libraryFiles;
	}
	public List<String> getLibraryFilesFileName() {
		return libraryFilesFileName;
	}
	public void setLibraryFilesFileName(List<String> libraryFilesFileName) {
		this.libraryFilesFileName = libraryFilesFileName;
	}

	@Override
	public void setApplication(Map<String, Object> application) {
		this.application = application;
		
	}
	public List<Worker> getWorkers() {
		return workers;
	}
	public void setWorkers(List<Worker> workers) {
		this.workers = workers;
	}
}
