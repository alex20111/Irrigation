package net.project.db.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.common.Event;
import net.project.db.entities.Weather;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.sql.WorkerSql;
import net.project.exception.ValidationException;

public class WorkerManager {

	private Log log = LogFactory.getLog(getClass());
	
	private WorkerSql sql = null;

	public WorkerManager(){
		sql = new WorkerSql();
	}

	/**
	 * 
	 * 
	 * @param onlyManaged - Load all the workers that are managed (Meaning that are added and managed by the system)
	 * @param getStatus - get current status of the worker.
	 * @param getWeather
	 * @return
	 * @throws SQLException
	 * 
	 * a unmanaged worker mesans that he self identified but we did not do anything with it yet!
	 */
	public List<Worker> loadAllWorkers(boolean onlyManaged, boolean currStatus, boolean getWeather) throws SQLException{

		List<Worker> workers = sql.loadAllWorkers(onlyManaged, currStatus, getWeather);		

		return workers;
	}

	public List<Worker> loadAllUnmanagedWorkers() throws SQLException{

		List<Worker> workers = sql.loadAllUnmanagedWorkers();		

		return workers;
	}
	
	public Worker loadWorkerById(String workerId, boolean currentStatus, boolean loadWeather) throws SQLException{
		Worker worker = null;

		if (workerId != null && workerId.length() > 0){

			worker = sql.loadWorkerById(workerId, currentStatus, loadWeather);
		}
		return worker;
	}

	public void addWorker(Worker worker, boolean addStatus) throws SQLException, ValidationException{

		if (worker != null){
			sql.addWorker(worker);
			
			if (worker.getStatus() != null && addStatus){
				
				//add current weather if not from worker..
				if(!worker.getStatus().isRainingFromWorker()){
					WeatherManager wm = new WeatherManager();
					Weather w = wm.weatherActive();
					if (w != null && w.isActive()){
						worker.getStatus().setRainStatus(w.getWeatherStatus());
					}
				}			
				
				worker.getStatus().setWorkerId(worker.getWorkId());
				sql.addWorkerStatus(worker.getStatus());
			}
		}else{
			throw new ValidationException("Worker is null in addWorker");
		}
	}

	public void updateWorker(Worker worker, boolean addStatus) throws SQLException, ValidationException{

		if (worker != null){
			
			sql.updateWorker(worker);
			
			//update the status if any
			if (worker.getStatus() != null && addStatus){
				
				//add current weather if not from worker..
				if(!worker.getStatus().isRainingFromWorker()){
					log.debug("Update Worker and not raining " + worker.getStatus().getRainStatus());
					WeatherManager wm = new WeatherManager();
					Weather w = wm.weatherActive();
					if (w != null && w.isActive()){
						worker.getStatus().setRainStatus(w.getWeatherStatus());
					}
				}
				
				worker.getStatus().setWorkerId(worker.getWorkId());
				sql.addWorkerStatus(worker.getStatus());
			}
			
		}else{
			throw new ValidationException("Worker is null in updateWorker");
		}
	}

	public void deleteWorker(String workerId) throws SQLException{
		if (workerId.length() > 0){
			sql.deleteWorker(workerId);
			
			//check if the worker has status also. if yes, delete them
			sql.deleteWorkerStatus(workerId);
			
			//weather status also
			sql.deleteWorkerWeather(workerId);
			
		}
	}
	public List<WorkerStatus> loadAllWorkersStatusById(String workerId, Date from, Date to) throws SQLException{

		List<WorkerStatus> workerStatus = sql.loadAllWorkerStatusById(workerId, from, to)	;		

		return workerStatus;
	}
	
	/**
	 * An event is basically all workers status for that range of date.
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws SQLException
	 */
	public List<Event> loadAllEventsByDate(String workerId , Date fromDate , Date toDate) throws SQLException{
		List<Event> events = new ArrayList<Event>();

		if (fromDate != null && toDate != null){
			
			if (workerId != null && workerId.length() > 0){
				List<WorkerStatus> ws = loadAllWorkersStatusById(workerId,fromDate, toDate);
				
				for(WorkerStatus w : ws){
					Event ev = new Event();
					ev.setEvent(w.getSystemComment());
					ev.setEventDate(w.getRecordedDate());
					ev.setEvntWorkerId(w.getWorkerId());
					events.add(ev);
				}
			
			}else{
				events = sql.loadAllWorkerCommentsByDates(fromDate, toDate);
			}
		}
		return events;
	}
	
	public WorkerStatus loadCurrentStatusByWorkerId(String workerId) throws SQLException{
		return sql.loadCurrentStatusByWorkerId(workerId, null);
	}
	/**
	 * Return all managed workers for power saving.
	 * ID, Name, StartSleep, StopSleep. fields.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Worker> loadAllWorkersForPowerSaving() throws SQLException{
		return sql.loadAllWorkersForPowerSaving();
	}
	/**
	 * Return all workers that have a matching start or stop sleep time.
	 * @param time
	 * @return
	 * @throws SQLException
	 */
	public List<Worker> loadPwrWrkByStarOrStopTime(int time) throws SQLException{
		return sql.loadPwrWrkByStarOrStopTime(time);
	}
}
