package net.project.web.action;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;

import net.project.common.Constants;
import net.project.common.Utils;
import net.project.db.entities.User;
import net.project.enums.AccessEnum;

import com.opensymphony.xwork2.ActionSupport;

public class LogFileAction extends ActionSupport implements SessionAware{

	private Log log = LogFactory.getLog(getClass());
	
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
	
	private String selInfoLogs = ""; //path to file
	private String selErrorLogs = "";	
	private String errorLogText = ""; //error text 
	private String infoLogText = "";	
	
	private String btnErrorShow = null;
	private String btnInfoShow = null;
	
	private int selectedTab = 0;
	
	
	public String loadLogs(){
		String retVal = SUCCESS;
		User user = (User)session.get(Constants.USER);

		session.remove("infoLogFiles");
		session.remove("errorLogFiles");
		
		try{
			if (user != null && user.getAccess() == AccessEnum.ADMIN){
				setSelectedTab(0);//select tab 0
				//load file 
				File dir = new File(Constants.logDirLoc);//get log file 

				if (dir.exists()){//if directory exist

					FileFilter fileFilter = new WildcardFileFilter("INFO.log*");
					File[] filesInfo = dir.listFiles(fileFilter);

					if (filesInfo != null && filesInfo.length > 0){
						Arrays.sort(filesInfo, new Comparator<File>(){
							public int compare(File f1, File f2)
							{
								return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
							} });

						session.put("infoLogFiles", filesInfo);

						setSelInfoLogs(filesInfo[0].getPath());
						infoLogText = FileUtils.readFileToString(filesInfo[0]);

					}
					
					fileFilter = new WildcardFileFilter("ERROR.log*");
					File[] filesError = dir.listFiles(fileFilter);
					
					if (filesError != null && filesError.length > 0){
						Arrays.sort(filesError, new Comparator<File>(){
							public int compare(File f1, File f2)
							{
								return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
							} });

						session.put("errorLogFiles", filesError);
						setSelErrorLogs(filesError[0].getPath());
					}		
				}else{
					session.put("infoLogFiles", new ArrayList<String>());
					infoLogText = "Error file not found";
				}

			}else{
				retVal = Constants.ACCESS_DENIED;
			}
		}catch(Exception ex){
			log.error("Error in LogFileAction", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}
		return retVal;
	}

	public String showLogFile(){
		String retVal = SUCCESS;

		User user = (User)session.get(Constants.USER);

		try{
			if (user != null && user.getAccess() == AccessEnum.ADMIN){

				if (log.isDebugEnabled()){
					log.debug("showLogFile");
					log.debug("getBtnInfoShow : " + getBtnInfoShow());
					log.debug("getBtnErrorShow : " + getBtnErrorShow());
					log.debug("selErrorLogs : " + selErrorLogs);
					log.debug("selInfoLogs : " + selInfoLogs);
					log.debug("selectedTab : " + selectedTab);
				}
				
				if (getBtnInfoShow() != null ){
					if (selInfoLogs.length() > 0){
						File file = new File(selInfoLogs);
						infoLogText = FileUtils.readFileToString(file);
					}
					else
					{
						infoLogText = "no existing log file";
					}
					selectedTab = 0;

				}
				else if(getBtnErrorShow() != null ){

					if (selErrorLogs.length() > 0){
						File file = new File(selErrorLogs);
						errorLogText = FileUtils.readFileToString(file);
					}
					else
					{
						errorLogText = "no existing log file";
					}
					selectedTab = 1;
				}			

			}
			else
			{
				retVal = Constants.ACCESS_DENIED;
			}
		}catch(Exception ex){
			log.error("Error in showLogFile", ex);
			Utils.sendErrorMessage(ex); //send error message if requested.
		}

		return retVal;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		
	}

	public String getSelInfoLogs() {
		return selInfoLogs;
	}

	public void setSelInfoLogs(String selInfoLogs) {
		this.selInfoLogs = selInfoLogs;
	}

	public String getSelErrorLogs() {
		return selErrorLogs;
	}

	public void setSelErrorLogs(String selErrorLogs) {
		this.selErrorLogs = selErrorLogs;
	}

	public String getErrorLogText() {
		return errorLogText;
	}

	public void setErrorLogText(String errorLogText) {
		this.errorLogText = errorLogText;
	}

	public String getInfoLogText() {
		return infoLogText;
	}

	public void setInfoLogText(String infoLogText) {
		this.infoLogText = infoLogText;
	}

	public String getBtnErrorShow() {
		return btnErrorShow;
	}

	public void setBtnErrorShow(String btnErrorShow) {
		this.btnErrorShow = btnErrorShow;
	}

	public String getBtnInfoShow() {
		return btnInfoShow;
	}

	public void setBtnInfoShow(String btnInfoShow) {
		this.btnInfoShow = btnInfoShow;
	}

	public int getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
	}
	
}
