package net.project.web.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;

import net.project.common.Constants;
import net.project.common.Utils;
import net.project.db.entities.User;
import net.project.db.entities.Worker;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.WorkerManager;
import net.project.scheduler.ScheduleManager;

import com.opensymphony.xwork2.ActionSupport;

public class WorkerAction extends ActionSupport implements SessionAware{
	
	private Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	private WorkerManager workerManager;
	private Map<String, Object> session;
	
	private List<Worker> workers = new ArrayList<Worker>();
	private Worker worker;
	
	
	private String startStop = "";
	
	public WorkerAction(){
		
		workerManager = new WorkerManager();		
	}
		
	public String listWorkers(){
		String retVal = SUCCESS;

		try{

			User user = (User) session.get(Constants.USER);

			if (user != null && user.canModify()){

				workers = workerManager.loadAllWorkers(false,true, false);
			}
			else{
				retVal = Constants.ACCESS_DENIED;
			}		
		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in listWorkers.", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		return retVal;
	}
	/**
	 * Manage worker.. 
	 * 
	 * Load from DB and the unmanaged cloud
	 * @return
	 */
	public String manageWorker(){

		String retVal = SUCCESS;
		try{
			User user = (User) session.get(Constants.USER);

			if (user != null && user.canModify()){

				worker = workerManager.loadWorkerById(worker.getWorkId(), false, false);
			}
			else{
				retVal = Constants.ACCESS_DENIED;
			}		

		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in manageWorker.", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	/**
	 * Decide to edit from the database of if not managed , add it to the DB.
	 * @return
	 */
	public String saveWorker(){
		
		String retVal = SUCCESS;
		try{
			User user = (User) session.get(Constants.USER);

			if (user != null && user.canModify()){

				log.debug("Saving worker: " + worker);
				
				if (worker.getName() == null || worker.getName().trim().length() == 0){
					addFieldError("name", "You must enter a name ");
				}
				if (worker.getWaterElapseTime() == 0 || worker.getWaterElapseTime() < 5){
					addFieldError("elapseTime", "You must put an elapsed time of 5 minutes or greater");
				}	
				
				
				if (!hasFieldErrors()){
					if(!worker.isManaged()){					
						worker.setManaged(true);										
					}					
					
					//load worker old information to get the schedules and status
					Worker dbWorker = workerManager.loadWorkerById(worker.getWorkId(), true, false);
					
					//check schedule
					if (ScheduleManager.threads.containsKey(worker.getWorkId())){						

						boolean changed = false;
						
						log.debug("worker.getSchedType() : " + worker.getSchedType() + " dbWorker.getSchedType() " + dbWorker.getSchedType() + " worker.getSchedStartTime() " + worker.getSchedStartTime() + " dbWorker.getSchedStartTime() " + dbWorker.getSchedStartTime());
						
						if ( worker.getSchedType() != dbWorker.getSchedType() || !worker.getSchedStartTime().equals(dbWorker.getSchedStartTime()) )
						{
							//schedule changed
							changed = true;
						}
						log.debug("changed: " + changed);
						if (changed){
							//cancel job
							ScheduleManager.cancelJob(worker.getWorkId());							
							//restart only if schedule defined in worker
							if (worker.getSchedStartTime() != null && worker.getSchedStartTime().length() > 0 && worker.getSchedType() != null){
								long delay = ScheduleManager.newSchedule(worker, true);
								worker.setNextWateringBySched(new Date(new Date().getTime() + delay));
							}
						}
						else{
							//keep same next schedule
							worker.setNextWateringBySched(dbWorker.getNextWateringBySched());
						}
					}
					
					WorkerStatus status = dbWorker.getStatus();
					
					if (status == null){
						status = new WorkerStatus();
						status.setWorkerId(worker.getWorkId());
					}
					status.setRecordedDate(new Date());
					status.setSystemComment("Manually updated worker");
					
					worker.setStatus(status);
					worker.setStartSleepTime(dbWorker.getStartSleepTime());
					worker.setStopSleepTime(dbWorker.getStopSleepTime());
					
					
					workerManager.updateWorker(worker, true);
					
					addActionMessage("Save successful");
					
					//re-load workers
					workers = workerManager.loadAllWorkers(false,true, false);
				}
				else
				{
					retVal = INPUT;
				}
			}
			else{
				retVal = Constants.ACCESS_DENIED;
			}		

		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in save worker" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	public String startStopSchedule(){
		
		String retVal = SUCCESS;
		
		boolean start = ("start".equals(startStop)? true : false);
		
		try{
			User user = (User) session.get(Constants.USER);

			if (user != null && user.canModify()){
				
				if (start){

					//verify that we don't have one first.. if yes, stop it.					
					Worker w = workerManager.loadWorkerById(worker.getWorkId(), false, false);

					//cancel job
					ScheduleManager.cancelJob(w.getWorkId());

					//verify that we have a schedule setup for the worker
					if (w.getSchedStartTime() != null && w.getSchedStartTime().length() > 0 && w.getSchedType() != null){

						//start job
						long delay = ScheduleManager.newSchedule(w, true);

						w.setScheduleRunning(true);
						w.setNextWateringBySched(new Date(new Date().getTime() + delay));
						workerManager.updateWorker(w, false);

						addActionMessage("Schedule started");
					}else{
						addActionMessage("Please define a schedule for the worker.");
					}
				}else{
					
					ScheduleManager.cancelJob(worker.getWorkId());
					
					Worker w = workerManager.loadWorkerById(worker.getWorkId(), false, false);
					w.setScheduleRunning(false);
					w.setNextWateringBySched(null);
					workerManager.updateWorker(w, false);
					
					addActionMessage("Schedule Stopped");
					log.debug("Scheduled Stopped. "  + w);
				}
				workers = workerManager.loadAllWorkers(false,true, false);
			}
			else{
				retVal = Constants.ACCESS_DENIED;
			}
		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in start and stop schedule", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		
		return retVal;
	}
	
	public String deleteWorker(){

		String retVal = INPUT;
		User user = (User) session.get(Constants.USER);

		try{
			if (user != null && user.canModify())
			{
				if (worker.getWorkId().length() > 0){
					workerManager.deleteWorker(worker.getWorkId());					
					
					//check schedule
					if (ScheduleManager.threads.containsKey(worker.getWorkId())){	
						ScheduleManager.cancelJob(worker.getWorkId());		
					}
					

					workers = workerManager.loadAllWorkers(false,true, false);
					
					addActionMessage("Worker : " + worker.getWorkId() + " has been deleted");
					
					retVal = SUCCESS;
				}
			}
			else
			{
				retVal = Constants.ACCESS_DENIED;
			}
		}catch(Exception ex){
			addActionError(Constants.errorMessage);
			log.error("Error in deleting worker" , ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		return retVal;
	}
	
	@Override
	public void setSession(Map<String, Object> session) {
	
		this.session = session;
	}
	public List<Worker> getWorkers() {
		return workers;
	}
	public void setWorkers(List<Worker> workers) {
		this.workers = workers;
	}
	public Worker getWorker() {
		return worker;
	}
	public void setWorker(Worker worker) {
		this.worker = worker;
	}


	public String getStartStop() {
		return startStop;
	}


	public void setStartStop(String startStop) {
		this.startStop = startStop;
	}
}
