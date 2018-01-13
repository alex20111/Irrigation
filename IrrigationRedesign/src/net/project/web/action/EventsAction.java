package net.project.web.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.project.common.Constants;
import net.project.common.Event;
import net.project.common.Utils;
import net.project.db.entities.Worker;
import net.project.db.manager.WorkerManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class EventsAction  extends ActionSupport implements SessionAware{
	
	private static final long serialVersionUID = -2929282289548956132L;

	private Log log = LogFactory.getLog(getClass());
	
	private Map<String, Object> session;
	
	private String workerId = "";
	private String fromDate = "";
	private String toDate = "";
		
	private List<Worker> workers = new ArrayList<Worker>();
	
	private WorkerManager wm;
	
	public EventsAction(){
		wm = new WorkerManager();
	}	
	
	public String listEvents(){		

		String retVal = SUCCESS;

		try{

			//1st remove new event bell if exist;
			if (session.containsKey("EventStartdate"))
			{
				session.remove("EventStartdate");
			}

			//load today's date events
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			try{					
				if (getFromDate().length() > 0 && getToDate().length() > 0){
					start.setTime(sdf.parse(getFromDate()));
					end.setTime(sdf.parse(getToDate()));					
				}
			}catch(ParseException pe){
				addActionError("Please enter a valid date");
			}	


			if (!hasActionErrors()){
				start.set(Calendar.HOUR_OF_DAY, 0);
				start.set(Calendar.MINUTE, 0);
				start.set(Calendar.SECOND,0);

				end.set(Calendar.HOUR_OF_DAY, 23);
				end.set(Calendar.MINUTE, 59);
				end.set(Calendar.SECOND,59);				

				setFromDate(start.get(Calendar.YEAR) + "-" + (start.get(Calendar.MONTH) + 1) + "-" + start.get(Calendar.DAY_OF_MONTH));
				setToDate(end.get(Calendar.YEAR) + "-" + (end.get(Calendar.MONTH) + 1) + "-" + end.get(Calendar.DAY_OF_MONTH));

				List<Event> events = new ArrayList<Event>();
				if (workerId == null || workerId.trim().length() == 0){
					events = wm.loadAllEventsByDate(null,start.getTime(), end.getTime());				
				}else{
					events = wm.loadAllEventsByDate(workerId.trim(), start.getTime(), end.getTime());
				}						

				//sort
				Collections.sort(events, new Comparator<Event>() {
					public int compare(Event o1, Event o2) {
						return o1.getEventDate().compareTo(o2.getEventDate());
					}
				});
				Collections.reverse(events);

				session.put("displayEvents", events);
			}
			//load all workers
			workers = wm.loadAllWorkers(false, false, false); 


		}catch(Exception ex){
			log.error("Error in listEvents", ex);
			addActionError(Constants.errorMessage);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}

	public String refreshEvents(){
		
		return SUCCESS;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		
	}
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public List<Worker> getWorkers() {
		return workers;
	}
	public void setWorkers(List<Worker> workers) {
		this.workers = workers;
	}
}
