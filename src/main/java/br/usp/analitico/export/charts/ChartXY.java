package br.usp.analitico.export.charts;

import java.awt.geom.Point2D;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.google.gson.Gson;

@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class ChartXY extends Application {

	private static ChartProperties chartProperties;

	public static void render(String properties) throws Exception{

		if(properties == null || properties.isEmpty()){
			throw new Exception("Properties missing!");
		}

		chartProperties = new Gson().fromJson(properties, ChartProperties.class);
		launch(properties);
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle(chartProperties.getChartType());

		final NumberAxis xAxis;
		if(chartProperties.isxAxisAutorange()){
			xAxis = new NumberAxis();
		}else{
			xAxis = new NumberAxis(chartProperties.getxAxisMinValue(), chartProperties.getxAxisMaxValue(), 0);
		}

		xAxis.setLabel(chartProperties.getxAxisLabel());

		final NumberAxis yAxis;
		if(chartProperties.isyAxisAutorange()){
			yAxis = new NumberAxis();
		}else{
			yAxis = new NumberAxis(chartProperties.getyAxisMinValue(), chartProperties.getyAxisMaxValue(), 0);
		}

		yAxis.setLabel(chartProperties.getyAxisLabel());

		
		final ScatterChart<Number,Number> chart = new ScatterChart<Number,Number>(xAxis,yAxis);
		chart.setTitle(chartProperties.getChartTitle());
		addSeries(chart);

		Scene scene  = new Scene(chart,1000,1000);
		stage.setScene(scene);
		stage.show();
		
		for (XYChart.Series s : chart.getData()) {
			Tooltip t = new Tooltip(s.getName());
			t.setAutoHide(false);
				for(Object n : s.getData()){
					Tooltip.install(((XYChart.Data)n).getNode(), t);
				}
		}
	}

	public static void addSeries(ScatterChart chart){

		for(ChartXYSeries chartSeries : chartProperties.getSeries()){
			Series series = new XYChart.Series();
			series.setName(chartSeries.getSeriesLabel());

			for(int i = 0; i < chartSeries.getDataPoints().length; i++){
				Point2D.Double point = chartSeries.getDataPoints()[i];
				XYChart.Data d = new XYChart.Data(point.x,point.y);

				if(point.x == 0 && point.y == 0){
					d.setNode(new Text(""));
				}
				series.getData().add(d);
			}
			chart.getData().add(series);
		}
	}
}
