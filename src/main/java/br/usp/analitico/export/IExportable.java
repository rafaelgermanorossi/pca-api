package br.usp.analitico.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface IExportable {
	
	public Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public default String toJson(){
		return gson.toJson(this);
	}
}
