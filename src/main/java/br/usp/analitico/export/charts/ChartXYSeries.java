package br.usp.analitico.export.charts;

import java.awt.geom.Point2D;

public class ChartXYSeries {

	private String seriesLabel;
	private String[] dataLabels;
	private Point2D.Double[] dataPoints;

	public String getSeriesLabel() {
		return seriesLabel;
	}
	public void setSeriesLabel(String seriesLabel) {
		this.seriesLabel = seriesLabel;
	}
	public String[] getDataLabels() {
		return dataLabels;
	}
	public void setDataLabels(String[] dataLabels) {
		this.dataLabels = dataLabels;
	}
	public Point2D.Double[] getDataPoints() {
		return dataPoints;
	}
	public void setDataPoints(Point2D.Double[] dataPoints) {
		this.dataPoints = dataPoints;
	}
}
