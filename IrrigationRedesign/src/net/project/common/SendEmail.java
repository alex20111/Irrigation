package net.project.common;

import java.util.Date;

import net.project.db.entities.Config;
import net.project.enums.EmailProvider;

import home.crypto.Encryptor;
import home.email.EmailMessage;
import home.email.EmailType;
import home.email.SendMail;

public class SendEmail {

	public static void send(String title, String content, Config cfg) throws Exception{
		EmailMessage email = new EmailMessage();
		email.setFrom("WaterIrrigationProject");
		email.setSubject(title);
		email.setMessageBody(content);
		email.setTo(cfg.getEmailUserName());
		email.setSentDate(new Date());
		
		if (cfg.getEmailProvider() == EmailProvider.google)
		{
			SendMail.sendGoogleMail(cfg.getEmailUserName(), 
					Encryptor.decryptString(cfg.getEmailPassword(), System.getProperty("email_key").toCharArray()), email, EmailType.html);
		}else if (cfg.getEmailProvider() == EmailProvider.yahoo){
			SendMail.sendYahooMail(cfg.getEmailUserName(), 
					Encryptor.decryptString(cfg.getEmailPassword(), System.getProperty("email_key").toCharArray()), email, EmailType.html);
		}
	}
	
	public static void sendTestMail(String userName, String password, EmailProvider provider) throws Exception{
		EmailMessage email = new EmailMessage();
		email.setFrom("project");
		email.setSubject("Test email from project");
		email.setMessageBody("Test e-mail from your friendly fully Automatic project");
		email.setTo(userName);
		email.setSentDate(new Date());
		
		if (provider == EmailProvider.google)
		{
			SendMail.sendGoogleMail(userName,password, email, EmailType.html);
		}
		else if (provider == EmailProvider.yahoo){
			SendMail.sendYahooMail(userName, password, email, EmailType.html);
		}else{
			
		}
	}

}
