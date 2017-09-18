package br.usp.analitico.export.charts;

import java.awt.geom.Point2D;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.google.gson.Gson;

@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class ChartLine extends Application {

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

		final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
		lineChart.setTitle(chartProperties.getChartTitle());
		addSeries(lineChart);

		Scene scene  = new Scene(lineChart,600,600);
		scene.getStylesheets().add("line-chart.css");
		stage.setScene(scene);
		stage.show();

		int i = 0;
		for (XYChart.Series s : lineChart.getData()) {
			Tooltip t = new Tooltip(s.getName());
			Tooltip.install(s.getNode(), t);
			if(i > 7){
				s.getNode().getStyleClass().remove(s.getNode().getStyleClass().size()-1);
				s.getNode().getStyleClass().add("default-color"+i);
				for(Object n : s.getData()){
					((XYChart.Data)n).getNode().getStyleClass().remove(((XYChart.Data)n).getNode().getStyleClass().size()-1);
					((XYChart.Data)n).getNode().getStyleClass().add("default-color"+i);
				}
			}
			i++;
			s.getNode().setOnMouseEntered(e -> {
				String series = ((String[])s.getNode().getStyleClass().toString().split(" "))[1].split("series")[1];
				s.getNode().getStyleClass().add("black");
				ObservableList<String> classes = lineChart.lookup(".chart-legend-item-symbol.chart-line-symbol.series"+series+".default-color"+series).getStyleClass();
				classes.add("black");
			});
			s.getNode().setOnMouseExited(e -> {
				String series = ((String[])s.getNode().getStyleClass().toString().split(" "))[1].split("series")[1];
				s.getNode().getStyleClass().remove(s.getNode().getStyleClass().size()-1);
				ObservableList<String> classes = lineChart.lookup(".chart-legend-item-symbol.chart-line-symbol.series"+series+".default-color"+series).getStyleClass();
				classes.remove(classes.size()-1);});
		}
		for(Object n : lineChart.lookupAll(".chart-legend-item-symbol").toArray()){
			String series = ((String[])((Node) n).getStyleClass().toString().split(" "))[2].split("series")[1];
			((Node) n).getStyleClass().remove(((Node) n).getStyleClass().size()-1);
			((Node) n).getStyleClass().add("default-color"+series);
		}
	}

	public static void addSeries(LineChart lineChart){

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
			lineChart.getData().add(series);
		}
	}
}
