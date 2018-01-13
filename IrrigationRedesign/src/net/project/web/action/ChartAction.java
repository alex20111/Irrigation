package net.project.web.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;

import net.project.common.Constants;
import net.project.common.Utils;
import net.project.db.entities.User;
import net.project.db.entities.WorkerStatus;
import net.project.db.manager.WorkerManager;

import com.opensymphony.xwork2.ActionSupport;

public class ChartAction extends ActionSupport implements SessionAware {

	private Log log = LogFactory.getLog(getClass());
	
	private static final long serialVersionUID = 7369460977074209806L;
	private Map<String, Object> session;
	private List<WorkerStatus> workerStatusList;
	private String workerId = "";
	
	private String fromDate = "";
	private String toDate = "";
	
	public String loadChartsAndStatus(){
		WorkerManager wm = new WorkerManager();
		String retVal = SUCCESS;
		User user = (User) session.get(Constants.USER);
		try {
			if (user != null && user.canModify()){	
				
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


					workerStatusList = wm.loadAllWorkersStatusById(workerId, start.getTime(), end.getTime());					
	
					Collections.sort(workerStatusList, new Comparator<WorkerStatus>() {
						public int compare(WorkerStatus o1, WorkerStatus o2) {
							return o1.getRecordedDate().compareTo(o2.getRecordedDate());
						}
					});
					Collections.reverse(workerStatusList);
				}
			}
			else
			{
				retVal = Constants.ACCESS_DENIED;
						
			}
		} catch (Exception e) {
			log.error("Error loading charts", e);
			Utils.sendErrorMessage(e); //send error message if requested.
		}
		
		return retVal;
	}

	public List<WorkerStatus> getWorkerStatusList() {
		return workerStatusList;
	}

	public void setWorkerStatusList(List<WorkerStatus> workerStatusList) {
		this.workerStatusList = workerStatusList;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		
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
}
