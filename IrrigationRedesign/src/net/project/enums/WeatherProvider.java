package net.project.enums;

public enum WeatherProvider {
	ENVCan("Environment Canada"), WTHUndergrnd("Weather Underground"), noSelected("Please Select");
	
	
	private String name;
	
	private WeatherProvider(String pName){
		this.name = pName;
	}
	
	public String getProviderName(){
		return this.name;
	}
}
