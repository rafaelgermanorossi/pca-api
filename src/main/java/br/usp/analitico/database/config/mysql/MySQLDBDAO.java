package br.usp.analitico.database.config.mysql;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import br.usp.analitico.database.config.AbstractDBDAO;
import br.usp.analitico.database.config.AbstractDBManager;
import br.usp.analitico.database.config.ConfigBean;
import br.usp.analitico.database.config.QueryBean;
import br.usp.analitico.database.config.UpdateMethod;
import br.usp.analitico.statistics.model.DimensionBean;
import br.usp.analitico.statistics.model.conversors.IDataSetConversor;

public class MySQLDBDAO extends AbstractDBDAO{

	private String api_proc_execute_proc;
	
	public MySQLDBDAO(ConfigBean config) throws SQLException, IOException, URISyntaxException{
		super(config);
		this.api_proc_execute_proc = new String(Files.readAllBytes(Paths.get(AbstractDBManager.class.getResource("/"+config.getDbType()+"/api_proc_execute_proc.sql").toURI())));
	}

	public void createView(QueryBean query) throws SQLException{
		connection.setCatalog(config.getDataSchema());

		PreparedStatement ps = connection.prepareStatement("create or replace view "+query.getViewName()+" as "+query.getQueryText());
		ps.execute();
		
		connection.setCatalog(config.getMetadataSchema());

		ps = connection.prepareStatement("update queries set viewName = ? where queryId = ?");
		ps.setString(1, query.getViewName());
		ps.setString(2, query.getQueryId());
		
		ps.execute();
		ps.close();
	}

	public void insertDependencies(QueryBean query) throws SQLException{

		connection.setCatalog(config.getMetadataSchema());

		PreparedStatement ps = connection.prepareStatement("INSERT INTO query_dependencies " +
				"SELECT '"+query.getQueryId()+"', tab.TABLE_NAME " +
				"FROM information_schema.`TABLES` AS tab " +
				"INNER JOIN information_schema.VIEWS AS views " +
				"ON views.VIEW_DEFINITION LIKE CONCAT('%',tab.TABLE_NAME,'%') " +
				"where views.TABLE_NAME = '"+query.getViewName()+"'");
		
		ps.execute();
		ps.close();
	}
	
	public void setCurrentIdentityMaxValue(QueryBean query) throws SQLException{
		
		connection.setCatalog(config.getDataSchema());

		PreparedStatement ps = connection.prepareStatement("select convert(varchar(100),max("+query.getIdentityColumn()+"),116) from "+query.getViewName());
		ResultSet rs = ps.executeQuery();
		rs.next();
		query.setCurrentIdentityMaxValue(rs.getString(1));
		rs.close();
		ps.close();
	}
	
	public void insertQuery(QueryBean query) throws SQLException{
		connection.setCatalog(config.getMetadataSchema());

		PreparedStatement ps = connection.prepareStatement("insert into queries (queryId, queryText, identityColumn, identityColumnType, currentIdentityMaxValue, viewName, incremental, updateMethod, currentRowCount) values (?,?,?,?,?,?,?,?,?)");
		ps.setString(1, query.getQueryId());
		ps.setString(2, query.getQueryText());
		ps.setString(3, query.getIdentityColumn());
		ps.setString(4, query.getIdentityColumnType());
		ps.setString(5, query.getCurrentIdentityMaxValue());
		ps.setString(6, query.getViewName());
		ps.setBoolean(7, query.isIncremental());
		ps.setString(8, query.getUpdateMethod());
		ps.setInt(9, query.getCurrentRowCount());
		ps.executeUpdate();
		ps.close();
	}
	
	public void insertDimensions(QueryBean query, List<DimensionBean> dimensions) throws SQLException {
		
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps = connection.prepareStatement("insert into dimensions (queryId, position, label, isNumeric, currentAverage, currentStandardDeviation, currentVariance, previousAverage, lastInput) values (?,?,?,?,?,?,?,?,?)");

		for(DimensionBean dim : dimensions){

			ps.setString(1, query.getQueryId());
			ps.setInt(2, dim.getPosition());
			ps.setString(3, dim.getLabel());
			ps.setBoolean(4, dim.isNumeric());
			ps.setDouble(5, dim.getCurrentAverage());
			ps.setDouble(6, dim.getCurrentStandardDeviation());
			ps.setDouble(7, dim.getCurrentVariance());
			ps.setDouble(8, dim.getPreviousAverage());
			ps.setDouble(9, dim.getLastInput());

			ps.executeUpdate();
		}
		ps.close();
	}
	
	public void createTriggerOnDependentTables(QueryBean query) throws SQLException{
		
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps = connection.prepareStatement("SELECT dependentTableName from query_dependencies where queryId = '"+query.getQueryId()+"'");
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			String table = rs.getString(1);
			connection.setCatalog(config.getDataSchema());
			PreparedStatement ps2 = connection.prepareStatement("drop trigger if exists api_trig_"+table+";");
			ps2.execute();
			
			String sql = api_trig_dependent_table_template
							.replaceAll("@@DEPENDENT_TABLE@@", table)
							.replaceAll("@@META_DB@@", config.getMetadataSchema());
			ps2 = connection.prepareStatement(sql);
			ps2.execute();
			ps2.close();
		}
		rs.close();
		ps.close();
	}
	
	public void createTriggerOnQueriesTable() throws SQLException{
			
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps = connection.prepareStatement("show triggers like 'queries';");
		ResultSet rs = ps.executeQuery();
		if(!rs.next()){
			rs.close();
			
			ps = connection.prepareStatement(api_trig_queries);
			ps.execute();
			ps.close();
		}
	}
	
	public void updateTriggerOnQueriesTable(QueryBean query) throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps = connection.prepareStatement("show triggers like 'queries';");
		ResultSet rs = ps.executeQuery();
		rs.next();
		String stmt = "create trigger api_trig_queries before update on queries for each row \n"+rs.getString("Statement");
		stmt = stmt.replace("@@replace@@", "@@replace@@\r\n\r\n"+api_proc_execute_proc.replaceAll("@@updateProcedure@@","api_proc_"+query.getQueryId()));
		ps = connection.prepareStatement("drop trigger api_trig_queries;");
		ps.execute();
		ps = connection.prepareStatement(stmt);
		ps.execute();
		ps.close();
	}
	
	public void createProcedureForIncrementalUpdate(List<DimensionBean> dimensionList, QueryBean query) throws SQLException{
		connection.setCatalog(config.getMetadataSchema());
		
		String declare = "";
		String dimensions = "";
		String dimensions_variables = "";
		String calc = "";
		
		for(DimensionBean dim : dimensionList){
			int position = dim.getPosition();
			String label = dim.getLabel();
			declare += "DECLARE Pposition"+position+" double; ";
			dimensions += label+",";
			dimensions_variables += "Pposition"+position+",";
			calc += api_proc_calc_template.replace("@@position@@", position+"");
		}
		
		dimensions = dimensions.substring(0,dimensions.length()-1);
		dimensions_variables = dimensions_variables.substring(0,dimensions_variables.length()-1);
		
		String identityType = "";
		if(query.getIdentityColumnType() == "int"){
			identityType = "signed";
		}else if(IDataSetConversor.numericTypes.contains(query.getIdentityColumnType())){
			identityType = "decimal";
		}else{
			identityType = "char";
		}
		
		String api_proc_query = api_proc_template
				.replaceAll("@@queryId@@", query.getQueryId())
				.replaceAll("@@DECLARE@@", declare)
				.replaceAll("@@DIMENSIONS@@", dimensions)
				.replaceAll("@@identityColumn@@", query.getIdentityColumn())
				.replaceAll("@@viewName@@", query.getViewName())
				.replaceAll("@@identityColumnType@@", identityType)
				.replaceAll("@@DIMENSIONS_VARIABLES@@", dimensions_variables)
				.replaceAll("@@DATA_SCHEMA@@", config.getDataSchema())
				.replace("@@CALC@@", calc);
		
		if(query.getUpdateMethodEnum() == UpdateMethod.BATCH_SQL){
			api_proc_query = api_proc_query.replace("-- @@remove@@", "");
		}
		
		PreparedStatement ps = connection.prepareStatement(api_proc_query);
		ps.execute();
		ps.close();
		
		ps = connection.prepareStatement("update queries set updateProcedure = ? where queryId = ?");
		ps.setString(1, "api_proc_"+query.getQueryId());
		ps.setString(2, query.getQueryId());
		ps.executeUpdate();
		ps.close();
	}
	
	public void insertCovariances(QueryBean query, List<DimensionBean> dimensions, double[][] covar) throws SQLException{
	
		connection.setCatalog(config.getMetadataSchema());

		for(int i = 0; i < dimensions.size(); i++){
			for(int j = i ; j < dimensions.size(); j++){
				PreparedStatement ps = connection.prepareStatement("insert into covariances (queryId, d1Pos, d2Pos, value) values (?,?,?,?)");

				ps.setString(1, query.getQueryId());
				ps.setInt(2, dimensions.get(i).getPosition());
				ps.setInt(3, dimensions.get(j).getPosition());
				ps.setDouble(4, covar.length == 0 ? 0 : covar[i][j]);

				ps.executeUpdate();
				ps.close();
			}
		}
	}
	
	public void updateCovariances(QueryBean query, List<DimensionBean> dimensions, double[][] covarTable) throws SQLException{
		
		connection.setCatalog(config.getMetadataSchema());

		for(int i = 0; i < dimensions.size(); i++){
			for(int j = i ; j < dimensions.size(); j++){
				PreparedStatement ps = connection.prepareStatement("update covariances set value = ? where queryId = ? and d1Pos = ? and d2Pos = ?");
				
				ps.setDouble(1, covarTable[i][j]);
				ps.setString(2, query.getQueryId());
				ps.setInt(3, dimensions.get(i).getPosition());
				ps.setInt(4, dimensions.get(j).getPosition());

				ps.executeUpdate();
				ps.close();
			}
		}
	}

	public double[][] getCovarianceTable(QueryBean query) throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps = connection.prepareStatement("select count(*) from dimensions where queryId = ? and isNumeric = 1");
		ps.setString(1, query.getQueryId());
		ResultSet rs = ps.executeQuery();

		rs.next();
		
		int dimSize = rs.getInt(1);
		rs.close();
		
		double[][] covarTable = new double[dimSize][dimSize];
		
		ps = connection.prepareStatement("select value from covariances where queryId = ? order by d1Pos, d2Pos");
		ps.setString(1, query.getQueryId());
		rs = ps.executeQuery();
		
		for(int i = 0; i<dimSize; i++){
			for(int j = i; j<dimSize; j++){
				rs.next();
				covarTable[i][j] = rs.getDouble(1);
				covarTable[j][i] = covarTable[i][j];
			}
		}
		
		return covarTable;
	}
	
	public ResultSet getQueryResultSet(String query) throws SQLException{

		connection.setCatalog(config.getDataSchema());
		ResultSet rs = connection.prepareStatement(query).executeQuery();
		
		return rs;
	}

	public void removeQuery(QueryBean query) throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		
		PreparedStatement ps;
		
		ps = connection.prepareStatement("delete from covariances where queryId = ?");
		ps.setString(1, query.getQueryId());
		ps.executeUpdate();
		
		ps = connection.prepareStatement("delete from dimensions where queryId = ?");
		ps.setString(1, query.getQueryId());
		ps.executeUpdate();
		
		ps = connection.prepareStatement("delete from queries where queryId = ?");
		ps.setString(1, query.getQueryId());
		ps.executeUpdate();
	}

	public List<QueryBean> getStoredQueries() throws SQLException {
		connection.setCatalog(config.getMetadataSchema()); 

		ResultSet rs = connection.prepareStatement("select JSON_OBJECT('queryId',queryId,'queryText',queryText,'currentRowCount',currentRowCount,'identityColumn',identityColumn,'identityColumnType',identityColumnType,'currentIdentityMaxValue',currentIdentityMaxValue,'shouldUpdate',if(shouldUpdate = 1,'true',false),'incremental',if(incremental = 1,'true',false),'shouldStandardize',if(shouldStandardize = 1,'true',false),'viewName',viewName,'updateProcedure',updateProcedure,'updateMethod',updateMethod) query from queries").executeQuery();
		List<QueryBean> dbQueries = new ArrayList<QueryBean>();
		
		while(rs.next()){
			dbQueries.add(gson.fromJson(rs.getString("query"), QueryBean.class));
		}
		rs.close();

		return dbQueries;
	}

	public void createMetadataSchema() throws SQLException {
		connection.prepareStatement("CREATE DATABASE IF NOT EXISTS "+config.getMetadataSchema()).execute();
	}

	public void createQueriesTable() throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS queries (queryId varchar(100) primary key, queryText text, viewName varchar(100) NULL, identityColumn varchar(100) NULL, identityColumnType varchar(100) null, currentIdentityMaxValue varchar(100) null, currentRowCount int default 0, shouldUpdate bit(1) default 0, incremental bit(1) default 0, shouldStandardize bit(1) default 0, updateProcedure varchar(100) null, updateMethod VARCHAR(10))").execute();
	}
	
	public void createQueryDependenciesTable() throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS query_dependencies (queryId varchar(100), dependentTableName varchar(100))").execute();
	}
	
	public void createDimensionsTable() throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS dimensions (queryId varchar(100), position int, label varchar(100), isNumeric bit(1), currentAverage double NULL, currentStandardDeviation double NULL, currentVariance double NULL, previousAverage double NULL, lastInput double null)").execute();
	}
	
	public void createCovariancesTable() throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS covariances (queryId varchar(100), d1Pos int, d2Pos int, value double)").execute();
	}	
	
	public void createApiAuxTable() throws SQLException {
		connection.setCatalog(config.getMetadataSchema());
		connection.prepareStatement("CREATE TABLE IF NOT EXISTS api_aux_table (queryId VARCHAR(100), identityColumn VARCHAR(100))").execute();
	}

	public List<DimensionBean> getQueryNumericDimensions(QueryBean query) throws SQLException{
		
		connection.setCatalog(config.getMetadataSchema());

		BeanListHandler<DimensionBean> h = new BeanListHandler<DimensionBean>(DimensionBean.class); 
		List<DimensionBean> dimensions = new QueryRunner().query(connection,"select * from dimensions where queryId = ? and isNumeric = 1 order by position",h,query.getQueryId());
		
		dimensions.stream().forEach(d ->{d.setNumeric(true);});
		return dimensions;
	}
	
	public void updateDimension(QueryBean query, DimensionBean dimension) throws SQLException{
		connection.setCatalog(config.getMetadataSchema());

		PreparedStatement ps = connection.prepareStatement("UPDATE dimensions set " +
																"currentAverage = ? " +
															  ", label = ? " +
															  ", lastInput = ? " +
															  ", previousAverage = ? " +
															  ", currentStandardDeviation = ? " +
															  ", isNumeric = ? " +
															  ", currentVariance = ? " +
															  "where position = ? and queryId = ?");
		
		
		ps.setDouble(1,dimension.getCurrentAverage());
		ps.setString(2,dimension.getLabel());
		ps.setDouble(3,dimension.getLastInput());
		ps.setDouble(4,dimension.getPreviousAverage());
		ps.setDouble(5,dimension.getCurrentStandardDeviation());
		ps.setBoolean(6, dimension.isNumeric());
		ps.setDouble(7,dimension.getCurrentVariance());
		ps.setInt(8,dimension.getPosition());
		ps.setString(9,query.getQueryId());
		
		ps.executeUpdate();
		ps.close();
	}

	public void updateQuery(QueryBean query) throws SQLException {
		connection.setCatalog(config.getMetadataSchema());

		PreparedStatement ps = connection.prepareStatement("UPDATE queries set " +
																"queryText = ? " +
															  ", viewName = ? " +
															  ", identityColumn = ? " +
															  ", identityColumnType = ? " +
															  ", currentIdentityMaxValue = ? " +
															  ", currentRowCount = ? " +
															  ", shouldUpdate = ? " +
															  ", incremental = ? " +
															  ", shouldStandardize = ? " +
															  ", updateProcedure = ? " +
															  ", updateMethod = ? " +
															  "where queryId = ?");
		
		
		ps.setString(1,query.getQueryText());
		ps.setString(2,query.getViewName());
		ps.setString(3,query.getIdentityColumn());
		ps.setString(4,query.getIdentityColumnType());
		ps.setString(5,query.getCurrentIdentityMaxValue());
		ps.setInt(6,query.getCurrentRowCount());
		ps.setBoolean(7, query.shouldUpdate());
		ps.setBoolean(8,query.isIncremental());
		ps.setBoolean(9,query.shouldStandardize());
		ps.setString(10,query.getUpdateProcedure());
		ps.setString(11,query.getUpdateMethod());
		ps.setString(12,query.getQueryId());
		
		ps.executeUpdate();
		ps.close();
	}

	public void executeQueryUpdateProcedure(QueryBean query) throws SQLException {
		connection.setCatalog(config.getMetadataSchema());

		PreparedStatement ps = connection.prepareStatement("call "+query.getUpdateProcedure()+" (@a,@b)");
		
		ps.execute();
		ps.close();
	}

	public void removeView(QueryBean query) throws SQLException {
		connection.setCatalog(config.getDataSchema());

		PreparedStatement ps = connection.prepareStatement("DROP VIEW "+query.getViewName());
		
		ps.execute();
		ps.close();
	}
	
}
