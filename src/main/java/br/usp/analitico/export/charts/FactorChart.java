package br.usp.analitico.export.charts;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.usp.analitico.statistics.calc.Statistics;

import com.google.gson.Gson;

public class FactorChart {

	private List<double[]> componentes;
	private String[] labels;
	private ChartProperties chartProperties;
	private boolean normalizaFatores;
	
	public FactorChart(List<double[]> componentes, String[] labels, boolean normalizaFatores, String title, String xAxisLabel, String yAxisLabel){
		
		if(componentes == null || componentes.size() == 0){
			
		}
		
		this.componentes = componentes;		
		this.labels = labels;
		this.normalizaFatores = normalizaFatores;
		
		chartProperties = new ChartProperties();
		chartProperties.setSeries(new ChartXYSeries[componentes.get(0).length]);
		chartProperties.setChartType("Gráfico de Fatores");
		chartProperties.setChartTitle(title);
		chartProperties.setxAxisLabel(xAxisLabel);
		chartProperties.setxAxisMinValue(-1);
		chartProperties.setxAxisMaxValue(1);
		chartProperties.setyAxisLabel(yAxisLabel);
		chartProperties.setyAxisMinValue(-1);
		chartProperties.setyAxisMaxValue(1);
		addSeries();
	}

	private void addSeries() {

		Point2D.Double origin = new Point2D.Double(0, 0);

		List<double[]> fatores = IntStream
									.range(0, componentes.get(0).length)
									.mapToObj(i -> new double[]{componentes.get(0)[i],componentes.get(1)[i]})
									.collect(Collectors.toList());
		
		if(normalizaFatores){
			fatores = Statistics.normalizaVetoresMaiorNorma(fatores);
		}
		for (int i = 0; i < fatores.size(); i++) {
			double[] factor = fatores.get(i);
			Point2D.Double point = new Point2D.Double(factor[0], factor[1]);

			ChartXYSeries series = new ChartXYSeries();
			series.setSeriesLabel(labels[i]);
			series.setDataLabels(new String[]{"",labels[i]});
			series.setDataPoints(new Point2D.Double[]{origin, point});
			
			chartProperties.getSeries()[i] = series;
		}
	}

	public void render() throws Exception{
		ChartLine.render(new Gson().toJson(chartProperties));
	}
}
