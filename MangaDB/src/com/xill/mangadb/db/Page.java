package com.xill.mangadb.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Page {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String page;
	@DatabaseField(dataType = DataType.STRING)
	private String pageRemote;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "chapter_id")
	private Chapter chapter;
	
	public Chapter getChapter() {
		return chapter;
	}
	
	public void setChapter(Chapter chapter) {
		this.chapter = chapter;
	}
	
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	
	public String getPageRemote() {
		return pageRemote;
	}

	public void setPageRemote(String pageRemote) {
		this.pageRemote = pageRemote;
	}
}
