package br.usp.analitico.statistics.model.conversors;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.usp.analitico.statistics.model.AbstractDataSet;
import br.usp.analitico.statistics.model.DefaultDataSet;
import br.usp.analitico.statistics.model.DimensionBean;

public class ResultSetDataSetConversor implements IDataSetConversor{

	private List<String[]> rawData;
	private double[][] numericData;
	private String[][] classData;

	private ResultSet recordsList;
	private ResultSetMetaData recordsMetadata;
	private String identityColumn;
	
	private AbstractDataSet dataset;
		
	public ResultSetDataSetConversor(ResultSet rs){
		dataset = new DefaultDataSet();
		recordsList = rs;
	}
	
	public ResultSetDataSetConversor(ResultSet rs, String identityColumn){
		dataset = new DefaultDataSet();
		recordsList = rs;
		this.identityColumn = identityColumn;
	}

	public AbstractDataSet convert(){
		try {
			convertWithExceptionHandling();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataset;
	}
	
	private void convertWithExceptionHandling() throws SQLException{
		recordsMetadata = recordsList.getMetaData();
				
		dataset.observationsSize = 0;
		int dimensionsSize = recordsMetadata.getColumnCount()-(identityColumn == null || identityColumn.trim().isEmpty()? 0 : 1);

		dataset.setDimensions(new ArrayList<DimensionBean>(dimensionsSize));
		dataset.setNumericDimensions(new ArrayList<DimensionBean>());
		dataset.setClassDimensions(new ArrayList<DimensionBean>());
		
		rawData = new ArrayList<String[]>();
		
		int position = 0;

		for (int dimension = 0; dimension < recordsMetadata.getColumnCount(); dimension++) {
			if(recordsMetadata.getColumnLabel(dimension+1).equals(identityColumn)){
				continue;
			}
			DimensionBean dim = new DimensionBean();
			dim.setPosition(position+1);
			dim.setLabel(recordsMetadata.getColumnLabel(dimension+1));
			if(numericTypes.contains(recordsMetadata.getColumnType(dimension+1))){
				dim.setNumeric(true);
				dataset.getNumericDimensions().add(dim);
			}else{
				dim.setNumeric(false);
				dataset.getClassDimensions().add(dim);
			}
			dataset.getDimensions().add(dim);
			position++;
		}
		
		while(recordsList.next()) {
			String[] observation = new String[dimensionsSize];
			position = 0;
			for (int dimension = 0; dimension < recordsMetadata.getColumnCount(); dimension++) {
				if(recordsMetadata.getColumnLabel(dimension+1).equals(identityColumn)){
					dataset.setCurrentIdentityMaxValue(recordsList.getObject(dimension+1).toString());
					continue;
				}
				
				String data = recordsList.getObject(dimension+1) == null ? "" :recordsList.getObject(dimension+1).toString();
				observation[position++] = data;
			}
			rawData.add(observation);
			dataset.observationsSize++;
		}
		
		recordsList.close();

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
					double data = Double.parseDouble(rawData.get(i)[j].toString());
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
					classData[i][classCounter++] = rawData.get(i)[j].toString();
				}
			}
		}
		
		dataset.setRawData(rawData.toArray(new Object[dataset.observationsSize][dataset.getDimensionsSize()]));
		dataset.setClassData(classData);
		dataset.setNumericData(numericData);
	}

}
