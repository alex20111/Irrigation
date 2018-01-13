package net.project.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Utils;
import net.project.db.entities.Weather;
import net.project.db.manager.WeatherManager;

public class WeatherSched implements Runnable {
	
	private static Log log = LogFactory.getLog(WeatherSched.class);
	@Override
	public void run() {

		try{
			WeatherManager wm = new WeatherManager();

			Weather weather = wm.loadWeatherInformation(false);

			log.debug("Weatehr -- > " + weather);
			
			if (weather != null && weather.isActive()){
				Weather w = wm.loadCurrentWeatherFromWeb(weather);
				log.debug("Update active weather. " + w);
				
			}
		}catch(Exception ex){
			log.error("Error in WeatherSched", ex );
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
	}
}
