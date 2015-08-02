package com.xill.mangadb.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.xill.mangadb.util.Options;
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
	
	@ForeignCollectionField
	private Collection<ChapterTag> tags = new ArrayList<ChapterTag>();
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "series_id")
	private Series series;
	
	public static final String KEY_CONTENT_TAGS = "content_tags";

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
	
	public void addTag(ChapterTag tag) {
		tag.addTaggedChapter(this);
		tags.add(tag);
	}
	
	public Collection<ChapterTag> getTags() {
		return tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getTagString() {
		StringBuilder builder = new StringBuilder();
		for(ChapterTag t : tags) {
			String name = t.getName();
			if(builder.length() > 0) builder.append(",");
			builder.append(name);
		}
		return builder.toString();
	}
	
	public void saveProperties() {
		File chapterProp = new File(Options.get(Options.REGISTRY_URL) + File.separator + series.getName() + File.separator + name + File.separator + "chapter.properties");
		Properties p = new Properties();
		// load existing properties file
		if(chapterProp.exists()) {
			try {
				p.load(new FileReader(chapterProp));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// create file since it didn't exit.
		else {
			try {
				chapterProp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		p.setProperty(KEY_CONTENT_TAGS, getTagString());

		// write properties
		try {
			p.store(new FileOutputStream(chapterProp), name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		builder.append("],");
		builder.append("\"tags\":");
		builder.append("[");
		List<ChapterTag> tagList = new ArrayList<ChapterTag>(tags);
		for(int i = 0; i < tagList.size(); ++i) {
			if(i > 0) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(tagList.get(i).getName()+""));
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
