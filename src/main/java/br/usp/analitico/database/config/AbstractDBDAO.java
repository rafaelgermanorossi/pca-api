package br.usp.analitico.database.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import br.usp.analitico.statistics.model.DimensionBean;

import com.google.gson.Gson;

public abstract class AbstractDBDAO {

	protected Gson gson;
	protected ConfigBean config;
	protected Connection connection;
	protected String api_trig_queries;
	protected String api_proc_template;
	protected String api_proc_calc_template;
	protected String api_trig_dependent_table_template;
	
	public AbstractDBDAO(ConfigBean config) throws IOException, URISyntaxException, SQLException{
		this.gson = new Gson();
		this.config = config;
//		final Map<String, String> env = new HashMap<>();
//		String fsURI = AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_trig_queries.sql").toURI().toString().split("!")[0];
//		FileSystem fs;
//		try{
//			fs = FileSystems.getFileSystem(URI.create(fsURI.replace("file:/", "")));
//		}catch(FileSystemNotFoundException e){
//			fs = FileSystems.newFileSystem(URI.create(fsURI), env);
//		}
//		this.api_trig_queries = new String(Files.readAllBytes(fs.getPath("/"+config.getDbType()+"/api_trig_queries.sql")));
//		this.api_proc_template = new String(Files.readAllBytes(fs.getPath("/"+config.getDbType()+"/api_proc_template.sql")));
//		this.api_proc_calc_template = new String(Files.readAllBytes(fs.getPath("/"+config.getDbType()+"/api_proc_calc_template.sql")));
//		this.api_trig_dependent_table_template = new String(Files.readAllBytes(fs.getPath("/"+config.getDbType()+"/api_trig_dependent_table_template.sql")));
		
		this.api_trig_queries = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_trig_queries.sql").toURI())));
		this.api_proc_template = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_proc_template.sql").toURI())));
		this.api_proc_calc_template = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_proc_calc_template.sql").toURI())));
		this.api_trig_dependent_table_template = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_trig_dependent_table_template.sql").toURI())));
		
		getConnection();
	}
	
	public abstract void createMetadataSchema() throws SQLException;
	public abstract void createQueriesTable() throws SQLException;
	public abstract void createQueryDependenciesTable() throws SQLException;
	public abstract void createDimensionsTable() throws SQLException;
	public abstract void createCovariancesTable() throws SQLException;
	public abstract void createApiAuxTable() throws SQLException;
	public abstract void createView(QueryBean query) throws SQLException;
	public abstract void insertDependencies(QueryBean query) throws SQLException;
	public abstract void setCurrentIdentityMaxValue(QueryBean query) throws SQLException;
	public abstract void insertQuery(QueryBean query) throws SQLException;
	public abstract void insertDimensions(QueryBean query, List<DimensionBean> dimensions) throws SQLException;
	public abstract void createTriggerOnDependentTables(QueryBean query) throws SQLException;
	public abstract void createTriggerOnQueriesTable() throws SQLException;
	public abstract void createProcedureForIncrementalUpdate(List<DimensionBean> dimensionList, QueryBean query) throws SQLException;
	public abstract void insertCovariances(QueryBean query, List<DimensionBean> dimensions, double[][] covarTable) throws SQLException;
	public abstract ResultSet getQueryResultSet(String query) throws SQLException;
	public abstract void removeQuery(QueryBean query) throws SQLException;
	public abstract List<QueryBean> getStoredQueries() throws SQLException;
	public abstract void updateCovariances(QueryBean query, List<DimensionBean> dimensions, double[][] covarTable) throws SQLException;
	public abstract double[][] getCovarianceTable(QueryBean query) throws SQLException;
	public abstract List<DimensionBean> getQueryNumericDimensions(QueryBean query) throws SQLException;
	public abstract void updateDimension(QueryBean query, DimensionBean dimension) throws SQLException;
	public abstract void updateQuery(QueryBean query) throws SQLException;
	public abstract void executeQueryUpdateProcedure(QueryBean query) throws SQLException;
	public abstract void removeView(QueryBean query) throws SQLException;
	
	protected void getConnection() throws SQLException{
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.connection = DriverManager.getConnection(config.getDbURL(), config.getDbUser(), config.getDbPwd());
	}
	
	@Override
	protected void finalize() throws Throwable {
		connection.close();
		super.finalize();
	}
}
