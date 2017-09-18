package br.usp.analitico.statistics.model;

import java.util.List;

import br.usp.analitico.export.IExportable;


public abstract class AbstractDataSet implements IExportable{

	public int observationsSize;

	protected List<DimensionBean> dimensions;
	protected List<DimensionBean> numericDimensions;
	protected List<DimensionBean> classDimensions;
	
	protected Object[][] rawData;
	protected double[][] numericData;
	protected String[][] classData;
	
	protected String currentIdentityMaxValue;

	public int getDimensionsSize() {
		return dimensions.size();
	}

	public int getObservationsSize() {
		return observationsSize;
	}

	public int getNumericDimensionsSize() {
		return numericDimensions.size();
	}

	public int getClassDimensionsSize() {
		return classDimensions.size();
	}

	public List<DimensionBean> getDimensions() {
		return dimensions;
	}
	
	public List<DimensionBean> getNumericDimensions() {
		return numericDimensions;
	}
	
	public List<DimensionBean> getClassDimensions() {
		return classDimensions;
	}

	public Object[][] getDataset() {
		return rawData;
	}

	public double[][] getNumericData() {
		return numericData;
	}

	public String[][] getClassData() {
		return classData;
	}
	
	public String getCurrentIdentityMaxValue(){
		return currentIdentityMaxValue;
	}

	public abstract void setDimensions(List<DimensionBean> dimensions);
	
	public abstract void setNumericDimensions(List<DimensionBean> dimensions);
	
	public abstract void setClassDimensions(List<DimensionBean> dimensions);

	public abstract void setRawData(Object[][] rawData);

	public abstract void setNumericData(double[][] numericData);

	public abstract void setClassData(String[][] classData);
	
	public abstract void setCurrentIdentityMaxValue(String value);
}