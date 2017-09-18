package br.usp.analitico.samples;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import br.usp.analitico.export.charts.FactorChart;
import br.usp.analitico.statistics.model.AbstractDataSet;
import br.usp.analitico.statistics.model.conversors.CSVDataSetConversor;
import br.usp.analitico.statistics.pca.PrincipalComponentAnalysis;

public class CSVSample {

	public static void main(String[] args) throws Exception {
		
		AbstractDataSet data = new CSVDataSetConversor(CSVSample.class.getResource("/CSVSample.csv").getPath(), ';').convert();
		
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(data, OptionalDouble.empty());

		List<double[]> pcs = pca.getPrincipalComponents().stream().map(p -> p.getEigenvector()).collect(Collectors.toList());
		FactorChart grafico = new FactorChart(pcs, data.getNumericDimensions().stream().map(d -> d.getLabel()).collect(Collectors.toList()).toArray(new String[data.getNumericDimensionsSize()]), true, "CSVSample", "PC1", "PC2");
		grafico.render();
	}

}
