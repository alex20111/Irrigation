package net.project.web.action;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.project.common.Constants;
import net.project.common.Utils;
import net.project.db.entities.User;
import net.project.db.entities.Weather;
import net.project.db.entities.WeatherWorker;
import net.project.db.entities.Worker;
import net.project.db.manager.WeatherManager;
import net.project.db.manager.WorkerManager;
import net.project.enums.WeatherProvider;
import net.project.scheduler.ScheduleManager;
import net.weather.bean.City;
import net.weather.bean.WeatherLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;


import com.opensymphony.xwork2.ActionSupport;

public class WeatherAction extends ActionSupport implements SessionAware {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(WeatherAction.class);
	private Map<String, Object> session;
	private Weather weather;
	
	private List<WeatherWorker> wthWorkers;
	
	private List<City> cities;
	
	//ajax call
	private String provider;	
	private String userInputCity = "";
	private String apiKey = "";
	
	public String loadWeatherInformation(){

		String retVal = SUCCESS;
		User user = (User) session.get(Constants.USER);

		try{


			if (user != null && user.canModify()){

				WeatherManager wm = new WeatherManager();
				weather = wm.loadWeatherInformation(true);
				
				WorkerManager wman = new WorkerManager();
				List<Worker> workers = wman.loadAllWorkers(true, false,false);
				
				if (weather != null){					
					
					//verify if any are already in the active list on the page (table) list. Basically these are workers that was added to the weather to manage.
					if (weather.getWorkers() != null && weather.getWorkers().size() > 0){
						for(Worker w : weather.getWorkers() ){
							for(int i = 0 ; i <  workers.size() ; i++){								
								Worker w2 = workers.get(i);
							
								if (w.getWorkId().equals(w2.getWorkId())){
									//match, remove
									workers.remove(i);
									break;
								}
							}
						}
					}
					
					setUserInputCity(weather.getSearchedCity());
					
				}
				
				session.put("allWorkersWeather", workers);

			}else{
				retVal = Constants.ACCESS_DENIED;
			}

		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in loadWeatherInformation"  , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	
	public String saveWeatherOptions(){
		String retVal = SUCCESS;
		
		try{
			WeatherManager wthM = new WeatherManager();

			if (weather != null && weather.isActive()){

				if (!hasFieldErrors()){
					//build a list of weather worker.
					List<WeatherWorker> toSaveWeatherWorker = new ArrayList<WeatherWorker>();
					if (wthWorkers != null && wthWorkers.size() > 0){
						for(WeatherWorker w : wthWorkers){
							if (w != null){		
								w.setActive(true);
								toSaveWeatherWorker.add(w);
							}
						}
					}

					if (weather.getLocation() == null || weather.getLocation().length() == 0){
						addFieldError("location", "Please add a location");
					}
					if (weather.getRefresh() <= 0 ){
						addFieldError("refresh", "Please add a refresh time");
					}


					if (!hasFieldErrors()){

						weather.setSearchedCity(userInputCity);						

						if (weather.getId() == -1){
							//add new
							wthM.addWeatherInfo(weather, toSaveWeatherWorker);
							ScheduleManager.scheduleWeatherStatus(weather.getRefresh(), 0);
						}else{
							Weather oldWeather = wthM.loadWeatherInformation(false);

							boolean restartSched = false;
							if (oldWeather == null ||
									oldWeather.getProvider() != weather.getProvider() || 
									oldWeather.getRefresh() != weather.getRefresh() || 
									!oldWeather.getLocation().equals(weather.getLocation()) || 
									!oldWeather.getSearchedCity().equals(weather.getSearchedCity()) ){		
								//restart schedule if any of the elements changed
								restartSched = true;
							}else if (oldWeather != null &&
									oldWeather.getProvider() == weather.getProvider() && 
									oldWeather.getRefresh() == weather.getRefresh() &&
									oldWeather.getLocation().equals(weather.getLocation()) &&
									oldWeather.getSearchedCity().equalsIgnoreCase(weather.getSearchedCity())){
								//if nothing has changed, re set the weather information.
								weather.setCurrTemp(oldWeather.getCurrTemp());
								weather.setLastUpdated(oldWeather.getLastUpdated());
								weather.setWeatherStatus(oldWeather.getWeatherStatus());
							}

							wthM.updateWeather(weather, toSaveWeatherWorker);	

							//if refresh time has changed or the provider has changed , restart the schedule
							if (restartSched){
								log.debug("Weather schedule restarted");
								ScheduleManager.scheduleWeatherStatus(weather.getRefresh(), 0);
							}
						}

						addActionMessage("Save Successful");						
					}
				}
			}
			else if (weather == null || !weather.isActive()) //remove weather
			{				
				
				weather = wthM.loadWeatherInformation(false);
				
				weather.setActive(false);
				
				List<WeatherWorker> toSaveWeatherWorker =  wthM.loadAllWeatherWorkers(weather.getId());
				
				if (toSaveWeatherWorker != null && toSaveWeatherWorker.size() > 0){
					for(WeatherWorker w : toSaveWeatherWorker){
						if (w != null){				
							w.setActive(false);							
						}
					}
				}
				
				wthM.updateWeather(weather, toSaveWeatherWorker);		
				
				ScheduleManager.cancelWeatherSchedule();
				
				return null; //TODO in ajax, remove the click to the link to see if it would speed things up.
			}

			loadWeatherInformation();

		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.debug("Error in saveWeatherOptions" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	
	/**
	 * called fron ajax.. Jsp page
	 * @return
	 */
	public String getWeatherLocation(){

		try{
			cities = new ArrayList<City>();

			if (userInputCity == null || userInputCity.length() ==0){ //verify if we have an input location
				City city = new City();
				city.setNameEn("Please enter city name");
				city.setKey("NoLoc");
				cities.add(city);
			}
			else if (provider.equals(WeatherProvider.ENVCan.name())){
				//load all cities for environment canada with the matched name.
				cities = net.weather.action.WeatherAction.getEnvCanLocationByCityName(userInputCity, true);

				if (cities.size() == 1){
					if (cities.get(0).getKey().equalsIgnoreCase("none")) {
						cities = new ArrayList<City>();
					}
				}				

				//sort
				Collections.sort(cities, new Comparator<City>() {
					public int compare(City o1, City o2) {
						return o1.getNameEn().compareTo(o2.getNameEn());
					}
				});

			}else if(provider.equals(WeatherProvider.WTHUndergrnd.name())){
				log.debug("apiKey: " + apiKey);
				//					Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.omega.dce-eir.net", 8080));	
				//					Utilities.proxy = proxy;
				//					Utilities.createAuthForProxy("axb161", "109256%Rt");
				List<WeatherLocation> locs = net.weather.action.WeatherAction.getWeatherStationLocByCity(apiKey, "Canada", userInputCity);

				for(WeatherLocation loc : locs){

					if(loc.getStationId() != null && loc.getStationId().length() > 0){
						City city = new City();
						if (loc.isAirport())
						{	
							city.setNameEn(loc.getCity() + " - " + loc.getIcao());						
							city.setKey("airportLoc:"+loc.getStationId());
						}
						else
						{
							if (loc.getNeighborhood() == null || loc.getNeighborhood().length() == 0)
							{
								if (loc.getCity() != null && loc.getCity().length() > 0)
								{
									city.setNameEn(loc.getCity() + " - " + loc.getState());	
								}
							}
							else
							{
								city.setNameEn(loc.getNeighborhood() + " , " + loc.getCity() + " - " + loc.getState());	
							}
							city.setKey( "pws:"+loc.getStationId() );
						}													
						cities.add(city);						
					}
				}

			}
		}catch (Exception ex){
			log.debug("Error getting weather location",ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		return SUCCESS;
	}
	
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		
	}
	public Weather getWeather() {
		return weather;
	}
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	public List<WeatherWorker> getWthWorkers() {
		return wthWorkers;
	}
	public void setWthWorkers(List<WeatherWorker> wthWorkers) {
		this.wthWorkers = wthWorkers;
	}
	public List<City> getCities() {
		return cities;
	}
	public void setCities(List<City> cities) {
		this.cities = cities;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getUserInputCity() {
		return userInputCity;
	}

	public void setUserInputCity(String userInputCity) {
		this.userInputCity = userInputCity;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
