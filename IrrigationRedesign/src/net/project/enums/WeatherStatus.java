package net.project.enums;

public enum WeatherStatus {
	sunny("Sunny"),partlyCloudy("Partly Cloudy"), cloudy("Cloudy"), lightDrizzle("Light Drizzle"), lightRain("Light Rain"),raining("Raining"),NA("N/A");
	
	private String status;
	
	private WeatherStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
}
