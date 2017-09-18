package br.usp.analitico.samples;

import java.util.OptionalDouble;
import java.util.Random;
import java.util.stream.Collectors;

import br.usp.analitico.export.charts.FactorChart;
import br.usp.analitico.export.charts.ScatterChart;
import br.usp.analitico.statistics.calc.Statistics;
import br.usp.analitico.statistics.pca.PrincipalComponentAnalysis;

public class RandomSample {

	public static void main(String[] args) throws Exception {
		
		double[][] data = new double[2000][2];
		Random random = new Random(10);
		for (int i = 0; i < data.length; i++) {
			
			double r = random.nextDouble();
			
			//pontos gerados em torno do eixo X
			double x = random.nextDouble();
			double y = 0.1*(r<0.5 ? 1 : -1)*random.nextDouble();
			
			//rotacao dos pontos em 45 graus
			data[i][0] = (Math.sqrt(2)/2.0)*(x+y);
			data[i][1] = (Math.sqrt(2)/2.0)*(x-y);
		}
		
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(Statistics.calculaMatrizCovariancia(data), OptionalDouble.of(0.95));
		
		double[] pc1 = pca.getPrincipalComponents().get(0).getEigenvector();
		double[] pc2 = pca.getPrincipalComponents().get(1).getEigenvector();
		double[][] newdata = new double[data.length][2];
		for(int obs = 0; obs <data.length; obs++){
			double[] observation = data[obs];
			double x = 0;
			double y = 0;
			for(int dim = 0; dim<observation.length; dim++){
				x += observation[dim]*pc1[dim];
				y += observation[dim]*pc2[dim];
			}
			newdata[obs][0] = x;
			newdata[obs][1] = y;
		}
	
		
//		new ScatterChart(newdata, new String[newdata.length]).render();
		new FactorChart(pca.getPrincipalComponents().stream().map(p -> p.getEigenvector()).collect(Collectors.toList()), new String[]{"x","y"}, true, "RandomSample", "PC1", "PC2").render();
	}
}

