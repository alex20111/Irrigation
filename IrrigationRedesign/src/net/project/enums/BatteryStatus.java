package net.project.enums;

public enum BatteryStatus {

	full("fa-battery-full"), threeQuarter("fa-battery-three-quarters"), half("fa-battery-half"), oneQuarter("fa-battery-quarter"), empty("fa-battery-empty"), noBatt("Not battery powered");
	
	private String faIcon;
	
	private BatteryStatus(String faIcon){
		this.faIcon = faIcon;
	}
	
	public String getFaIcon(){
		return this.faIcon;
	}
	
}