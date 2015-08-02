package com.xill.mangadb.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class ContentTag {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String name = "";
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "series_id")
	private Series series;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addTaggedSeries(Series serie) {
		this.series = serie;
	}
	
	public int getId() {
		return id;
	}
	
	public int getRefId() {
		return (series != null)? series.getId() : -1;
	}
}
