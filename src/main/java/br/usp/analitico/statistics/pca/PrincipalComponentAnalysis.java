package br.usp.analitico.statistics.pca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import br.usp.analitico.export.IExportable;
import br.usp.analitico.statistics.calc.Statistics;
import br.usp.analitico.statistics.model.AbstractDataSet;

public class PrincipalComponentAnalysis implements IExportable{

	public static final double DEFAULT_VARIANCE_THRESHOLD = 0.95;
	public static final boolean DEFAULT_SHOULD_NORMALIZE_MATRIX = true;

	private double varianceThreshold;
	private boolean shouldNormalizeMatrix = DEFAULT_SHOULD_NORMALIZE_MATRIX;
	
	private List<PrincipalComponent> pcs = new ArrayList<PrincipalComponent>();
	

	public PrincipalComponentAnalysis(AbstractDataSet dataset, OptionalDouble varianceThreshold, Optional<Boolean> shouldNormalizeMatrix){
		this.varianceThreshold = varianceThreshold.orElse(DEFAULT_VARIANCE_THRESHOLD);
		this.shouldNormalizeMatrix = shouldNormalizeMatrix.orElse(DEFAULT_SHOULD_NORMALIZE_MATRIX);
		calculatePCA(dataset);
	}
	
	public PrincipalComponentAnalysis(double[][] covarCorrelMatrix, OptionalDouble varianceThreshold, Optional<Boolean> shouldNormalizeMatrix){
		this.varianceThreshold = varianceThreshold.orElse(DEFAULT_VARIANCE_THRESHOLD);
		this.shouldNormalizeMatrix = shouldNormalizeMatrix.orElse(DEFAULT_SHOULD_NORMALIZE_MATRIX);
		calculatePCAfromCovarCorrelMatrix(covarCorrelMatrix);
	}
	
	public PrincipalComponentAnalysis(AbstractDataSet dataset, OptionalDouble varianceThreshold){
		this.varianceThreshold = varianceThreshold.orElse(DEFAULT_VARIANCE_THRESHOLD);
		calculatePCA(dataset);
	}
	
	public PrincipalComponentAnalysis(double[][] covarCorrelMatrix, OptionalDouble varianceThreshold){
		this.varianceThreshold = varianceThreshold.orElse(DEFAULT_VARIANCE_THRESHOLD);
		calculatePCAfromCovarCorrelMatrix(covarCorrelMatrix);
	}

	private void calculatePCA(AbstractDataSet dataset){

		double[][] data = dataset.getNumericData().clone();
//		data = Statistics.centralizaColunasMatriz(data);

		if(shouldNormalizeMatrix){
			data = Statistics.normalizaColunasMatrizDesvioPadrao(data);
		}

		double[][] mCovarCorrel;
		mCovarCorrel = Statistics.calculaMatrizCovariancia(data);

		calculatePCAfromCovarCorrelMatrix(mCovarCorrel);
	}

	public List<PrincipalComponent> getPrincipalComponents(){
		return pcs;
	}
	
	private void calculatePCAfromCovarCorrelMatrix(double[][] mCovarCorrel){
		Map<Double,double[]> autoValoresVetores = Statistics.calculaAutoValoresVetores(mCovarCorrel);
		double somatoriaAutovalores = autoValoresVetores.keySet().stream().mapToDouble(d -> d).sum();
		
		autoValoresVetores.keySet().stream().sorted(Collections.reverseOrder()).reduce(0.0, (cumulativeVarianceBefore,autoValor) -> {
			double varianceRepresented = autoValor/somatoriaAutovalores;
			double cumulativeVariance = cumulativeVarianceBefore+varianceRepresented;
			PrincipalComponent pc = new PrincipalComponent();
			pc.setVarianceRepresented(varianceRepresented);
			pc.setCumulativeVariance(cumulativeVariance);
			pc.setRank(pcs.size()+1);
			pc.setEigenvalue(autoValor);
			pc.setEigenvector(autoValoresVetores.get(autoValor));
			pc.setDiscarded(cumulativeVarianceBefore >= varianceThreshold);
			
			pcs.add(pc);
			return cumulativeVariance;
		});
	}
}
