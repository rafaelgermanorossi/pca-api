package br.usp.analitico.export.charts;


public class ChartProperties {

	private String chartType;
	private String chartTitle;
	
	private String xAxisLabel;
	private String yAxisLabel;
	
	private boolean xAxisAutorange;
	private double xAxisMinValue;
	private double xAxisMaxValue;
	
	private boolean yAxisAutorange;
	private double yAxisMinValue;
	private double yAxisMaxValue;
	
	private ChartXYSeries[] series;

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public String getChartTitle() {
		return chartTitle;
	}

	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	public String getxAxisLabel() {
		return xAxisLabel;
	}

	public void setxAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}

	public String getyAxisLabel() {
		return yAxisLabel;
	}

	public void setyAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}

	public boolean isxAxisAutorange() {
		return xAxisAutorange;
	}

	public void setxAxisAutorange(boolean xAxisAutorange) {
		this.xAxisAutorange = xAxisAutorange;
	}

	public double getxAxisMinValue() {
		return xAxisMinValue;
	}

	public void setxAxisMinValue(double xAxisMinValue) {
		this.xAxisMinValue = xAxisMinValue;
	}

	public double getxAxisMaxValue() {
		return xAxisMaxValue;
	}

	public void setxAxisMaxValue(double xAxisMaxValue) {
		this.xAxisMaxValue = xAxisMaxValue;
	}

	public boolean isyAxisAutorange() {
		return yAxisAutorange;
	}

	public void setyAxisAutorange(boolean yAxisAutorange) {
		this.yAxisAutorange = yAxisAutorange;
	}

	public double getyAxisMinValue() {
		return yAxisMinValue;
	}

	public void setyAxisMinValue(double yAxisMinValue) {
		this.yAxisMinValue = yAxisMinValue;
	}

	public double getyAxisMaxValue() {
		return yAxisMaxValue;
	}

	public void setyAxisMaxValue(double yAxisMaxValue) {
		this.yAxisMaxValue = yAxisMaxValue;
	}

	public ChartXYSeries[] getSeries() {
		return series;
	}

	public void setSeries(ChartXYSeries[] series) {
		this.series = series;
	}

}