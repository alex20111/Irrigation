package net.project.enums;

public enum AccessEnum {
	VIEW("View"),REGULAR("Regular"),ADMIN("Administrator");
	
	private String access;
	
	private AccessEnum(String access){
		this.access = access;
	}
	
	public String getAccess(){
		return this.access;
	}
}
