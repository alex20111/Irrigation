package net.project.db.manager;

import java.sql.SQLException;

import net.project.db.entities.Config;
import net.project.db.sql.ConfigSql;
import net.project.exception.ValidationException;

public class ConfigManager {

	private ConfigSql sql;
	
	public ConfigManager(){
		sql = new ConfigSql();
	}
	
	public Config loadConfig() throws SQLException, ClassNotFoundException{	
		
		return sql.loadConfig();
	}
	
	public void updateConfig(Config cfg) throws ValidationException, SQLException, ClassNotFoundException{
		
		if (cfg == null){
			throw new ValidationException("Config is null in updateConfig");
		}
		if (cfg.getConfigId() == 0 || cfg.getConfigId() < 0){
			throw new ValidationException("Config id is not populated : " + cfg);
		}
		
		sql.updateUser(cfg);
		
	}
}
