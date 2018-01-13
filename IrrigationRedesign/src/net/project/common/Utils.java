package net.project.common;


import home.misc.StackTrace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.project.db.entities.Config;
import net.project.db.manager.ConfigManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;

public class Utils {

	private static Log log = LogFactory.getLog(Utils.class);
	
	public static String reverseLogFile(File file) throws IOException{
		FileReader fr=new FileReader(file);
		BufferedReader br=new BufferedReader(fr);
		String s;

		List<String> tmp = new ArrayList<String>();
		do{
			s = br.readLine();
			tmp.add(s);
		}while(s!=null);

		br.close();

		StringBuilder sb = new StringBuilder();
		for(int i=tmp.size()-1;i>=0;i--) {
			sb.append(tmp.get(i) + "\n");    

		}
		return sb.toString();
	}
	/**
	 * Get the ip address of the current machine
	 * @return
	 * @throws SocketException
	 */
	@SuppressWarnings("rawtypes")
	public static String getIp() throws SocketException{		
		
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress i = (InetAddress) ee.nextElement();
		        if (i.getHostAddress().startsWith("192")){
		        	return i.getHostAddress();
		        }
		    }
		}
		return "";
	}
	//OFF
	//FATAL	//ERROR	//WARN	//INFO	//DEBUG	//TRACE
	//ALL
	public static Level getLog4jLevel(String loggerLevel){
		
		Level level = Level.DEBUG;
		if (loggerLevel != null && loggerLevel.length() > 0){
			if (loggerLevel.trim().equalsIgnoreCase(Level.TRACE.toString())){
				level = Level.TRACE;
			}else if(loggerLevel.trim().equalsIgnoreCase(Level.INFO.toString())){
				level = Level.INFO;				
			}else if(loggerLevel.trim().equalsIgnoreCase(Level.WARN.toString())){
				level = Level.WARN;
			}else if(loggerLevel.trim().equalsIgnoreCase(Level.ERROR.toString())){
				level = Level.ERROR;
			}else if(loggerLevel.trim().equalsIgnoreCase(Level.FATAL.toString())){
				level = Level.FATAL;
			}		
		}
		
		return level;
	}
	/**
	 * 
	 * @param nbr
	 * @return
	 */
	public static String addIntLeftZeroPadding(int nbr){
		if (nbr < 10){
			return "00" + nbr;
		}else if (nbr < 100){
			return "0" + nbr;
		}else if (nbr < 1000){
			return "" + nbr;
		}
		return "";
	}
	public static String addOneLeftZeroPadding(int nbr){
		if (nbr < 10){
			return "0" + nbr;
		}else{
			return "" + nbr;
		}
	}
	
	public static void sendErrorMessage(Throwable msg){

		try{
			Config cfg = new ConfigManager().loadConfig();

			if (cfg.isActivateWarning() && cfg.isNotiErrors()){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<body>");
				sb.append("<p> <strong>Error: </strong> <br/>" + StackTrace.displayStackTrace(msg) +  " </p>");
				sb.append("</body>");
				sb.append("</html>");
				SendEmail.send("ERROR Message",sb.toString(), cfg);
			}
		}catch(Exception ex){
			log.error("Error while trying to send mail: " + ex);
		}
	}
	
	public static Map<Integer,String> generateTime(){

		Map<Integer,String> genTime = new LinkedHashMap<Integer,String>();
		int hours;
		int minutes;
		String ampm = "";

		for(int i = 0; i <= 1410; i += 30){			

			minutes = i % 60;
			hours = (int) Math.floor(i / 60);

			ampm = (hours % 24 < 12 ? "AM" : "PM");
			hours = hours % 12;
			if (hours == 0){
				hours = 12;
			}
			String time = hours + ":" + (minutes < 10 ? "0" + minutes : minutes) + " " + ampm;
			genTime.put(i, time);
		
		}

		return genTime;
	}
	
}
