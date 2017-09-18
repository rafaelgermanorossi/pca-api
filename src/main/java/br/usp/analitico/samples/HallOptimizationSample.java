package br.usp.analitico.samples;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import br.usp.analitico.statistics.calc.Statistics;

public class HallOptimizationSample {
	
	private Random ran = new Random(1);
	private double[] means;
	private double[][] covar;
	private double[][] data;
	
	public static void main(String[] args) throws Exception {

		HallOptimizationSample instance = new HallOptimizationSample();
		int dimSize = Integer.parseInt(args[0]);
		int initialRows = Integer.parseInt(args[1]);
		int newRows = Integer.parseInt(args[2]);
		int type = Integer.parseInt(args[3]);
		int N = initialRows;
		
		//Populate initial data
		instance.data = new double[initialRows][dimSize];
		for(int i =0; i<instance.data.length; i++){
			for(int j = 0; j< dimSize; j++){
				instance.data[i][j] = instance.ran.nextDouble();
			}
		}
		
		//Populate new observations
		double[][] newData = new double[newRows][dimSize];
		for(int i =0; i<newData.length; i++){
			for(int j = 0; j< dimSize; j++){
				newData[i][j] = instance.ran.nextDouble();
			}
		}
		
		long executionTime;
		
		if(type==1){
			
		RealMatrix aux = new Array2DRowRealMatrix(instance.data, false);
		double [] variance = new double[dimSize];
		double [] desvios = new double[dimSize];
		for(int j = 0; j< dimSize; j++){
			variance[j] = Statistics.calculaVarianciaVetor(aux.getColumn(j));
			desvios[j] = Math.sqrt(variance[j]);
		}
		
		double[] meansBeforeNorm = new double[dimSize];
		RealMatrix dataM1 = new Array2DRowRealMatrix(instance.data, false);
		for(int j = 0; j< dimSize; j++){
			meansBeforeNorm[j] = Statistics.calculaMediaVetor(dataM1.getColumn(j));
		}
		
		instance.data = Statistics.normalizaColunasMatrizDesvioPadrao(instance.data);
		instance.covar = Statistics.calculaMatrizCovariancia(instance.data);
		
		instance.means = new double[dimSize];
		
		RealMatrix dataM = new Array2DRowRealMatrix(instance.data, false);
		for(int j = 0; j< dimSize; j++){
			instance.means[j] = Statistics.calculaMediaVetor(dataM.getColumn(j));
		}
			
		executionTime = Calendar.getInstance().getTimeInMillis();
			for(int i = 0 ; i < newData.length; i++){
				for(int j = 0; j< dimSize; j++){
					double oldMean =  meansBeforeNorm[j];
					double delta = newData[i][j] - oldMean;
					double newMean = oldMean + delta/(double)(N+1);
					variance[j] = (variance[j]*(double)(N) + (newData[i][j]-newMean)*(newData[i][j]-oldMean))/(double)(N+1);
					instance.means[j] = oldMean/Math.sqrt(variance[j]);
					newData[i][j] = newData[i][j]/Math.sqrt(variance[j]);
					meansBeforeNorm[j] = newMean;
				}
				for(int j = 0; j< dimSize; j++){
					for(int k = j; k < dimSize; k++){
						instance.covar[j][k] = instance.covar[j][k]*desvios[j]*desvios[k]/Math.sqrt(variance[j])/Math.sqrt(variance[k]);
						instance.covar[k][j] = instance.covar[j][k];
					}
				}
				for(int j = 0; j< dimSize; j++){
					desvios[j] = Math.sqrt(variance[j]);
				}
				
				
				instance.covar = Statistics.updateCovarianceMatrix(instance.covar, instance.means, N, newData[i]);
				N++;
			}
		}else{
			double[][] dadosCompostos = new double[instance.data.length + newData.length][dimSize];
			System.arraycopy(instance.data, 0, dadosCompostos, 0, instance.data.length);
			System.arraycopy(newData, 0, dadosCompostos, instance.data.length, newData.length);
			instance.data = dadosCompostos.clone();
			
			executionTime = Calendar.getInstance().getTimeInMillis();
			instance.covar = Statistics.calculaMatrizCovariancia(Statistics.normalizaColunasMatrizDesvioPadrao(instance.data));
		}
		
		executionTime = Calendar.getInstance().getTimeInMillis()-executionTime;

		// N;N';P;Algorithm;Time
		String out = args[1]+";"+args[2]+";"+args[0]+";"+(type==1?"Hall":"Full")+";"+executionTime+"\n";
		
		Files.write(Paths.get("HallOptimization.out"), out.getBytes(), StandardOpenOption.APPEND);

	}

}
