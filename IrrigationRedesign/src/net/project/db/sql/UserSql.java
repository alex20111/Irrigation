package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.project.common.Constants;
import net.project.db.entities.User;

public class UserSql {	


	public User loadUserByUserName(String userName) throws SQLException, ClassNotFoundException{

		User user = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "SELECT * FROM " + User.TBL_NAME + " where " + User.USER_NAME + " = :userName";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("userName", userName)
					.getSelectResultSet();

			while (rs.next()) {
				user = new User(rs);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return user;
	}
	public User addUser(User userToAdd) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			int key = con.buildAddQuery(User.TBL_NAME)
					.setParameter(User.ACCESS, userToAdd.getAccess().name())
					.setParameter(User.EMAIL, userToAdd.getEmail())
					.setParameter(User.FIRST_NAME, userToAdd.getFirstName())
					.setParameter(User.LAST_LOGIN, userToAdd.getLastLogin())
					.setParameter(User.LAST_NAME, userToAdd.getLastName())
					.setParameter(User.NBR_TRIES, userToAdd.getNbOfTries())
					.setParameter(User.PASSWORD, userToAdd.getPassword())
					.setParameter(User.USER_NAME, userToAdd.getUserName())
					.add();



			userToAdd.setId(key);
		}finally{
			if (con!=null){
				con.close();
			}
		}
		return userToAdd;
	}
	public void updateUser(User userToUpd) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			con.buildUpdateQuery(User.TBL_NAME)

			.setParameter(User.USER_NAME, userToUpd.getUserName())
			.setParameter(User.PASSWORD, userToUpd.getPassword())
			.setParameter(User.FIRST_NAME, userToUpd.getFirstName())
			.setParameter(User.LAST_NAME, userToUpd.getLastName())
			.setParameter(User.EMAIL, userToUpd.getEmail())
			.setParameter(User.LAST_LOGIN, userToUpd.getLastLogin())
			.setParameter(User.NBR_TRIES, userToUpd.getNbOfTries())
			.setParameter(User.ACCESS , userToUpd.getAccess().name())
			.addUpdWhereClause("WHERE " + User.ID + " = :userId", userToUpd.getId())
			.update();


		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public List<User> loadAllUsers() throws SQLException, ClassNotFoundException{

		List<User> users = new ArrayList<User>();  

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT * FROM " + User.TBL_NAME;

			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {
				User user = new User(rs);
				users.add(user);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return users;
	}
	public User loadUserByid(int id) throws SQLException, ClassNotFoundException{

		User user = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT * FROM " + User.TBL_NAME + " where id = :id";

			ResultSet rs = con.createSelectQuery(query)
					.setParameter("id", id)
					.getSelectResultSet();

			while (rs.next()) {
				user = new User(rs);

			}

		}finally{
			if (con!=null){
				con.close();
			}
		}
		return user;
	}
	public void deleteUserByUser(int userId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			String query = "DELETE FROM " + User.TBL_NAME + " where id = :id";

			con.createSelectQuery(query)
			.setParameter("id", userId)
			.delete();

		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
}
