package br.usp.analitico.database.config.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import br.usp.analitico.database.config.AbstractDBManager;
import br.usp.analitico.database.config.ConfigBean;
import br.usp.analitico.database.config.QueryBean;
import br.usp.analitico.database.config.UpdateMethod;
import br.usp.analitico.statistics.calc.Statistics;
import br.usp.analitico.statistics.model.AbstractDataSet;
import br.usp.analitico.statistics.model.DimensionBean;
import br.usp.analitico.statistics.model.conversors.IDataSetConversor;
import br.usp.analitico.statistics.model.conversors.ResultSetDataSetConversor;

public class MySQLDBManager extends AbstractDBManager {
		
	private MySQLDBDAO dao;
	
	public MySQLDBManager(ConfigBean config) throws Exception{
		super(config);
		dao = new MySQLDBDAO(config);
	}
	
	public void initMetaDatabase() throws SQLException {

		try {
			dao.createMetadataSchema();
		} catch (SQLException e) {
			throw new SQLException("Could not create metadata DB: "+ e.getMessage());
		}

		try {
			dao.createQueriesTable();
			dao.createTriggerOnQueriesTable();
		} catch (SQLException e) {
			throw new SQLException("Could not create queries table: "+ e.getMessage());
		}
		
		try {
			dao.createQueryDependenciesTable();
		} catch (SQLException e) {
			throw new SQLException("Could not create queries table: "+ e.getMessage());
		}

		try {
			dao.createDimensionsTable();
		} catch (SQLException e) {
			throw new SQLException("Could not create dimensions table: "+ e.getMessage());
		}

		try {
			dao.createCovariancesTable();
		} catch (SQLException e) {
			throw new SQLException("Could not create covariances table: "+ e.getMessage());
		}

		try {
			dao.createApiAuxTable();
		} catch (SQLException e) {
			throw new SQLException("Could not create api_aux_table table: "+ e.getMessage());
		}
	}

	public void updateQueries() throws SQLException {
		List<QueryBean> dbQueries = getStoredQueries();
		for(QueryBean query : config.getQueries()){
			if(!dbQueries.contains(query)){
				insertNewQuery(query);
			}else{
				updateExistingQuery(dbQueries.get(dbQueries.indexOf(query)));
			}
		}
	}

	public List<QueryBean> getStoredQueries() throws SQLException {
		return dao.getStoredQueries();
	}

	public void insertNewQuery(QueryBean query) throws SQLException {
		
		ResultSet rs = dao.getQueryResultSet(query.getQueryText());
		AbstractDataSet data = new ResultSetDataSetConversor(rs, query.getIdentityColumn()).convert();
		query.setCurrentRowCount(data.getObservationsSize());
		query.setCurrentIdentityMaxValue(data.getCurrentIdentityMaxValue() == null ? ""+Integer.MIN_VALUE : data.getCurrentIdentityMaxValue());
		query.setViewName("api_v_"+query.getQueryId());
		
		dao.createView(query);
		dao.insertDependencies(query);
		dao.createTriggerOnDependentTables(query);

		if(!query.isIncremental()){
			dao.removeView(query);
			query.setViewName(null);
		}
		
		dao.insertQuery(query);
		dao.insertDimensions(query, data.getDimensions());
		dao.insertCovariances(query, data.getNumericDimensions(), Statistics.calculaMatrizCovariancia(data.getNumericData()));

		if(query.isIncremental()){
			dao.createProcedureForIncrementalUpdate(data.getNumericDimensions(), query);
			if(query.getUpdateMethodEnum() == UpdateMethod.TRIGGER){
				dao.updateTriggerOnQueriesTable(query);
			}
		}
	}

	public void removeQuery(QueryBean query) throws SQLException {
		dao.removeQuery(query);
	}

	public void updateExistingQuery(QueryBean query) throws SQLException {
		
		if(query.shouldUpdate()){
			if(query.isIncremental()){
				if(query.getUpdateMethodEnum() == UpdateMethod.BATCH_SQL){
					dao.executeQueryUpdateProcedure(query);
				}else if(query.getUpdateMethodEnum() == UpdateMethod.BATCH_JAVA){
					updateIncrementalQueryJava(query);					
				}
			}else{
				updateNonIncrementalQueryJava(query);
			}
		}
	}
	
	public void updateNonIncrementalQueryJava(QueryBean query) throws SQLException {
		ResultSet rs = dao.getQueryResultSet(query.getQueryText());
		AbstractDataSet data = new ResultSetDataSetConversor(rs, query.getIdentityColumn()).convert();
		query.setCurrentRowCount(data.getObservationsSize());
		query.setShouldUpdate(false);
		
		dao.updateQuery(query);
		for(DimensionBean dimension : data.getDimensions()){
			dao.updateDimension(query, dimension);
		}
		dao.updateCovariances(query, data.getNumericDimensions(), Statistics.calculaMatrizCovariancia(data.getNumericData()));
	}

	public void updateIncrementalQueryJava(QueryBean query) throws SQLException{
		double[][] covarTable = dao.getCovarianceTable(query);
		
		List<DimensionBean> dimensions = dao.getQueryNumericDimensions(query);
		double[] newMeans = dimensions.stream().mapToDouble(d -> d.getCurrentAverage()).toArray();
		double[] variances = dimensions.stream().mapToDouble(d -> d.getCurrentVariance()).toArray();
		
		String identityType = "";
		if(query.getIdentityColumnType() == "int"){
			identityType = "signed";
		}else if(IDataSetConversor.numericTypes.contains(query.getIdentityColumnType())){
			identityType = "decimal";
		}else{
			identityType = "char";
		}
		
		String sql = "select * from "+query.getViewName()+" where "+query.getIdentityColumn()+
				" > cast("+query.getCurrentIdentityMaxValue()+" as "+identityType+") order by "+query.getIdentityColumn();
		ResultSet rs = dao.getQueryResultSet(sql);
		
		AbstractDataSet newData = new ResultSetDataSetConversor(rs,query.getIdentityColumn()).convert(); 
		
		int N = query.getCurrentRowCount();
		double[] oldMeans = new double[newMeans.length];
		double[] lastInput = new double[newMeans.length];
		for(double[] input : newData.getNumericData()){
			lastInput = input.clone();
			oldMeans = newMeans.clone();
			covarTable = Statistics.updateCovarianceMatrix(covarTable, oldMeans, N, input);
			newMeans = Statistics.updateMeans(oldMeans, input, N);
			variances = Statistics.updateVariances(variances, input, oldMeans, newMeans, N);
			N++;
		}
		
		query.setCurrentRowCount(N);
		query.setShouldUpdate(false);
		query.setCurrentIdentityMaxValue(newData.getCurrentIdentityMaxValue());
		
		dao.updateQuery(query);
		
		for(int i = 0; i < dimensions.size(); i++){
			DimensionBean dim = dimensions.get(i);
			dim.setCurrentAverage(newMeans[i]);
			dim.setPreviousAverage(oldMeans[i]);
			dim.setCurrentVariance(variances[i]);
			dim.setCurrentStandardDeviation(Math.sqrt(variances[i]));
			dim.setLastInput(lastInput[i]);
			dim.setSize(N);
			
			dao.updateDimension(query, dim);
		}
		
		dao.updateCovariances(query, dimensions, covarTable);
	}
}
