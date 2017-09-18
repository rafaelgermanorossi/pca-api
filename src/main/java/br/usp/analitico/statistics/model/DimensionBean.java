package br.usp.analitico.statistics.model;

import br.usp.analitico.export.IExportable;

public class DimensionBean implements IExportable{

	private int position;
	private String label;
	private boolean isNumeric;
	private double currentAverage;
	private double currentVariance;
	private double currentStandardDeviation;
	private double previousAverage;
	private double lastInput;
	private int size;
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public DataTypes getType() {
		return isNumeric ? DataTypes.NUMERIC : DataTypes.CLASS;
	}
	public double getCurrentAverage() {
		return currentAverage;
	}
	public void setCurrentAverage(double currentAverage) {
		this.currentAverage = currentAverage;
	}
	public double getCurrentVariance() {
		return currentVariance;
	}
	public void setCurrentVariance(double currentVariance) {
		this.currentVariance = currentVariance;
	}
	public double getCurrentStandardDeviation() {
		return currentStandardDeviation;
	}
	public void setCurrentStandardDeviation(double currentStandardDeviation) {
		this.currentStandardDeviation = currentStandardDeviation;
	}
	public double getPreviousAverage() {
		return previousAverage;
	}
	public void setPreviousAverage(double previousAverage) {
		this.previousAverage = previousAverage;
	}
	public double getLastInput() {
		return lastInput;
	}
	public void setLastInput(double lastInput) {
		this.lastInput = lastInput;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public boolean isNumeric(){
		return isNumeric;
	}
	public void setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
	
	@Override
	public String toString(){
		return label;
	}
}
