package br.usp.analitico.statistics.calc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class Statistics {

	public static double[][] calculaMatrizCovariancia(double[][] data){
		
		if(data.length == 0){
			return data;
		}
		
		double[][] covarianceMatrix = new double[data[0].length][data[0].length];
		double[] means = new double[data[0].length]; 
				
		for(int i = 0; i < data[0].length; i++){
			for(int j = 0; j < data.length; j++){
				means[i] += data[j][i];
			}
			means[i] = means[i]/data.length;
		}
		
		for(int i = 0; i < data[0].length; i++){
			for(int j = i; j < data[0].length; j++){
				for(int d = 0; d < data.length; d++){
					covarianceMatrix[i][j] += (data[d][i] - means[i]) * (data[d][j] - means[j]);
				}
				covarianceMatrix[i][j] = covarianceMatrix[i][j]/(double)(data.length);
				covarianceMatrix[j][i] = covarianceMatrix[i][j];
			}
		}
		
		return covarianceMatrix;
	}
	
	public static double[][] calculaMatrizCorrelacao(double[][] data){
		PearsonsCorrelation pc = new PearsonsCorrelation(data);
		return pc.getCorrelationMatrix().getData();
	}

	public static double[][] centralizaColunasMatriz(double[][] matriz){
		RealMatrix realMatrix = new Array2DRowRealMatrix(matriz, false);
		for(int i = 0; i < realMatrix.getColumnDimension(); i++){
			realMatrix.setColumn(i, centralizaVetor(realMatrix.getColumn(i)));
		}

		return realMatrix.getData();
	}

	public static double[] centralizaVetor(double[] vetor){
		double media = calculaMediaVetor(vetor);

		double[] centralizado = new double[vetor.length];

		for (int i = 0; i < vetor.length; i++) {
			centralizado[i] = vetor[i]-media;
		}

		return centralizado;
	}

	public static double[][] normalizaColunasMatrizDesvioPadrao(double[][] matriz){
		RealMatrix realMatrix = new Array2DRowRealMatrix(matriz, false);
		for(int i = 0; i < realMatrix.getColumnDimension(); i++){
			realMatrix.setColumn(i, normalizaVetorDesvioPadrao(realMatrix.getColumn(i)));
		}

		return realMatrix.getData();
	}

	public static double[] normalizaVetorDesvioPadrao(double[] vetor){

		double desvioPadrao = calculaDesvioPadraoVetor(vetor);

		if(desvioPadrao == 0) {
			return vetor;
		}

		double[] normalizado = new double[vetor.length];
		for (int i = 0; i < vetor.length; i++) {
			normalizado[i] = vetor[i]/desvioPadrao;
		}

		return normalizado;
	}

	public static double calculaMediaVetor(double[] vetor){
		double media = 0;
		for (int i = 0; i < vetor.length; i++) {
			media += vetor[i];
		}
		media = media/vetor.length;
		
		return media;
	}

	public static double calculaVarianciaVetor(double[] vetor){
		double media = calculaMediaVetor(vetor);

		double variancia = 0;
		for (int i = 0; i < vetor.length; i++) {
			variancia += (vetor[i]-media)*(vetor[i]-media);
		}

		variancia = variancia/vetor.length;
		return variancia;
	}
	
	public static double calculaDesvioPadraoVetor(double[] vetor){
		return Math.sqrt(calculaVarianciaVetor(vetor));
	}

	public static double[][] normalizaColunasMatriz(double[][] matriz){
		RealMatrix realMatrix = new Array2DRowRealMatrix(matriz, false);
		for(int i = 0; i < realMatrix.getColumnDimension(); i++){
			realMatrix.setColumn(i, normalizaVetor(realMatrix.getColumn(i)));
		}

		return realMatrix.getData();
	}

	public static List<double[]> normalizaVetoresMaiorNorma(List<double[]> vetores){
		double normaMaxima = vetores.stream().mapToDouble(v -> calculaNormaVetor(v)).max().getAsDouble();
		
		List<double[]> normalizados = ((Stream<double[]>)vetores
											.stream()
											.map(v -> (double[])IntStream.range(0, v.length).mapToDouble(i -> v[i]/normaMaxima).toArray())
											).collect(Collectors.toList());
		
		return normalizados;
	}
	
	public static double[] normalizaVetor(double[] vetor){

		double norma = calculaNormaVetor(vetor);

		if(norma == 0){
			return vetor;
		}

		double[] normalizado = new double[vetor.length];

		for (int i = 0; i < vetor.length; i++) {
			normalizado[i] = vetor[i]/norma;
		}

		return normalizado;
	}

	public static double calculaNormaVetor(double[] vetor){
		double norma = 0;
		for (int i = 0; i < vetor.length; i++) {
			norma += vetor[i]*vetor[i];
		}
		norma = Math.sqrt(norma);

		return norma;
	}

	public static Map<Double,double[]> calculaAutoValoresVetores(double[][] matrix){

		Map<Double,double[]> mapa = new LinkedHashMap<Double,double[]>();

		RealMatrix realMatrix = new BlockRealMatrix(matrix); 
		EigenDecomposition eigen = new EigenDecomposition(realMatrix, 0);

		double[] eigenValues = eigen.getRealEigenvalues();
		for (int i = 0; i < eigenValues.length; i++) {
			mapa.put(eigenValues[i], eigen.getEigenvector(i).toArray());
		}
		
		return mapa;
	}

	public static double obtemMaiorValorAbsolutoVetor(double[] vetor){

		double max = Math.abs(vetor[0]);
		for (int i = 1; i < vetor.length; i++) {
			if(Math.abs(vetor[i]) > max){
				max = vetor[i];
			}
		}

		return max;
	}

	public static double[] normalizaVetorMaiorValorAbsoluto(double[] vetor){

		double max = obtemMaiorValorAbsolutoVetor(vetor);

		double[] normalizado = new double[vetor.length];
		for (int i = 0; i < vetor.length; i++) {
			normalizado[i] = vetor[i]/max;
		}

		return normalizado;
	}

	public static double[][] normalizaColunasMatrizMaiorValor(double[][] matriz){
		RealMatrix realMatrix = new Array2DRowRealMatrix(matriz, false);
		for(int i = 0; i < realMatrix.getColumnDimension(); i++){
			realMatrix.setColumn(i, normalizaVetorMaiorValorAbsoluto(realMatrix.getColumn(i)));
		}

		return realMatrix.getData();
	}
	
	public static double[][] updateCovarianceMatrix(double[][] covarianceMatrixBefore, double[] dataMeanBefore, int currentRowCount, double[] newData){
		RealMatrix _newData = new Array2DRowRealMatrix(new double[][]{newData}, false).transpose();
		RealMatrix _dataMeanBefore = new Array2DRowRealMatrix(new double[][]{dataMeanBefore}, false).transpose();
		RealMatrix _covarianceMatrixBefore = new Array2DRowRealMatrix(covarianceMatrixBefore, false);
		
		double coef1 = (double)currentRowCount/(double)(currentRowCount+1);
		RealMatrix c1 = _covarianceMatrixBefore.scalarMultiply(coef1);
		
		RealMatrix yLinha = _newData.add(_dataMeanBefore.scalarMultiply(-1));
		double coef2 = (double)currentRowCount/(double)Math.pow((currentRowCount+1),2);
		
		RealMatrix c2 = yLinha.scalarMultiply(coef2).multiply(yLinha.transpose());
		
		RealMatrix newCovarianceMatrix = c1.add(c2);
		return newCovarianceMatrix.getData();
	}
	
	public static double[] updateMeans(double[] currentMeans, double[] newInput, int currentRowCount){
		double[] newMeans = new double[currentMeans.length];
		for(int i = 0; i < currentMeans.length; i++){
			newMeans[i] = updateMean(currentMeans[i],newInput[i],currentRowCount);
		}
		return newMeans;
	}
	
	public static double updateMean(double currentMean, double newInput, int currentRowCount){
		double delta = newInput - currentMean;
		double newMean = currentMean + delta/(double)(currentRowCount+1);
		
		return newMean;
	}
	
	public static double[] updateVariances(double[] currentVariances, double[] newInput, double[] oldMeans, double[] newMeans, int currentRowCount){
		double[] newVariances = new double[currentVariances.length];
		for(int i = 0; i < currentVariances.length; i++){
			newVariances[i] = updateVariance(currentVariances[i], newInput[i], oldMeans[i], newMeans[i], currentRowCount);
		}
		return newVariances;
	}
	
	public static double updateVariance(double currentVariance, double newInput, double oldMean, double newMean, int currentRowCount){
		double newVariance = (currentVariance*(double)(currentRowCount) + (newInput-newMean)*(newInput-oldMean))/(double)(currentRowCount+1);
		return newVariance;
	}
	

}
