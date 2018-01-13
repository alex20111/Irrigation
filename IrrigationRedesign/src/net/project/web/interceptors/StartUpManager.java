package net.project.web.interceptors;


import java.util.Date;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.project.arduino.Arduino;
import net.project.common.Constants;
import net.project.common.Utils;
import net.project.db.entities.Config;
import net.project.db.entities.Weather;
import net.project.db.entities.Worker;
import net.project.db.manager.ConfigManager;
import net.project.db.manager.WeatherManager;
import net.project.db.manager.WorkerManager;
import net.project.db.sql.CreateTables;
import net.project.scheduler.ScheduleManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class StartUpManager implements ServletContextListener    {

	private Log log = LogFactory.getLog(getClass());
	

	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) 
	{
		//get logger level
		String loggerLevel = System.getProperty("root.logger.level");
		
		if (loggerLevel != null && loggerLevel.length() > 0){
			Logger.getLogger("net.project").setLevel(Utils.getLog4jLevel(loggerLevel));
		}	
	
		//
		// Pre-load some information
		//
		try
		{
			log.debug("Starting app");
			//create new tables if needed
			CreateTables.createTables();

			//constants to decide where the login screen will appear.
			String loginOptions = System.getProperty("web.login.options");	
			
			
			if (loginOptions.equals("mustLogin")){
				Constants.mustLogin = true;
				ce.getServletContext().setAttribute("callAction", "displayLogin");
			}else if(loginOptions.equals("loginAfter")){
				Constants.loginAfter = true;
				ce.getServletContext().setAttribute("callAction", "mainPageAction");
			}			

			Arduino.start(); 
			
			WorkerManager man = new WorkerManager();
			List<Worker> workers = man.loadAllWorkers(true, false, true);
			
			for(Worker worker : workers){
				
				//start the schedule if the schedule was running
				if (worker.isScheduleRunning()){
					long delay = ScheduleManager.newSchedule(worker, true);
					worker.setNextWateringBySched(new Date(new Date().getTime() + delay));
					man.updateWorker(worker, false);
				}
			}
			ConfigManager cfgMngr = new ConfigManager();
			Config cfg = cfgMngr.loadConfig();
			
			if (cfg != null){
				Thread.sleep(2000); //time to settle before communicating
				//	start verification
				ScheduleManager.scheduleWorkerVerification(Integer.parseInt(cfg.getCheckWorkers()), 3);
				
				ce.getServletContext().setAttribute(Constants.PAGE_TITLE, cfg.getApplicationTitle());
			}
			
			 // create gpio controller
	        final GpioController gpio = GpioFactory.getInstance();

	        // provision gpio pin #01 as an output pin and turn on
	        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "MyLED");
			
	        pin.high();
			
			
			//weather
			WeatherManager wman = new WeatherManager();
			Weather weather = wman.loadWeatherInformation(false);
			
			if (weather != null && weather.isActive()){
				ScheduleManager.scheduleWeatherStatus(weather.getRefresh(), 0); 
			}
			
			ScheduleManager.schedulePowerSaving();
			
			log.debug("App ready");

		}
		catch(Exception ex)
		{
			log.error("Error in startup manager" , ex);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{

	}
}