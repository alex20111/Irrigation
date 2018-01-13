package net.project.scheduler;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.project.db.entities.Worker;
import net.project.enums.SchedType;
import net.project.exception.ValidationException;
import net.project.thread.CheckWorkers;
import net.project.thread.PowerSavingSched;
import net.project.thread.SchedThread;
import net.project.thread.WeatherSched;

public class ScheduleManager {
	
	private static Log log = LogFactory.getLog(ScheduleManager.class);
	
	public static Map<String, ScheduledFuture<?>> threads = new HashMap<String, ScheduledFuture<?>>();
	public static Map<String, SchedThread> threadsClass = new HashMap<String, SchedThread>();
	public static ScheduledExecutorService scheduledThreadPool;
	
	private static ScheduledFuture<?> workerVerification = null;
	private static CheckWorkers checkWorker = null;
	
	private static ScheduledFuture<?> weatherStatus = null;
	@SuppressWarnings("unused")
	private static ScheduledFuture<?> powerStatus = null;
	
	
	private static int maxThread = 10;

	/**
	 * 
	 * @param worker - Worker to have the schedule started on
	 * 
	 * @param init - If this is the initial start of the schedule. False would be a restart of the schedule.
	 * @return
	 * @throws SQLException
	 * @throws ValidationException
	 */
	public static Long newSchedule(Worker worker, boolean init) throws SQLException, ValidationException{
		if (scheduledThreadPool == null){
			scheduledThreadPool = Executors.newScheduledThreadPool(maxThread);
		}
		
		//verify if there is actually a schedule on the worker.
		if (worker.getSchedStartTime() == null && worker.getSchedStartTime().length() == 0){
			throw new ValidationException("Need to define a schedule");
		}
		
		
		
		long delay = calculateDelay(worker, init);
		
		log.debug("New schedule type: " + worker.getSchedType() + " init: " + init + " worker id : " + worker.getWorkId() + " " + delay);
		
		SchedThread schdWorker = new SchedThread(worker.getWorkId());
		
		ScheduledFuture<?> job = scheduledThreadPool.schedule(schdWorker, delay, TimeUnit.MILLISECONDS);
		
		threads.put(worker.getWorkId(), job);
		threadsClass.put(worker.getWorkId(), schdWorker);

		return delay;
		
	}
	public static void cancelJob(String workerId) throws SQLException, ValidationException, InterruptedException{
		 ScheduledFuture<?> job = threads.get(workerId);
		 SchedThread stopThrd = threadsClass.get(workerId);		 
	
		 //TODO , turn off water if watering when cancelling the schedule?
		 
		if (job != null){
			stopThrd.setRestart(false);	
					
			job.cancel(true);
			
			//if job is currently running. wait until complete.
			if (stopThrd.running){
				int cnt  = 0;
				while (stopThrd.running && cnt < 60){ Thread.sleep(100); cnt++;}

				if (cnt == 60){
					throw new ValidationException("Could not cancel schedule. CNT == 60");
				}
			}
			threads.remove(workerId);
			threadsClass.remove(workerId);
			log.debug("job cancelled");
		}	
	}
	
	
	/**
	 * Start or restart a worker verification schedule to verify if the worker is still alive. 
	 * @param timeInMinutes - number of minutes to verify . Default 1 hour.
	 */
	public static void scheduleWorkerVerification(int timeInMinutes, int webStartup){
		if (scheduledThreadPool == null){
			scheduledThreadPool = Executors.newScheduledThreadPool(maxThread);
		}
		
		boolean canStartJob = true;
		
		if (timeInMinutes > 0){
			log.debug("Scheduling worker checkup thread");

			if (workerVerification != null){
				workerVerification.cancel(true);

				int cnt = 0;

				while(!checkWorker.jobDone && cnt < 30){ //wait 3 secondes
					//wait
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
					cnt++;
				}
				
				if (cnt > 20){
					canStartJob = false;
				}
			}

			if (canStartJob){
				checkWorker = new CheckWorkers();
				workerVerification = scheduledThreadPool.scheduleWithFixedDelay(checkWorker, webStartup, timeInMinutes, TimeUnit.MINUTES);
			}
		}		
	}	
	public static void scheduleWeatherStatus(int delayPeriod, int initDelay){
		if (scheduledThreadPool == null){
			scheduledThreadPool = Executors.newScheduledThreadPool(maxThread);
		}
		
		if (delayPeriod > 0){
			log.debug("Scheduling Weather status");

			if (weatherStatus != null){
				
				cancelWeatherSchedule();
			}

			weatherStatus = scheduledThreadPool.scheduleAtFixedRate(new WeatherSched(), initDelay, delayPeriod, TimeUnit.MINUTES);
		}		
	}

	public static void cancelWeatherSchedule(){
		
		if (weatherStatus != null){
			weatherStatus.cancel(true);

			int cnt = 0;

			while(!weatherStatus.isDone() && cnt < 20){ //wait 2 secondes
				//wait
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				cnt++;
			}

			if (cnt == 20){
				log.error("Tried to cancel weatherStatus thread but exceede delay of CNT -> " + cnt);
			}
		}
	}
	
	public static void schedulePowerSaving(){
		if (scheduledThreadPool == null){
			scheduledThreadPool = Executors.newScheduledThreadPool(maxThread);
		}
		Calendar now = Calendar.getInstance();
		Calendar futureDate = Calendar.getInstance();
		
		if(futureDate.get(Calendar.MINUTE) >= 30){
			futureDate.add(Calendar.HOUR_OF_DAY, 1);
			futureDate.set(Calendar.MINUTE, 00);
		}else{
			futureDate.set(Calendar.MINUTE, 30);			
		}
		
		futureDate.set(Calendar.SECOND, 00);
		futureDate.set(Calendar.MILLISECOND, 00);		
		
		long delay = futureDate.getTimeInMillis() - now.getTimeInMillis();

		log.debug("starting power Saving sched at: " + new Date(new Date().getTime() + delay));
		
		
		powerStatus = scheduledThreadPool.scheduleAtFixedRate(new PowerSavingSched(), delay, 1800000, TimeUnit.MILLISECONDS);
			
	}
	
	
	
	private static long calculateDelay(Worker worker, boolean init){
		
		long delay = 0l;
		int day = 86400000; 		

		Calendar now = Calendar.getInstance();

		Calendar st = Calendar.getInstance();
			
		String start = worker.getSchedStartTime();
		
		int hour = Integer.parseInt(start.substring(0,2));
		int min = Integer.parseInt(start.substring(3,start.length()));
		
		if (init){			

			st.set(Calendar.HOUR_OF_DAY, hour);
			st.set(Calendar.MINUTE, min);
			st.set(Calendar.SECOND, 0);
			st.set(Calendar.MILLISECOND, 0);
			
			if (now.before(st)){

				delay = st.getTimeInMillis() - now.getTimeInMillis();

			}else if(now.after(st)){
				//date is tomorrow. --> start tomorrow
				delay = (st.getTimeInMillis() + 86400000) - now.getTimeInMillis();
			}
		}else{
			
			st.set(Calendar.HOUR_OF_DAY, hour);
			st.set(Calendar.MINUTE, min);
			st.set(Calendar.SECOND, 0);
			st.set(Calendar.MILLISECOND, 0);
		
			if (worker.getSchedType() == SchedType.daily){
				st.setTimeInMillis(st.getTimeInMillis() + day);
			}else if (worker.getSchedType() == SchedType.twoDays){
				st.setTimeInMillis(st.getTimeInMillis() + (day * 2));
			}else if (worker.getSchedType() == SchedType.threeDays){
				st.setTimeInMillis(st.getTimeInMillis() + (day * 3));
			}else if (worker.getSchedType() == SchedType.fourDays){
				st.setTimeInMillis(st.getTimeInMillis() + (day * 4));
			}else if (worker.getSchedType() == SchedType.week){
				st.setTimeInMillis(st.getTimeInMillis() + (day * 7));
			}else if (worker.getSchedType() == SchedType.daily){
				
			}
			delay = st.getTimeInMillis() - now.getTimeInMillis();
			
		}
		return delay;
	}
	


	
}
