package net.project.common;

import home.db.DbClass;

public class Constants {

	public static final String errorMessage = "Error, please contact system administrator";
	public static final String USER 	= "user";
	public static final int NBR_TRIES 	= 3;
	
	//DB
//	public static final String url 		= "jdbc:h2:" + System.getProperty("base.dir") + "/db/testDB";
	public static String url 		= "jdbc:h2:./server/db/testDB;DB_CLOSE_ON_EXIT=FALSE";
	public static final DbClass dbType  =  DbClass.H2;
	public static String dbUser 	= "dbadmin";
	public static String dbPassword = "123456";
	
	public static final String LIGHT_OVERRIDE = "lightsoverride";
	public static final String HEATER_OVERRIDE = "heateroverride";
	public static final String PUMP_OVERRIDE = "pumpoverride";
	
	public static final String ACCESS_DENIED = "accessdenied";
	
	public static String EMAILKEY = "";
	public static String cookieKey = "";
	
	public static final String WEBCAM_ACTIVE = "webCamActive?";
	
	public static final String logErrorLoc = "logs/WG-ERROR.log";
	public static final String logInfoLoc = "logs/WG-INFO.log";
	public static final String logDirLoc = "/server/logs";
	
	//water level constants
	public static final String pythonSonic 		= "range_sesnsor.py";
	public static final String pythonDirectory 		= "/server";
	public static final int bucketHeight 		= 23; //in CM
	
	public static final short pumpOnInSec 		= 10; //Determin how long the pump must stay on.
	
	public static boolean loginAfter = false; //determine if you will have the login screen 1st or you can log in after.
	public static boolean mustLogin = false;
	public static String PAGE_TITLE = "pageTitle";
	
	//common messages
	public static final String onManuallyRain 	= "Tried to turn on manually but did not water because it is raining";
	public static final String onByScheduleRain = "It is currently raining. The worker will not water";
	
	
}
