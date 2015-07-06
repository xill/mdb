package com.xill.mangadb.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Thumbnail {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String url = "";
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "series_id")
	private Series series;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void addThumbnailSeries(Series serie) {
		this.series = serie;
	}
	
	public int getId() {
		return id;
	}
	
	public int getRefId() {
		return (series != null)? series.getId() : -1;
	}
}
