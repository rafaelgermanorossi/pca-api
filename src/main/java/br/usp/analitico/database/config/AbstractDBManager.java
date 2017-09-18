package br.usp.analitico.database.config;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDBManager {

	protected static final int MYSQL_PRIMARY_KEY_VIOLATION_ERROR_CODE = 1062;

	protected ConfigBean config;
	protected AbstractDBDAO abstractDAO;
	
	public AbstractDBManager(ConfigBean config) throws Exception{
		this.config = config;
	}
	
	public abstract void initMetaDatabase() throws SQLException;
	public abstract void updateQueries() throws SQLException;
	public abstract List<QueryBean> getStoredQueries() throws SQLException;
	public abstract void insertNewQuery(QueryBean query) throws SQLException;
	public abstract void removeQuery(QueryBean query) throws SQLException;
	public abstract void updateExistingQuery(QueryBean query) throws SQLException;
	public abstract void updateIncrementalQueryJava(QueryBean query) throws SQLException;
	public abstract void updateNonIncrementalQueryJava(QueryBean query) throws SQLException;
}
