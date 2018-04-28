package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.project.common.Constants;
import net.project.db.entities.Config;

public class ConfigSql {

	public Config addConfig(Config cfg) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);

			int key = con.buildAddQuery(Config.TABLE_NAME)
					.setParameter(Config.EMAIL_USER, cfg.getEmailUserName())
					.setParameter(Config.EMAIL_PASS, cfg.getEmailPassword())	
					.setParameter(Config.WARNING, cfg.isActivateWarning())	
					.setParameter(Config.SERIAL_PORT, cfg.getSerialPort())
					.setParameter(Config.CHECK_WORKER, cfg.getCheckWorkers())
					.setParameter(Config.EMAIL_PROVIDER, cfg.getEmailProvider().name())
					.setParameter(Config.NOTI_BATT, cfg.isNotiBattLow())	
					.setParameter(Config.NOTI_ERRORS, cfg.isNotiErrors())	
					.setParameter(Config.NOTI_RAIN, cfg.isNotiRainNotWatering())	
					.setParameter(Config.NOTI_USER_LOG, cfg.isNotiUserLogin())
					.setParameter(Config.APP_TITLE, cfg.getApplicationTitle())	
					.add();			

			cfg.setConfigId(key);

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return cfg;
	}
	public void updateUser(Config cfg) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			con.buildUpdateQuery(Config.TABLE_NAME)

			.setParameter(Config.EMAIL_USER, cfg.getEmailUserName())
			.setParameter(Config.EMAIL_PASS, cfg.getEmailPassword())
			.setParameter(Config.WARNING, cfg.isActivateWarning())	
			.setParameter(Config.SERIAL_PORT, cfg.getSerialPort())
			.setParameter(Config.CHECK_WORKER, cfg.getCheckWorkers())
			.setParameter(Config.EMAIL_PROVIDER, cfg.getEmailProvider().name())
			.setParameter(Config.NOTI_BATT, cfg.isNotiBattLow())	
			.setParameter(Config.NOTI_ERRORS, cfg.isNotiErrors())	
			.setParameter(Config.NOTI_RAIN, cfg.isNotiRainNotWatering())	
			.setParameter(Config.NOTI_USER_LOG, cfg.isNotiUserLogin())	
			.setParameter(Config.APP_TITLE, cfg.getApplicationTitle())	

			.addUpdWhereClause("WHERE " + Config.CONFIG_ID + " = :cfgId", cfg.getConfigId())
			.update();


		}finally{
			if (con!=null){
				con.close();
			}
		}
	}
	public Config loadConfig() throws SQLException, ClassNotFoundException{

		Config cfg = null;

		DBConnection con = null;
		try{
			con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword, Constants.dbType);


			String query = "SELECT * FROM " + Config.TABLE_NAME;

			ResultSet rs = con.createSelectQuery(query)
					.getSelectResultSet();

			while (rs.next()) {

				cfg = new Config(rs);
			}

		}finally{
			if (con!=null){
				con.close();
			}
		}

		return cfg;
	}
}
