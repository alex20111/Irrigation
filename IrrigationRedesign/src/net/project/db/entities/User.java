package net.project.db.entities;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import net.project.enums.AccessEnum;

public class User {

	//Table name
	public static String TBL_NAME 	= "usertbl";
	
	//columns for user
	public static final String ID 			= "id";
	public static final String USER_NAME 	= "username";
	public static final String PASSWORD 	= "password";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME 	= "lastname";
	public static final String EMAIL 		= "email";
	public static final String LAST_LOGIN = "lastlogin";
	public static final String NBR_TRIES 	= "nbrtries";
	public static final String ACCESS 	= "access";	
	
	//DB fields
	private int    id			= -1;
	private String userName 	= "";
	private String password 	= "";
	private String firstName	= "";
	private String lastName 	= "";
	private String email 		= "";
	private Date lastLogin;
	private int nbOfTries 		= 0;
	private AccessEnum access;
	
	
	public User(){}
	public User(ResultSet rs) throws SQLException{

		ResultSetMetaData rsmd = rs.getMetaData();
		for(int i = 1 ; i <= rsmd.getColumnCount()  ; i++){

			if ("id".equalsIgnoreCase(rsmd.getColumnName(i))){
				id 		= rs.getInt("id");	
			}else if (USER_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				userName 	= rs.getString(USER_NAME);
			}else if (PASSWORD.equalsIgnoreCase(rsmd.getColumnName(i))){
				password 	= rs.getString(PASSWORD);
			}else if (FIRST_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				firstName	= rs.getString(FIRST_NAME);
			}else if (LAST_NAME.equalsIgnoreCase(rsmd.getColumnName(i))){
				lastName 	= rs.getString(LAST_NAME);
			}else if (EMAIL.equalsIgnoreCase(rsmd.getColumnName(i))){
				email 		= rs.getString(EMAIL);
			}else if (LAST_LOGIN.equalsIgnoreCase(rsmd.getColumnName(i))){
				lastLogin 	= rs.getTimestamp(LAST_LOGIN);
			}else if (NBR_TRIES.equalsIgnoreCase(rsmd.getColumnName(i))){
				nbOfTries 	= rs.getInt(NBR_TRIES);
			}else if (ACCESS.equalsIgnoreCase(rsmd.getColumnName(i))){
				access  	= AccessEnum.valueOf(rs.getString(ACCESS));
			}		
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	public int getNbOfTries() {
		return nbOfTries;
	}
	public void setNbOfTries(int nbOfTries) {
		this.nbOfTries = nbOfTries;
	}
	public AccessEnum getAccess() {
		return access;
	}
	public void setAccess(AccessEnum access) {
		this.access = access;
	}
	public static String updateUser(){
		StringBuilder update = new StringBuilder();
		update.append("UPDATE " + TBL_NAME + " ");
		update.append("SET " + USER_NAME + " = ? ");
		update.append("," + PASSWORD + " = ? ");
		update.append("," + FIRST_NAME + " = ? ");
		update.append("," + LAST_NAME + " = ? ");
		update.append("," + EMAIL + " = ? ");
		update.append("," + LAST_LOGIN + " = ? ");
		update.append("," + NBR_TRIES + " = ? ");
		update.append("," + ACCESS + " = ? ");
		update.append("WHERE id = ?");
		
		return update.toString();
	}
	public static String addUser(){
		StringBuilder add = new StringBuilder();
		add.append("INSERT INTO " + TBL_NAME);
		add.append( " ( " + USER_NAME + "," + PASSWORD + "," + FIRST_NAME + "," + LAST_NAME + "," + EMAIL + "," + LAST_LOGIN + "," + NBR_TRIES + "," + ACCESS +" ) " );
		add.append(" VALUES(?,?,?,?,?,?,?,?)");
		
		return add.toString();
	}
	public static String createTable(){
		StringBuilder create = new StringBuilder();
		create.append("CREATE TABLE " + TBL_NAME + " (");
		create.append("ID INT PRIMARY KEY auto_increment");
		create.append(", " + USER_NAME + " VARCHAR(200)" );
		create.append(", " + PASSWORD + " VARCHAR(200)" ); 
		create.append(", " + FIRST_NAME + " VARCHAR(200)" ); 
		create.append(", " + LAST_NAME + " VARCHAR(200)" ); 
		create.append(", " + EMAIL + " VARCHAR(200)" ); 
		create.append(", " + LAST_LOGIN + " TIMESTAMP" ); 
		create.append(", " + NBR_TRIES + " TINYINT" ); 
		create.append(", " + ACCESS + " VARCHAR(200)" ); 	
		create.append(")");
		
		return create.toString();
	}
	
	/**
	 * Determin if the user can modifiy the controls (Turn on or off)
	 * @return
	 */
	public boolean canModify(){
		boolean canMod = false;
		
		if (getAccess() == AccessEnum.REGULAR || getAccess() == AccessEnum.ADMIN){
			canMod = true;
		}
		return canMod;
	}
	
	public boolean isAdministrator(){
		if (getAccess() != null && getAccess() == AccessEnum.ADMIN){
			return true;
		}

		return false;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [id=");
		builder.append(id);
		builder.append(", userName=");
		builder.append(userName);
		builder.append(", password=");
		builder.append(password);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", email=");
		builder.append(email);
		builder.append(", lastLogin=");
		builder.append(lastLogin);
		builder.append(", nbOfTries=");
		builder.append(nbOfTries);
		builder.append(", access=");
		builder.append(access);
		builder.append("]");
		return builder.toString();
	}
}
