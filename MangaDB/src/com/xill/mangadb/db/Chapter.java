package com.xill.mangadb.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.xill.mangadb.util.StringUtil;

public class Chapter implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String name = "";
	
	@ForeignCollectionField
	private Collection<Page> pages = new ArrayList<Page>();
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "series_id")
	private Series series;

	public void setSeries(Series series) {
		this.series = series;
	}
	
	public Series getSeries() {
		return this.series;
	}
	
	public ArrayList<Page> getPages() {
		return new ArrayList<Page>(pages);
	}
	
	public void addPage(Page page) {
		page.setChapter(this);
		this.pages.add(page);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"id\":"+StringUtil.toValidJsonValue(id+"")+",");
		builder.append("\"name\":"+StringUtil.toValidJsonValue(name));
		builder.append(",");
		builder.append("\"pages\":");
		builder.append("[");
		List<Page> pageList =  new ArrayList<Page>(pages);
		for(int i = 0; i < pageList.size(); ++i) {
			if(i > 0) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(pageList.get(i)+""));
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
	
	public String outputJson() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"name\":"+StringUtil.toValidJsonValue(name));
		builder.append(",");
		builder.append("\"pages\":");
		builder.append("[");
		List<Page> pageList = new ArrayList<Page>(pages);
		for(int i = 0; i < pageList.size(); ++i) {
			if(i > 0) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(pageList.get(i).getPage()));
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
}
