package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.project.enums.EmailProvider;

public class Config {

	public static final String TABLE_NAME 		= "config";
	public static final String CONFIG_ID 		= "cfg_id";
	public static final String EMAIL_USER 		= "email_user";
	public static final String EMAIL_PASS 		= "email_password";
	public static final String WARNING	 		= "warning_opt";
	public static final String NOTI_BATT	 	= "noti_batt_low";
	public static final String NOTI_RAIN	 	= "noti_rain"; //will send notification if raining and worker did not water
	public static final String NOTI_USER_LOG	= "noti_user_log";
	public static final String NOTI_ERRORS	 	= "noti_errors";
	public static final String SERIAL_PORT 		= "serial_port";
	public static final String CHECK_WORKER 	= "check_workers";
	public static final String EMAIL_PROVIDER 	= "email_provider";
	public static final String APP_TITLE	 	= "app_title";
	
	
	private int configId	= -1;
	private String emailUserName = "";
	private String emailPassword = null;	
	private String serialPort = "/dev/ttyAMA0";
	private String checkWorkers	= "60"; //check if the workers are active every x minutes
	private boolean activateWarning 	= false;
	private boolean notiBattLow 		= false;
	private boolean notiRainNotWatering = false;
	private boolean notiUserLogin 		= false;
	private boolean notiErrors			= false;
	private String applicationTitle		= "Irrigation project";
	
	private EmailProvider emailProvider = EmailProvider.noSelected;
	
	public Config(){}
	public Config(ResultSet rs) throws SQLException{
		
		this.configId = rs.getInt(CONFIG_ID);
		this.emailPassword = rs.getString(EMAIL_PASS);
		this.emailUserName = rs.getString(EMAIL_USER);		
		this.serialPort = rs.getString(SERIAL_PORT);
		this.checkWorkers = rs.getString(CHECK_WORKER);
		this.emailProvider = EmailProvider.valueOf(rs.getString(EMAIL_PROVIDER));
		this.applicationTitle = rs.getString(APP_TITLE);
		
		this.activateWarning 	= rs.getBoolean(WARNING);
		this.notiBattLow 		= rs.getBoolean(NOTI_BATT);
		this.notiRainNotWatering = rs.getBoolean(NOTI_RAIN);
		this.notiUserLogin 		= rs.getBoolean(NOTI_USER_LOG);
		this.notiErrors 		= rs.getBoolean(NOTI_ERRORS);		
		
	}	
	
	public String getEmailUserName() {
		return emailUserName;
	}
	public void setEmailUserName(String emailUserName) {
		this.emailUserName = emailUserName;
	}
	public String getEmailPassword() {
		return emailPassword;
	}
	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}
	
	public int getConfigId() {
		return configId;
	}
	public void setConfigId(int configId) {
		this.configId = configId;
	}
	public boolean isActivateWarning() {
		return activateWarning;
	}
	public void setActivateWarning(boolean activateWarning) {
		this.activateWarning = activateWarning;
	}
	public String getSerialPort() {
		return serialPort;
	}
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	public String getCheckWorkers() {
		return checkWorkers;
	}
	public void setCheckWorkers(String checkWorkers) {
		this.checkWorkers = checkWorkers;
	}
	public EmailProvider getEmailProvider() {
		return emailProvider;
	}
	public void setEmailProvider(EmailProvider emailProvider) {
		this.emailProvider = emailProvider;
	}
	public boolean isNotiBattLow() {
		return notiBattLow;
	}
	public void setNotiBattLow(boolean notiBattLow) {
		this.notiBattLow = notiBattLow;
	}
	public boolean isNotiRainNotWatering() {
		return notiRainNotWatering;
	}
	public void setNotiRainNotWatering(boolean notiRainNotWatering) {
		this.notiRainNotWatering = notiRainNotWatering;
	}
	public boolean isNotiUserLogin() {
		return notiUserLogin;
	}
	public void setNotiUserLogin(boolean notiUserLogin) {
		this.notiUserLogin = notiUserLogin;
	}
	public boolean isNotiErrors() {
		return notiErrors;
	}
	public void setNotiErrors(boolean notiErrors) {
		this.notiErrors = notiErrors;
	}
	public String getApplicationTitle() {
		return applicationTitle;
	}
	public void setApplicationTitle(String applicationTitle) {
		this.applicationTitle = applicationTitle;
	}
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TABLE_NAME + " (");
		create.append(CONFIG_ID + " INT PRIMARY KEY auto_increment");
		create.append(", " + EMAIL_USER + " VARCHAR(200)" );
		create.append(", " + EMAIL_PASS + " VARCHAR(200)" );
		create.append(", " + WARNING + " BOOLEAN" );
		create.append(", " + SERIAL_PORT + " VARCHAR(300)" );
		create.append(", " + CHECK_WORKER + " VARCHAR(6)" );
		create.append(", " + EMAIL_PROVIDER + " VARCHAR(100)" );
		create.append(", " + NOTI_BATT + " BOOLEAN" );
		create.append(", " + NOTI_RAIN + " BOOLEAN" );
		create.append(", " + NOTI_USER_LOG + " BOOLEAN" );
		create.append(", " + NOTI_ERRORS + " BOOLEAN" );
		create.append(", " + APP_TITLE + " VARCHAR(100)" );
		create.append(")");
		
		return create.toString();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Config [configId=");
		builder.append(configId);
		builder.append(", emailUserName=");
		builder.append(emailUserName);
		builder.append(", emailPassword=");
		builder.append(emailPassword);
		builder.append(", activateWarning=");
		builder.append(activateWarning);
		builder.append(", serialPort=");
		builder.append(serialPort);
		builder.append(", checkWorkers=");
		builder.append(checkWorkers);
		builder.append(", emailProvider=");
		builder.append(emailProvider);
		builder.append("]");
		return builder.toString();
	}
}
