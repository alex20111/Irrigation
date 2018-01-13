package net.project.db.sql;

import home.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.project.common.Constants;
import net.project.db.entities.Config;

public class ConfigSql {

	public Config addConfig(Config cfg) throws SQLException{

		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

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

		con.close();

		cfg.setConfigId(key);

		return cfg;
	}
	public void updateUser(Config cfg) throws SQLException{

		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

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


		con.close();
	}
	public Config loadConfig() throws SQLException{

		Config cfg = null;
		

		DBConnection con = new DBConnection(Constants.url, Constants.dbUser, Constants.dbPassword);

		String query = "SELECT * FROM " + Config.TABLE_NAME;

		ResultSet rs = con.createQuery(query)
				.getSelectResultSet();

		while (rs.next()) {
			
			cfg = new Config(rs);
		}

		con.close();

		return cfg;
	}
}
