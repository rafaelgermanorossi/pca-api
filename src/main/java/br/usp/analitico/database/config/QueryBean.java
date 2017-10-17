package br.usp.analitico.database.config;

import br.usp.analitico.export.IExportable;

public class QueryBean implements IExportable{

	private boolean incremental;
	private String queryId;
	private String queryText;
	private int currentRowCount;
	private String updateMethod;
	private String identityColumn;
	private String identityColumnType;
	private String currentIdentityMaxValue;
	private String viewName;
	private String updateProcedure;
	private boolean shouldUpdate;
	private boolean shouldStandardize;
	
	public boolean isIncremental() {
		return incremental;
	}

	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}
	
	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	public int getCurrentRowCount() {
		return currentRowCount;
	}

	public void setCurrentRowCount(int currentRowCount) {
		this.currentRowCount = currentRowCount;
	}

	public UpdateMethod getUpdateMethodEnum() {
		switch(updateMethod){
		case "trigger":
			return UpdateMethod.TRIGGER;
		case "batch_sql":
			return UpdateMethod.BATCH_SQL;
		default:
			return UpdateMethod.BATCH_JAVA;
		}
	}
	
	public String getUpdateMethod() {
		return updateMethod;
	}

	public void setUpdateMethod(String updateMethod) {
		this.updateMethod = updateMethod;
	}

	public String getIdentityColumn() {
		return identityColumn;
	}

	public void setIdentityColumn(String identityColumn) {
		this.identityColumn = identityColumn;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getIdentityColumnType() {
		return identityColumnType;
	}

	public void setIdentityColumnType(String identityColumnType) {
		this.identityColumnType = identityColumnType;
	}

	public String getCurrentIdentityMaxValue() {
		return currentIdentityMaxValue;
	}

	public void setCurrentIdentityMaxValue(String currentIdentityMaxValue) {
		this.currentIdentityMaxValue = currentIdentityMaxValue;
	}

	public String getUpdateProcedure() {
		return updateProcedure;
	}

	public void setUpdateProcedure(String updateProcedure) {
		this.updateProcedure = updateProcedure;
	}

	public boolean shouldUpdate() {
		return shouldUpdate;
	}

	public void setShouldUpdate(boolean shouldUpdate) {
		this.shouldUpdate = shouldUpdate;
	}

	public boolean shouldStandardize() {
		return shouldStandardize;
	}

	public void setShouldStandardize(boolean shouldStandardize) {
		this.shouldStandardize = shouldStandardize;
	}

	@Override
	public String toString(){
		return queryId + " : " + queryText;
	}
	
	@Override
	public boolean equals(Object query){
		if(query instanceof QueryBean){
			return queryId.equals(((QueryBean)query).getQueryId()) && queryText.equals(((QueryBean)query).getQueryText());
		}
		
		return false;
	}
}
