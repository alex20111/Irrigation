package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Constants;
import net.project.common.Event;
import net.project.db.entities.WeatherWorker;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.enums.SchedType;

public class WorkerSql {

	private static Log log = LogFactory.getLog(WorkerSql.class);

	public List<Worker> loadAllWorkers(boolean onlyManaged, boolean currStatus, boolean getWeather) throws SQLException, ClassNotFoundException{

		List<Worker> workers = new ArrayList<Worker>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "SELECT * FROM " + Worker.TABLE_NAME;

			if (onlyManaged){
				query += " WHERE " + Worker.MANAGED + " = true";
			}		

			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {
				Worker worker = new Worker(rs);

				if (currStatus){				
					String query2 = "SELECT * FROM "
							+ WorkerStatus.TABLE_NAME + " where " + WorkerStatus.WORKER_ID_FK + " = :workerId "
							+ " AND " + WorkerStatus.REC_DATE + " = select max("+WorkerStatus.REC_DATE+")    from "
							+ WorkerStatus.TABLE_NAME + "  where " + WorkerStatus.WORKER_ID_FK +" = :workerId2";

					ResultSet rs2 = con.createSelectQuery(query2)
							.setParameter("workerId", worker.getWorkId())
							.setParameter("workerId2",  worker.getWorkId())
							.getSelectResultSet();

					while (rs2.next()) {
						WorkerStatus ws = new WorkerStatus(rs2);
						worker.setStatus(ws);
					}		
				}	

				if (getWeather){
					String weatherQuery = "SELECT * FROM " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WORKER_ID + " = :workerId"; 
					ResultSet rsWeather = con.createSelectQuery(weatherQuery).setParameter("workerId", worker.getWorkId()).getSelectResultSet();
					while (rsWeather.next()) {
						WeatherWorker ws = new WeatherWorker(rsWeather);
						worker.setWeatherWrk(ws);
					}
				}

				workers.add(worker);										
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return workers;
	}
	public List<Worker> loadAllUnmanagedWorkers() throws SQLException, ClassNotFoundException{

		List<Worker> workers = new ArrayList<Worker>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "SELECT * FROM " + Worker.TABLE_NAME + " Where " + Worker.MANAGED + " = :managed";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("managed", false)
					.getSelectResultSet();

			while (rs.next()) {
				Worker worker = new Worker(rs);		
				workers.add(worker);
			}					


		}finally{
			if (con!=null){
				con.close();
			}
		}

		return workers;
	}
	public Worker loadWorkerById(String workerId, boolean getCurrentStatus, boolean loadWeather) throws SQLException, ClassNotFoundException{

		Worker worker = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "SELECT * FROM " + Worker.TABLE_NAME + " where " + Worker.WORKER_ID + " = :id";

			ResultSet rs = con.createSelectQuery(query).setParameter("id", workerId)
					.getSelectResultSet();

			while (rs.next()) {
				worker = new Worker(rs);

				if (getCurrentStatus){				
					WorkerStatus currStatus = loadCurrentStatusByWorkerId(worker.getWorkId(),con);
					worker.setStatus(currStatus);
				}

				if (loadWeather){
					String weatherQuery = "SELECT * from " + WeatherWorker.TABLE_NAME + " WHERE " + WeatherWorker.WORKER_ID + " = :id";
					ResultSet weatherRS = con.createSelectQuery(weatherQuery).setParameter("id", workerId)
							.getSelectResultSet();

					while (weatherRS.next()) {
						worker.setWeatherWrk(new WeatherWorker(weatherRS));
					}
				}

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}


		return worker;
	}

	public WorkerStatus loadCurrentStatusByWorkerId(String workerId, DBConnection con) throws SQLException, ClassNotFoundException{

		WorkerStatus retWs = null;

		boolean closeConnection = false;

		if (con == null){
			System.out.println("New conn for loadCurrentStatusByWorkerId");
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);
			closeConnection = true;
		}		

		String query = "SELECT * FROM "
				+ WorkerStatus.TABLE_NAME + " where " + WorkerStatus.WORKER_ID_FK + " = :workerId "
				+ " AND " + WorkerStatus.REC_DATE + " = select max("+WorkerStatus.REC_DATE+")    from "
				+ WorkerStatus.TABLE_NAME + "  where " + WorkerStatus.WORKER_ID_FK +" = :workerId2";

		ResultSet rs = con.createSelectQuery(query)
				.setParameter("workerId", workerId)
				.setParameter("workerId2",  workerId)
				.getSelectResultSet();

		while (rs.next()) {
			retWs = new WorkerStatus(rs);			
		}

		if (closeConnection){
			con.close();
		}

		return retWs;

	}

	public void addWorker(Worker worker) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			log.debug("adding worker : " +worker);
			con.buildAddQuery(Worker.TABLE_NAME)
			.setParameter(Worker.WORKER_ID, worker.getWorkId())
			.setParameter(Worker.NAME, worker.getName())
			.setParameter(Worker.DESCRIPTION, worker.getDescription())
			.setParameter(Worker.SCHED_TYPE, SchedType.daily.name())
			.setParameter(Worker.START_TIME, worker.getSchedStartTime())
			.setParameter(Worker.ELAPSE_TIME, worker.getWaterElapseTime())
			.setParameter(Worker.DO_NOT_WATER, worker.isDoNotWater())
			.setParameter(Worker.MANAGED, worker.isManaged())
			.setParameter(Worker.SCHEDULE, worker.isScheduleRunning())
			.setParameter(Worker.NEXT_WATERING, worker.getNextWateringBySched())
			.setParameter(Worker.START_SLEEP_TIME, worker.getStartSleepTime())
			.setParameter(Worker.STOP_SLEEP_TIME, worker.getStopSleepTime())

			.add();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public void updateWorker(Worker worker) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			con.buildUpdateQuery(Worker.TABLE_NAME)
			.setParameter(Worker.NAME, worker.getName())
			.setParameter(Worker.DESCRIPTION, worker.getDescription())
			.setParameter(Worker.SCHED_TYPE, worker.getSchedType().name())
			.setParameter(Worker.START_TIME, worker.getSchedStartTime())
			.setParameter(Worker.ELAPSE_TIME, worker.getWaterElapseTime())
			.setParameter(Worker.DO_NOT_WATER, worker.isDoNotWater())
			.setParameter(Worker.MANAGED, worker.isManaged())
			.setParameter(Worker.SCHEDULE, worker.isScheduleRunning())
			.setParameter(Worker.NEXT_WATERING, worker.getNextWateringBySched())
			.setParameter(Worker.START_SLEEP_TIME, worker.getStartSleepTime())
			.setParameter(Worker.STOP_SLEEP_TIME, worker.getStopSleepTime())

			.addUpdWhereClause("WHERE " + Worker.WORKER_ID + " = :workerId", worker.getWorkId())
			.update();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public void addWorkerStatus(WorkerStatus ws) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			log.debug("adding status : " +ws);

			if (ws.getWorkerId() == null || ws.getWorkerId().trim().length() == 0){
				throw new SQLException("Worker id FK null in worker status");
			}

			//null out the id in case we update
			ws.setId(-1);

			con.buildAddQuery(WorkerStatus.TABLE_NAME)
			.setParameter(WorkerStatus.BATT_LVL, ws.getBatteryLevel())
			.setParameter(WorkerStatus.CONNECTED, ws.isConnected())
			.setParameter(WorkerStatus.RAIN_STATUS, ws.getRainStatus().name())
			.setParameter(WorkerStatus.REC_DATE, ws.getRecordedDate())
			.setParameter(WorkerStatus.LIGHT_STATUS, ws.getLightStatus().name())
			.setParameter(WorkerStatus.WATER_CONS, ws.getWaterConsumption())
			.setParameter(WorkerStatus.WORKER_ID_FK, ws.getWorkerId())
			.setParameter(WorkerStatus.WORKER_ON, ws.isWorkerWatering())
			.setParameter(WorkerStatus.SYS_COMMENT, ws.getSystemComment())
			.setParameter(WorkerStatus.ERR_DETECTED, ws.isErrorDetected())
			.setParameter(WorkerStatus.WORKER_SLEEPING, ws.isSleeping())

			.add();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}



	public void deleteWorker(String workerId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "DELETE FROM " + Worker.TABLE_NAME + " where " + Worker.WORKER_ID + " = :id";

			con.createSelectQuery(query)
			.setParameter("id", workerId)
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}

	public void deleteWorkerStatus(String workerId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "DELETE FROM " + WorkerStatus.TABLE_NAME + " where " + WorkerStatus.WORKER_ID_FK + " = :idfk";

			con.createSelectQuery(query)
			.setParameter("idfk", workerId)
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public void deleteWorkerWeather(String workerId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "DELETE FROM " + WeatherWorker.TABLE_NAME + " where " + WeatherWorker.WORKER_ID + " = :idfk";

			con.createSelectQuery(query)
			.setParameter("idfk", workerId)
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public List<WorkerStatus> loadAllWorkerStatusById(String workerId, Date from, Date to) throws SQLException, ClassNotFoundException{

		List<WorkerStatus> workerStatus = new ArrayList<WorkerStatus>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT * FROM " + WorkerStatus.TABLE_NAME + " Where " + WorkerStatus.WORKER_ID_FK + " = :workerId "+
					" AND "  + WorkerStatus.REC_DATE + " BETWEEN :fromDate AND :toDate ";;

					ResultSet rs = con.createSelectQuery(query)
							.setParameter("workerId", workerId)
							.setParameter("fromDate", from)
							.setParameter("toDate", to)
							.getSelectResultSet();

					while (rs.next()) {
						WorkerStatus ws = new WorkerStatus(rs);		
						workerStatus.add(ws);
					}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return workerStatus;
	}

	public List<Event> loadAllWorkerCommentsByDates(Date fromDate, Date toDate) throws SQLException, ClassNotFoundException{

		List<Event> events = new ArrayList<Event>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT " + WorkerStatus.SYS_COMMENT +
					",  "  + WorkerStatus.REC_DATE +
					", " + WorkerStatus.WORKER_ID_FK +
					" FROM " + WorkerStatus.TABLE_NAME + " Where " + WorkerStatus.REC_DATE + " BETWEEN :fromDate AND :toDate ";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("fromDate", fromDate)
					.setParameter("toDate", toDate)
					.getSelectResultSet();

			while (rs.next()) {
				Event event = new Event();
				event.setEvent(rs.getString(WorkerStatus.SYS_COMMENT));
				event.setEventDate(rs.getTimestamp(WorkerStatus.REC_DATE));
				event.setEvntWorkerId(rs.getString(WorkerStatus.WORKER_ID_FK));

				events.add(event);
			}	
		}finally{
			if (con!=null){
				con.close();
			}
		}

		return events;
	}

	public List<Worker> loadAllWorkersForPowerSaving() throws SQLException, ClassNotFoundException{

		List<Worker> workers = new ArrayList<Worker>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT "  + Worker.WORKER_ID + ", " + Worker.NAME + "," + Worker.START_SLEEP_TIME + " , " + Worker.STOP_SLEEP_TIME +
					" FROM " + Worker.TABLE_NAME + " WHERE " + Worker.MANAGED + " = true ";

			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {
				Worker worker = new Worker();
				worker.setWorkId(rs.getString(Worker.WORKER_ID));
				worker.setName(rs.getString(Worker.NAME));
				worker.setStartSleepTime(rs.getInt(Worker.START_SLEEP_TIME));
				worker.setStopSleepTime(rs.getInt(Worker.STOP_SLEEP_TIME));

				workers.add(worker);										
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return workers;
	}

	public List<Worker> loadPwrWrkByStarOrStopTime(int time) throws SQLException, ClassNotFoundException{

		List<Worker> workers = new ArrayList<Worker>();

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT "  + Worker.WORKER_ID + ", " + Worker.NAME + "," + Worker.START_SLEEP_TIME + " , " + Worker.STOP_SLEEP_TIME +
					" FROM " + Worker.TABLE_NAME + " WHERE " + Worker.MANAGED + " = true " + 
					" AND ( " + Worker.START_SLEEP_TIME + " = :startTime OR " + Worker.STOP_SLEEP_TIME + " = :stopTime ) ";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("startTime", time)
					.setParameter("stopTime", time)
					.getSelectResultSet();

			while (rs.next()) {
				Worker worker = new Worker();
				worker.setWorkId(rs.getString(Worker.WORKER_ID));
				worker.setName(rs.getString(Worker.NAME));
				worker.setStartSleepTime(rs.getInt(Worker.START_SLEEP_TIME));
				worker.setStopSleepTime(rs.getInt(Worker.STOP_SLEEP_TIME));		

				WorkerStatus currStatus = loadCurrentStatusByWorkerId(worker.getWorkId(),con);
				worker.setStatus(currStatus);

				workers.add(worker);										
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return workers;
	}


}
