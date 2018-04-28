package net.project.db.manager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.project.common.Constants;
import net.project.db.entities.User;
import net.project.db.sql.UserSql;
import net.project.exception.ValidationException;
import net.project.security.PassHash;

public class UserManager {

	private UserSql sql;
	
	public UserManager(){
		sql = new UserSql();
	}
	
	public User loginUser(String userName, String password) throws ValidationException, SQLException, ClassNotFoundException{
		
		User user = null;
		
		if (password == null || password.length() == 0){
			throw new ValidationException("Password empty");
		}
		if (userName == null || userName.length() == 0 ){
			throw new ValidationException("userName empty");
		}
		
		//load user
		user = sql.loadUserByUserName(userName);
		
		if (user != null){
			//check if exceeded nbr of tries
			if(user.getNbOfTries() < Constants.NBR_TRIES){
				
				if(PassHash.verifyPassword(password, user.getPassword())){					
					user.setNbOfTries(0);
					user.setLastLogin(new Date());
					sql.updateUser(user);
					return user;
				}else{
					//update user with number of tries
					int tries = user.getNbOfTries();
					user.setNbOfTries(tries + 1);
					sql.updateUser(user);
					throw new ValidationException("Invalid password");
				}
			}
			else{
				throw new ValidationException("User over maximum number of tries");
			}
		}
		return null;		
	}
	/**
	 * Load all users from the database
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public List<User> loadAllUsers() throws SQLException, ClassNotFoundException{
		return sql.loadAllUsers();
	}
	
	public void updateUser(User user) throws SQLException, ClassNotFoundException{
		if (user != null){
			sql.updateUser(user);
		}
	}
	
	public User loadUserById(int id) throws SQLException, ClassNotFoundException{
		return sql.loadUserByid(id);
	}
	public void addUser(User user) throws SQLException, ClassNotFoundException{
		if (user != null){
			sql.addUser(user);
		}
	}
	public void deleteUser(int userId) throws SQLException, ClassNotFoundException{
		sql.deleteUserByUser(userId);
	}
	public User loadUserByUserName(String userName) throws SQLException, ClassNotFoundException{
		return sql.loadUserByUserName(userName);
	}
	
}
