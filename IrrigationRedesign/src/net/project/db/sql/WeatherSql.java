package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Constants;
import net.project.db.entities.Weather;
import net.project.db.entities.WeatherWorker;
import net.project.db.entities.Worker;
import net.project.enums.WeatherStatus;

public class WeatherSql {

	private static Log log = LogFactory.getLog(WeatherSql.class);


	public Weather loadWeatherInformation(boolean loadWorkers) throws SQLException, ClassNotFoundException{

		Weather weather = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "SELECT * FROM " + Weather.TABLE_NAME;

			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {
				weather = new Weather(rs);	

				//load workers outside
				if (loadWorkers){

					List<WeatherWorker> ww = new ArrayList<WeatherWorker>();

					String workerQuery = "SELECT * FROM " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WEATHER_ID + " = :weatherId"; 
					ResultSet rsWeather = con.createSelectQuery(workerQuery).setParameter("weatherId", weather.getId()).getSelectResultSet();
					while (rsWeather.next()) {
						WeatherWorker ws = new WeatherWorker(rsWeather);
						ww.add(ws);
					}

					List<Worker> workers = new ArrayList<Worker>();

					if (ww.size() > 0){					
						//get the workers
						WorkerSql ws = new WorkerSql();
						for(WeatherWorker w : ww){
							Worker wrk = ws.loadWorkerById(w.getWorkerId(), true, false);

							wrk.setWeatherWrk(w);

							if (wrk != null){
								workers.add(wrk);
							}
						}
					}			

					weather.setWorkers(workers);
				}
			}


		}finally{
			if (con!=null){
				con.close();
			}
		}

		return weather;
	}

	public void addWeatherInfo(Weather weather, List<WeatherWorker> weatherWks)  throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			log.debug("adding weather : " + weather);

			int id = con.buildAddQuery(Weather.TABLE_NAME)
					.setParameter(Weather.PROVIDER, weather.getProvider().name())
					.setParameter(Weather.REFRESH, weather.getRefresh())
					.setParameter(Weather.ACTIVE, weather.isActive())
					.setParameter(Weather.LOCATION, weather.getLocation())
					.setParameter(Weather.SEARCHED_CITY, weather.getSearchedCity())
					.setParameter(Weather.TEMP, weather.getCurrTemp())
					.setParameter(Weather.WEATHER_STATUS, weather.getWeatherStatus().name())
					.setParameter(Weather.LAST_UPDATE, weather.getLastUpdated())
					.setParameter(Weather.API_KEY, weather.getApiKey())
					.add();


			if ( weatherWks != null && weatherWks.size() > 0){
				con.buildAddQuery(WeatherWorker.TABLE_NAME);
				for(WeatherWorker ww : weatherWks){				
					con.setParameter(WeatherWorker.WEATHER_ID,id)
					.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
					.setParameter(WeatherWorker.ACTIVE, ww.isActive())
					.addToBatch();
				}

				con.executeBatchQuery();
			}


		}finally{
			if (con!=null){
				con.close();
			}
		}	

	}

	public void updateWeather(Weather weather) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			con.buildUpdateQuery(Weather.TABLE_NAME)
			.setParameter(Weather.PROVIDER, weather.getProvider().name())
			.setParameter(Weather.REFRESH, weather.getRefresh())
			.setParameter(Weather.ACTIVE, weather.isActive())
			.setParameter(Weather.LOCATION, weather.getLocation())
			.setParameter(Weather.SEARCHED_CITY, weather.getSearchedCity())
			.setParameter(Weather.WEATHER_STATUS, weather.getWeatherStatus().name())
			.setParameter(Weather.TEMP, weather.getCurrTemp())
			.setParameter(Weather.LAST_UPDATE, weather.getLastUpdated())
			.setParameter(Weather.API_KEY, weather.getApiKey())


			.addUpdWhereClause("WHERE " + Weather.ID + " = :workerId", weather.getId())
			.update();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public List<WeatherWorker> loadAllWeatherWorkers(int weatherId) throws SQLException, ClassNotFoundException{

		List<WeatherWorker> ww = new ArrayList<WeatherWorker>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String workerQuery = "SELECT * FROM " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WEATHER_ID + " = :weatherId"; 
			ResultSet rsWeather = con.createSelectQuery(workerQuery).setParameter("weatherId", weatherId).getSelectResultSet();
			while (rsWeather.next()) {
				WeatherWorker ws = new WeatherWorker(rsWeather);
				ww.add(ws);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return ww;
	}

	public void addWeatherWorker(WeatherWorker ww) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			con.buildAddQuery(WeatherWorker.TABLE_NAME)
			.setParameter(WeatherWorker.WEATHER_ID, ww.getWeatherId())
			.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
			.setParameter(WeatherWorker.ACTIVE, ww.isActive())
			.add();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public void updateWeatherWorker(WeatherWorker ww) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);
			con.buildUpdateQuery(WeatherWorker.TABLE_NAME)
			.setParameter(WeatherWorker.WEATHER_ID, ww.getWeatherId())
			.setParameter(WeatherWorker.WORKER_ID, ww.getWorkerId())
			.setParameter(WeatherWorker.ACTIVE, ww.isActive())

			.addUpdWhereClause("WHERE " + WeatherWorker.WORKER_ID + " = :workerId", ww.getWorkerId())
			.update();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public void removeWeatherWorkers(List<WeatherWorker> wthWrkList) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			StringBuilder delete = new StringBuilder("DELETE FROM " + WeatherWorker.TABLE_NAME + " where ( " );

			boolean first = true;
			for(int i=0 ; i < wthWrkList.size() ; i ++) {

				if(first){
					delete.append( WeatherWorker.WORKER_ID + "  = :id"+i );
					first = false;
				}else{
					delete.append(" OR " + WeatherWorker.WORKER_ID + " = :id"+i );
				}
			}
			delete.append( " ) ");

			con.createSelectQuery(delete.toString());

			for(int i=0 ; i < wthWrkList.size() ; i ++){
				con.setParameter("id"+i, wthWrkList.get(i).getWorkerId());
			}
			con.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public Weather weatherActive() throws SQLException, ClassNotFoundException{
		Weather w = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String weatherQuery = "SELECT "+Weather.WEATHER_STATUS + "," + Weather.ACTIVE + " FROM " + Weather.TABLE_NAME; 

			ResultSet rsWeather = con.createSelectQuery(weatherQuery).getSelectResultSet();

			while (rsWeather.next()) {
				w = new Weather();
				w.setWeatherStatus(WeatherStatus.valueOf(rsWeather.getString(Weather.WEATHER_STATUS)));
				w.setActive(rsWeather.getBoolean(Weather.ACTIVE));

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return w;
	}

}
