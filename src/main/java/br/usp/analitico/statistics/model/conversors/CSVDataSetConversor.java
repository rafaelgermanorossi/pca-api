package br.usp.analitico.statistics.model.conversors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;

import br.usp.analitico.statistics.model.AbstractDataSet;
import br.usp.analitico.statistics.model.DefaultDataSet;
import br.usp.analitico.statistics.model.DimensionBean;

public class CSVDataSetConversor implements IDataSetConversor{
	
	private Object[][] rawData;
	private double[][] numericData;
	private String[][] classData;

	private List<CSVRecord> recordsList;
	private AbstractDataSet dataset;
		
	public CSVDataSetConversor(String filePath, char separator){
		dataset = new DefaultDataSet();

		try {
			recordsList = CSVParser.parse(new File (filePath), Charset.defaultCharset(), CSVFormat.newFormat(separator)).getRecords();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AbstractDataSet convert(){
		dataset.observationsSize = recordsList.size()-1;
		int dimensionsSize = recordsList.get(0).size();
		
		dataset.setDimensions(new ArrayList<DimensionBean>(dimensionsSize));
		dataset.setNumericDimensions(new ArrayList<DimensionBean>());
		dataset.setClassDimensions(new ArrayList<DimensionBean>());
		
		rawData = new String[dataset.observationsSize][dimensionsSize];

		for (int dimension = 0; dimension < dimensionsSize; dimension++) {	
			DimensionBean dim = new DimensionBean();
			dim.setPosition(dimension+1);
			for (int observation = 0; observation <= dataset.observationsSize; observation++) {
					String data = recordsList.get(observation).get(dimension);
					if(observation == 0) {
						dim.setLabel(data);
					}else{
						if(observation == 1){
							if(NumberUtils.isCreatable(data)){
								dim.setNumeric(true);
								dataset.getNumericDimensions().add(dim);
							}else{
								dim.setNumeric(false);
								dataset.getClassDimensions().add(dim);
							}
							dataset.getDimensions().add(dim);
						}
						rawData[observation-1][dimension] = data;
					}
				}
		}

		numericData = new double [dataset.observationsSize][dataset.getNumericDimensionsSize()];
		classData = new String [dataset.observationsSize][dataset.getClassDimensionsSize()];

		int numericCounter = 0;
		int classCounter = 0;

		for (int i = 0; i < dataset.observationsSize; i++) {
			numericCounter = 0;
			classCounter = 0;
			for (int j = 0; j < dataset.getDimensionsSize(); j++) {
				DimensionBean dim = dataset.getDimensions().get(j);
				if(dim.isNumeric()){
					double data = Double.parseDouble(rawData[i][j].toString());
					numericData[i][numericCounter] = data;
					if(i == 0){
						dim.setCurrentVariance(0);
						dim.setCurrentAverage(data);
						dim.setCurrentStandardDeviation(0);
					}else{
						double delta = data - dim.getCurrentAverage();
						dim.setPreviousAverage(dim.getCurrentAverage());
						dim.setLastInput(data);
						dim.setCurrentAverage(dim.getCurrentAverage() + delta/(i+1));
						dim.setCurrentVariance(((i)*dim.getCurrentVariance()+delta*(data-dim.getCurrentAverage()))/(double)(i+1));
						dim.setCurrentStandardDeviation(Math.sqrt(dim.getCurrentVariance()));
					}
					numericCounter++;
				}else {
					classData[i][classCounter++] = rawData[i][j].toString();
				}
			}
		}
		
		dataset.setRawData(rawData);
		dataset.setClassData(classData);
		dataset.setNumericData(numericData);
		
		return dataset;
	}

}
