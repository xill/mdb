package com.xill.mangadb.db;

import java.util.ArrayList;
import java.util.Collection;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Author {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String name = "";
	@ForeignCollectionField
	private Collection<Series> series = new ArrayList<Series>();
	
	public void addSeries(Series serie) {
		serie.setAuthor(this);
		series.add(serie);
	}
	
	public void addSeriesArtist(Series serie) {
		serie.setArtist(this);
		series.add(serie);
	}
	
	public Collection<Series> getSeries() {
		return series;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
}
