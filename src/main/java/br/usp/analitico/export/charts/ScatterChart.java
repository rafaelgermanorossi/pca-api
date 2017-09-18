package br.usp.analitico.export.charts;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class ScatterChart {

	private Map<String, List<Point2D.Double>> series;
	private ChartProperties chartProperties;
	private final static double AXIS_FACTOR = 0.3;
	
	public ScatterChart(double[][] data, String[] classes){
		
		series = new HashMap<String, List<Point2D.Double>>();
		
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMax = Double.MIN_VALUE;
		
		for(int i = 0; i<data.length; i++){

			Point2D.Double p = new Point2D.Double(data[i][0], data[i][1]);
			
			if(data[i][0] < xMin){
				xMin = p.x;
			}
			if(data[i][0] > xMax){
				xMin = p.x;
			}
			if(data[i][1] < yMin){
				yMin = p.y;
			}
			if(data[i][1] > yMax){
				yMax = p.y;
			}
			
			if(!series.containsKey(classes[i])){
				series.put(classes[i], new ArrayList<Point2D.Double>());
			}
			
			series.get(classes[i]).add(p);
		}
		
		chartProperties = new ChartProperties();
		chartProperties.setChartType("Gráfico de Dispersão");
		chartProperties.setChartTitle("Gráfico de Dispersão");
		chartProperties.setxAxisLabel("PC1");
		chartProperties.setxAxisMinValue(xMin - (xMax-xMin)*AXIS_FACTOR);
		chartProperties.setxAxisMaxValue(xMax + (xMax-xMin)*AXIS_FACTOR);
		chartProperties.setyAxisLabel("PC2");
		chartProperties.setyAxisMinValue(yMin - (yMax-yMin)*AXIS_FACTOR);
		chartProperties.setyAxisMaxValue(yMax + (yMax-yMin)*AXIS_FACTOR);
		chartProperties.setSeries(new ChartXYSeries[series.size()]);
		chartProperties.setxAxisAutorange(true);
		chartProperties.setyAxisAutorange(true);

		addSeries();
	}

	private void addSeries() {
		int i = 0;
		for(String className : series.keySet()){
			ChartXYSeries chartSeries = new ChartXYSeries();
			chartSeries.setSeriesLabel(className == null || className.isEmpty() ? "Series "+(i+1) : className);
			chartSeries.setDataPoints(series.get(className).toArray(new Point2D.Double[series.get(className).size()]));
			chartProperties.getSeries()[i++] = chartSeries;
		}
	}

	public void render() throws Exception{
		ChartXY.render(new Gson().toJson(chartProperties));
	}
}
