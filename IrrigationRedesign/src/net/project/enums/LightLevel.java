package net.project.enums;

public enum LightLevel {

	Sunny("Bright"), LowLight("Low Light"),MediumLight("Medium light"), Dark("Dark"), NA("N/A");
	
	private String status;
	
	private LightLevel(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
	
}
