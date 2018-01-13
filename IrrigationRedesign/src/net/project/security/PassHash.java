package net.project.security;

import net.project.common.BCrypt;

public class PassHash {

	public static String hashPassword(String password){
		
		String salt = BCrypt.gensalt();
		String hashed = BCrypt.hashpw(password, salt);
		
		return hashed;
	}
	
	public static boolean verifyPassword(String password, String hashedPassword){
		return BCrypt.checkpw(password, hashedPassword);
	}
	
}
