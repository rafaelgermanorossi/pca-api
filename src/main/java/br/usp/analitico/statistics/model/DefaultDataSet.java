package br.usp.analitico.statistics.model;

import java.util.List;

public class DefaultDataSet extends AbstractDataSet {

	@Override
	public void setDimensions(List<DimensionBean> dimensions) {
		this.dimensions = dimensions;
	}
	
	@Override
	public void setNumericDimensions(List<DimensionBean> dimensions) {
		this.numericDimensions = dimensions;
	}

	@Override
	public void setClassDimensions(List<DimensionBean> dimensions) {
		this.classDimensions = dimensions;
	}

	@Override
	public void setRawData(Object[][] rawData) {
		this.rawData = rawData;
	}

	@Override
	public void setNumericData(double[][] numericData) {
		this.numericData = numericData;
	}

	@Override
	public void setClassData(String[][] classData) {
		this.classData = classData;
	}

	@Override
	public void setCurrentIdentityMaxValue(String value) {
		this.currentIdentityMaxValue = value;
	}
}
