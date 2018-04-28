package net.project.db.manager;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Utils;
import net.project.db.entities.Weather;
import net.project.db.entities.WeatherWorker;
import net.project.db.sql.WeatherSql;
import net.project.enums.WeatherProvider;
import net.project.exception.ValidationException;
import net.weather.action.WeatherAction;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.EnvCanLang;

public class WeatherManager {
	
	private static Log log = LogFactory.getLog(WeatherManager.class);
	
	private WeatherSql sql;
	
	public WeatherManager(){
		sql = new WeatherSql();
	}
	
	public Weather loadWeatherInformation(boolean loadWorkers) throws SQLException, ClassNotFoundException{
		return sql.loadWeatherInformation(loadWorkers);		
	}
	
	public void addWeatherInfo(Weather weather, List<WeatherWorker> weatherWks)  throws SQLException, ValidationException, ClassNotFoundException{
		
		//verify that we hav all the data.
		if (weatherWks != null && weatherWks.size() > 0){
			for(WeatherWorker ww : weatherWks){
				if (ww.getWorkerId() == null || ww.getWorkerId().length() == 0 ){
					throw new ValidationException("Worker id is needed");
				}
			}
		}
		
		sql.addWeatherInfo(weather, weatherWks);		
		
	}
	/**
	 * Update the weather entity
	 * @param weather	- The weather entity to be updated.
	 * @param weatherWks - If null , only the weather entity will be updated. 
	 * 					 	if not null, the the weatherworkers will be processed.
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void updateWeather(Weather weather, List<WeatherWorker> weatherWks) throws SQLException, ClassNotFoundException{
		
		if (weatherWks != null){ //only compare when we have a non null list. If list is null only update the weather

			//compare the old weather worker and the new weatherworker and delete the one that has been removed.. do that in the update manager
			//load old weather workers.

			List<WeatherWorker> ww = sql.loadAllWeatherWorkers(weather.getId());

			List<WeatherWorker> toRemove = new ArrayList<WeatherWorker>();
			if (ww.size() > 0 ){
				for(WeatherWorker oldWthW : ww){
					boolean toRmve = true;
					for(WeatherWorker newWthW : weatherWks){
						if (oldWthW.getWorkerId().equals(newWthW.getWorkerId()) ){
							toRmve = false;
							break;
						}
					}

					if (toRmve){
						toRemove.add(oldWthW);
					}
				}

				if (toRemove.size() > 0){
					//call remove 
					sql.removeWeatherWorkers(toRemove);
				}

			}

			if (weatherWks.size() > 0){
				//verify if new , then add
				for(WeatherWorker newlist : weatherWks){
					if (newlist.getWeatherId() == -1){
						newlist.setWeatherId(weather.getId());
						//add
						sql.addWeatherWorker(newlist);
					}else{
						//update
						sql.updateWeatherWorker(newlist);
					}
				}
			}
		}
		sql.updateWeather(weather);
		
	}

	public Weather loadCurrentWeatherFromWeb(Weather weather){

		Weather wth = null;

		try{
			WeatherGenericModel wgm= null;

		
			
			if (weather.getProvider() == WeatherProvider.ENVCan){
				log.debug("Loading env can data");				
				wgm = WeatherAction.getEnvironmentCanadaRSSWeather(weather.getLocation(),  EnvCanLang.english, false, false);
			}else if (weather.getProvider() == WeatherProvider.WTHUndergrnd){
				
				//1st find out if its a airport location or personal weather (Pws)
				String[] loc = weather.getLocation().split(":");
				
				log.debug("LOCCC " + loc[0] + " " + loc[1]);
				if ("airportLoc".equals(loc[0])){
					wgm = WeatherAction.getWeatherByAirportCode(weather.getApiKey(), loc[1]);
				}else{
					wgm = WeatherAction.getWeatherByPWS(weather.getApiKey(),loc[1], null);
				}		

			}

			if (wgm != null){
				log.debug("Weather status. Current condition: " +  wgm.getWeatherCurrentModel().getCurrectTempC() + " Weather outlook: " + wgm.getWeatherCurrentModel().getWeather());
				wth = weather;
				wth.setCurrTemp(Float.toString(wgm.getWeatherCurrentModel().getCurrectTempC()) + "c");
				wth.setWeather(	wgm.getWeatherCurrentModel().getWeather());
				wth.setLastUpdated(new Date());
				
				updateWeather(wth,null);

			}
		}catch(Exception ex){
			log.error("Error in loadCurrentWeatherFromWeb" , ex);
			Utils.sendErrorMessage(ex);
		}

		return wth;
	}
	public List<WeatherWorker> loadAllWeatherWorkers(int weatherId) throws SQLException, ClassNotFoundException{
		return sql.loadAllWeatherWorkers(weatherId);
	}
	/**
	 * verify if the weather is active..
	 *  
	 *  if active, it will return the status also.
	 * 
	 * @return not null if active
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public Weather weatherActive() throws SQLException, ClassNotFoundException{
		return sql.weatherActive();
	}
}
