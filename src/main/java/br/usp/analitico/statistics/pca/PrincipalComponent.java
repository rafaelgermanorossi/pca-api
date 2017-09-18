package br.usp.analitico.statistics.pca;

import br.usp.analitico.export.IExportable;

public class PrincipalComponent implements IExportable{
	private int rank;
	private String label;
	private double varianceRepresented;
	private double cumulativeVariance;
	private double[] eigenvector;
	private double eigenvalue;
	private boolean isDiscarded;
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getVarianceRepresented() {
		return varianceRepresented;
	}
	public void setVarianceRepresented(double varianceRepresented) {
		this.varianceRepresented = varianceRepresented;
	}
	public double getCumulativeVariance() {
		return cumulativeVariance;
	}
	public void setCumulativeVariance(double cumulativeVariance) {
		this.cumulativeVariance = cumulativeVariance;
	}
	public double[] getEigenvector() {
		return eigenvector;
	}
	public void setEigenvector(double[] eigenvector) {
		this.eigenvector = eigenvector;
	}
	public double getEigenvalue() {
		return eigenvalue;
	}
	public void setEigenvalue(double eigenvalue) {
		this.eigenvalue = eigenvalue;
	}
	public boolean isDiscarded() {
		return isDiscarded;
	}
	public void setDiscarded(boolean isDiscarded) {
		this.isDiscarded = isDiscarded;
	}
}
