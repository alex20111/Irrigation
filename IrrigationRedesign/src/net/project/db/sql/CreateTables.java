package net.project.db.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Constants;
import net.project.db.entities.Config;
import net.project.db.entities.User;
import net.project.db.entities.Weather;
import net.project.db.entities.WeatherWorker;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.WorkerManager;
import net.project.enums.AccessEnum;
import net.project.exception.ValidationException;
import net.project.security.PassHash;

import net.project.enums.LightLevel;
import net.project.enums.WeatherStatus;

public class CreateTables {

	private static Log log = LogFactory.getLog(CreateTables.class);
	
	@SuppressWarnings("resource")
	public static boolean createTables() throws SQLException, ClassNotFoundException{

		Connection con = null;
		PreparedStatement pst = null;

		
		try {
			Class.forName("org.h2.Driver");

			con = DriverManager.getConnection(Constants.url,Constants.dbUser, Constants.dbPassword);

			//check if tables exist, if not create the tables
			DatabaseMetaData meta = con.getMetaData();
			ResultSet tables = meta.getTables(null, null, User.TBL_NAME.toUpperCase(), null);
			
			if (!tables.next()) {

				log.debug("Database Does not exist, create");
				pst = con.prepareStatement(User.createTable());
				pst.executeUpdate();					
				pst = con.prepareStatement(Worker.createTable());
				pst.executeUpdate();
				pst = con.prepareStatement(WorkerStatus.createTable());
				pst.executeUpdate();

				pst = con.prepareStatement(Config.createTable());
				pst.executeUpdate();
				
				pst = con.prepareStatement(WeatherWorker.createTable());
				pst.executeUpdate();
				
				pst = con.prepareStatement(Weather.createTable());
				pst.executeUpdate();
				
				
				
				//add index if needed
//				pst = con.prepareStatement(Temperature.createIndex());
//				pst.executeUpdate();
//				return "create INDEX idx_dt on " + TBL_NAME +"(" + TEMP_DATE + ");";
				
				
				//add admin user (default user)
				UserSql uSql = new UserSql();
				User user = new User();
				user.setUserName("admin");
				user.setPassword(PassHash.hashPassword("admin"));
				user.setAccess(AccessEnum.ADMIN);
				user.setEmail("Home");
				user.setFirstName("Admin");
				user.setLastName("");

				uSql.addUser(user);
				
				//add guest
				User guest = new User();
				guest.setUserName("guest");
				guest.setPassword(PassHash.hashPassword("guest"));
				guest.setAccess(AccessEnum.VIEW);
				guest.setEmail("guest");
				guest.setFirstName("Guest");
				guest.setLastName("");

				uSql.addUser(guest);	
				
				
				Config config = new Config();
				
				ConfigSql sql = new ConfigSql();
				sql.addConfig(config);
				
				log.debug("Database Created!");

//				createTempWorkers();
			}

		} finally {

			if (pst != null) {
				pst.close();
			}
			if (con != null) {
				con.close();
			}

		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private static void createTempWorkers(){
		WorkerManager wm = new WorkerManager();
		
		
		Worker w1 = new Worker();
		w1.setDescription("1st of 2");
		w1.setManaged(false);
		w1.setName("Outside garden");
		w1.setNextWateringBySched(null);
		w1.setSchedStartTime(null);
		w1.setScheduleRunning(false);
		w1.setWaterElapseTime(0);
		w1.setWorkId("122");
		w1.setDoNotWater(false);
		
		WorkerStatus ws1 = new WorkerStatus();
		ws1.setBatteryLevel(0.0);
		ws1.setConnected(true);
		ws1.setLightStatus(LightLevel.Sunny);
		ws1.setRainStatus(WeatherStatus.sunny);
		ws1.setRecordedDate(new Date());
		ws1.setSystemComment("TEST TEST TES");
		ws1.setWaterConsumption(0);
		ws1.setWorkerId("122");
		ws1.setWorkerWatering(false);
		
		w1.setStatus(ws1);
		
		try {
			wm.addWorker(w1, true);
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (ValidationException e) {
			
			e.printStackTrace();
		}
		
		Worker w2 = new Worker();
		w2.setDescription("2 of 2");
		w2.setManaged(false);
		w2.setName("Outside garden Way there");
		w2.setNextWateringBySched(null);
		w2.setSchedStartTime(null);
		w2.setScheduleRunning(false);
		w2.setWaterElapseTime(0);
		w2.setWorkId("111");
		w2.setDoNotWater(false);
		
		WorkerStatus ws2 = new WorkerStatus();
		ws2.setBatteryLevel(0.0);
		ws2.setConnected(false);
		ws2.setLightStatus(LightLevel.Dark);
		ws2.setRainStatus(WeatherStatus.raining);
		ws2.setRecordedDate(new Date());
		ws2.setSystemComment("TEST TEST TEwwwwwwwwwwS");
		ws2.setWaterConsumption(0);
		ws2.setWorkerId("111");
		ws2.setWorkerWatering(false);
		
		w2.setStatus(ws2);


		try {
			wm.addWorker(w2, true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		
		
	}
}
