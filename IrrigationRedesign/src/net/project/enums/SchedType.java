package net.project.enums;

public enum SchedType {
	daily("Daily"), twoDays("Every 2 days"),threeDays("Every 3 days")
	,fourDays("Every 4 days"),week("Every week");
	
	private String desc = "";
	
	private SchedType(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		return this.desc;
	}
}
