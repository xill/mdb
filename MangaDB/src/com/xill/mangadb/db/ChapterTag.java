package com.xill.mangadb.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class ChapterTag {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String name = "";
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "chapter_id")
	private Chapter chapter;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addTaggedChapter(Chapter chapter) {
		this.chapter = chapter;
	}
	
	public int getId() {
		return id;
	}
	
	public int getRefId() {
		return (chapter != null)? chapter.getId() : -1;
	}
}
