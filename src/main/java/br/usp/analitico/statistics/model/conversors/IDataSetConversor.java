package br.usp.analitico.statistics.model.conversors;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import br.usp.analitico.statistics.model.AbstractDataSet;


public interface IDataSetConversor {
	public static final List<Integer> numericTypes = Arrays.asList(new Integer[]{Types.NUMERIC, Types.DECIMAL, Types.BIT, Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.REAL, Types.FLOAT, Types.DOUBLE});
	
	public abstract AbstractDataSet convert();
}
