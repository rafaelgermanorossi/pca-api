package br.usp.analitico.database.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import br.usp.analitico.database.config.mysql.MySQLDBManager;
import br.usp.analitico.database.config.sybase.SybaseDBManager;

import com.google.gson.Gson;

public class DBManagerFactory {

	private static String configJson;
	
	public static AbstractDBManager getManager(URI configURI) throws Exception{
		configJson = new String(Files.readAllBytes(Paths.get(configURI)));
		return getManagerInternal();
	}
	
	public static AbstractDBManager getManager() throws Exception{
		try {
//			configJson = new String(Files.readAllBytes(FileSystems.getDefault().getPath(AbstractDBManager.class.getResource("/config.json").toURI().toString())));
			configJson = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/config.json").toURI())));
		} catch (Exception e) {
			Exception ex = new Exception("Could not read the configuration file from the resources folder (resources/config.json).");
			ex.addSuppressed(e);
			throw ex;
		}
		 
		return getManagerInternal();
	}
	private static AbstractDBManager getManagerInternal() throws Exception{
		ConfigBean config = new Gson().fromJson(configJson, ConfigBean.class);
		
		AbstractDBManager manager;
		switch(config.getDbType()){
		case "MySQL":
			manager = new MySQLDBManager(config);
			break;
		case "Sybase":
			manager = new SybaseDBManager(config);
			break;
		default:
			throw new Exception("Database type not supported!");
		}
		return manager;
	}
}
