package net.project.enums;

public enum EmailProvider {

	google("Google"), yahoo("Yahoo"), noSelected("Please select");
	
	private String emailClient;
	
	private EmailProvider(String client){
		this.emailClient = client;
	}
	
	public String getEmailClient(){
		return this.emailClient;
	}
}
