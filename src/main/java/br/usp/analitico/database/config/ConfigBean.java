package br.usp.analitico.database.config;

import br.usp.analitico.export.IExportable;

public class ConfigBean implements IExportable{

	private String dbType;
	private String dbUser;
	private String dbPwd;
	private String dbURL;
	private String metadataSchema;
	private String dataSchema;
	private QueryBean[] queries;

	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getDbUser() {
		return dbUser;
	}
	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}
	public String getDbPwd() {
		return dbPwd;
	}
	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}
	public String getDbURL() {
		return dbURL;
	}
	public void setDbURL(String dbURL) {
		this.dbURL = dbURL;
	}
	public String getMetadataSchema() {
		return metadataSchema;
	}
	public void setMetadataSchema(String metadataSchema) {
		this.metadataSchema = metadataSchema;
	}
	public String getDataSchema() {
		return dataSchema;
	}
	public void setDataSchema(String dataSchema) {
		this.dataSchema = dataSchema;
	}
	public QueryBean[] getQueries() {
		return queries;
	}
	public void setQueries(QueryBean[] queries) {
		this.queries = queries;
	}
}
