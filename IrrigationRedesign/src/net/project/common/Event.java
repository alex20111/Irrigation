package net.project.common;

import java.util.Date;

/**
 * Class to display events for all workers
 */
public class Event {
	
	private String event 		= "";
	private Date eventDate;
	private boolean eventViewed = false;
	private String evntWorkerId = "";
	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public boolean isEventViewed() {
		return eventViewed;
	}
	public void setEventViewed(boolean eventViewed) {
		this.eventViewed = eventViewed;
	}
	public String getEvntWorkerId() {
		return evntWorkerId;
	}
	public void setEvntWorkerId(String evntWorkerId) {
		this.evntWorkerId = evntWorkerId;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
}
