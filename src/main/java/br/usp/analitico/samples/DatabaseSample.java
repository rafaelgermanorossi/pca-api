package br.usp.analitico.samples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import br.usp.analitico.database.config.AbstractDBManager;
import br.usp.analitico.database.config.ConfigBean;
import br.usp.analitico.database.config.DBManagerFactory;

import com.google.gson.Gson;

public class DatabaseSample {

	private static ConfigBean config;
	private static AbstractDBManager dbManager;
	
	public static void main(String[] args) throws Exception{
		
		loadConfig();
		createFactTable();
		//initial Rows
		insertRows(100);
		
		dbManager = DBManagerFactory.getManager(AbstractDBManager.class.getResource("/DatabaseSample_config.json").toURI());
		dbManager.initMetaDatabase();
		dbManager.updateQueries();
		
		//more rows
		insertRows(100);
		
		dbManager.updateQueries();
	}

	public static void loadConfig() throws IOException, URISyntaxException{
		Gson gson = new Gson();
		String json = new String(Files.readAllBytes(Paths.get(DatabaseSample.class.getResource("/DatabaseSample_config.json").toURI())));
		config = gson.fromJson(json, ConfigBean.class);
	}
	
	private static void insertRows(int N) throws SQLException{
		Connection connection = DriverManager.getConnection(config.getDbURL(), config.getDbUser(), config.getDbPwd());
		connection.setCatalog(config.getDataSchema());
		Random random = new Random(10);

		for(int i = 0; i<N; i++){
			double d1 = random.nextDouble();
			double d2 = random.nextDouble();
			double d3 = random.nextDouble();
			double d4 = random.nextDouble();
			double d5 = random.nextDouble();
			
			String sql = "INSERT INTO MY_FACT (d1,d2,d3,d4,d5) values (?,?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ps.setDouble(1, d1);
			ps.setDouble(2, d2);
			ps.setDouble(3, d3);
			ps.setDouble(4, d4);
			ps.setDouble(5, d5);
			
			ps.execute();
			ps.close();
		}
		connection.close();
	}
	
	private static void createFactTable() throws SQLException{
		Connection connection = DriverManager.getConnection(config.getDbURL(), config.getDbUser(), config.getDbPwd());
		connection.setCatalog(config.getDataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS my_fact ( " +
									"  api_row_id int(11) NOT NULL AUTO_INCREMENT, " +
									"  d1 double DEFAULT NULL, " +
									"  d2 double DEFAULT NULL, " +
									"  d3 double DEFAULT NULL, " +
									"  d4 double DEFAULT NULL, " +
									"  d5 double DEFAULT NULL, " +
									"  PRIMARY KEY (api_row_id) " +
									") ").execute();
		connection.close();
	}
}
