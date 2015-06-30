package com.xill.mangadb.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.xill.mangadb.util.NaturalComparator;
import com.xill.mangadb.util.Options;
import com.xill.mangadb.util.StringUtil;

public class Series {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(dataType = DataType.STRING)
	private String name = "";
	@DatabaseField(dataType = DataType.STRING)
	private String description = "";
	@DatabaseField(dataType = DataType.STRING)
	private String chapterOrder = "";
	
	@ForeignCollectionField
	private Collection<Chapter> chapters = new ArrayList<Chapter>();
	@ForeignCollectionField
	private Collection<Tag> tags = new ArrayList<Tag>();
	@ForeignCollectionField
	private Collection<SeriesName> seriesNames = new ArrayList<SeriesName>(); 
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "author_id")
	private Author author;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "artist_id")
	private Author artist;
	
	public static final String KEY_TAGS = "tags";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_ARTIST = "artist";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CHAPTER_ORDER = "chapter_order";
	public static final String KEY_NAMES = "names";
	
	public void setAuthor(Author author) {
		this.author = author;
		if(artist == null) artist = this.author;
	}
	
	public Author getAuthor() {
		return this.author;
	}
	
	public void setArtist(Author artist) {
		this.artist = artist;
	}
	
	public Author getArtist() {
		return this.artist;
	}

	public void addChapter(Chapter chapter) {
		chapter.setSeries(this);
		chapters.add(chapter);
	}
	
	public Collection<Chapter> getChapters() {
		return chapters;
	}
	
	public void addTag(Tag tag) {
		tag.addTaggedSeries(this);
		tags.add(tag);
	}
	
	public Collection<Tag> getTags() {
		return tags;
	}
	
	public void addSeriesName(SeriesName name) {
		name.setRefSeries(this);
		seriesNames.add(name);
	}
	
	public Collection<SeriesName> getSeriesNames() {
		return seriesNames;
	}
	
	public String getSeriesNameString() {
		StringBuilder builder = new StringBuilder();
		for( SeriesName n : seriesNames ) {
			if(builder.length() > 0) builder.append(",");
			builder.append(n.getName());
		}
		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getChapterOrder() {
		return this.chapterOrder;
	}
	
	public void setChapterOrder(String chapterOrder) {
		this.chapterOrder = chapterOrder;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * Set some default values.
	 */
	public void setDefaults() {
		if(seriesNames.isEmpty()) {
			SeriesName sn = new SeriesName();
			sn.setName(name);
			addSeriesName(sn);
		}
	}
	
	/**
	 * Sort chapters to a specific ordering.
	 */
	public void sortToOrder() {
		
		Map<String,Integer> nameToKey = new HashMap<String,Integer>();
		List<Chapter> chapterList = new ArrayList<Chapter>(chapters);
		List<Integer> newOrder = new ArrayList<Integer>(chapterList.size());
		List<Chapter> finalChapterOrder = new ArrayList<Chapter>(chapters.size());
		// record initial ordering.
		for(int i = 0; i < chapterList.size(); ++i) {
			Chapter c = chapterList.get(i);
			nameToKey.put(c.getName(), i);
		}
		
		// order by chapter custom chapter order.
		if(chapterOrder != null && chapterOrder.length() > 0) {
			
			String[] order = chapterOrder.split(",");
			for( String o : order ) {
				Integer i = nameToKey.remove(o);
				if(i != null) {
					newOrder.add(i);
				}
			}
			newOrder.addAll(nameToKey.values());
			
		} 
		// order by natural order.
		else {
			// perform a natural sort on name listing.
			List<String> cL = new ArrayList<String>(nameToKey.size());
			for(String s : nameToKey.keySet()) cL.add(s);
			Collections.sort(cL, new NaturalComparator());
			
			for( String o : cL ) {
				Integer i = nameToKey.remove(o);
				if(i != null) {
					newOrder.add(i);
				}
			}
			// add all remaining to the end in original order.
			newOrder.addAll(nameToKey.values());
		}
		
		StringBuilder orderBuilder = new StringBuilder();
		// determine final order.
		for( int i : newOrder ) {
			Chapter c = chapterList.get(i);
			finalChapterOrder.add(c);
			
			if(orderBuilder.length() > 0) orderBuilder.append(","); 
			orderBuilder.append(c.getName());
		}
		
		this.chapterOrder = orderBuilder.toString(); 
		this.chapters = finalChapterOrder;
	}
	
	public void saveProperties() {
		File serieProp = new File(Options.get(Options.REGISTRY_URL) + File.separator + name + File.separator + "series.properties");
		Properties p = new Properties();
		// load existing properties file
		if(serieProp.exists()) {
			try {
				p.load(new FileReader(serieProp));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// create file since it didn't exit.
		else {
			try {
				serieProp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		p.setProperty(KEY_CHAPTER_ORDER, this.chapterOrder);
		p.setProperty(KEY_NAMES, getSeriesNameString());
		
		// write properties
		try {
			p.store(new FileOutputStream(serieProp), name);
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
		builder.append("\"name\":"+StringUtil.toValidJsonValue(name)+",");
		List<SeriesName> names = new ArrayList<SeriesName>(seriesNames);
		builder.append("\"names\":[");
		for(int i = 0; i < names.size(); ++i) {
			if(i > 0) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(names.get(i).getName()));
		}
		builder.append("],");
		builder.append("\"chapters\":");
		builder.append("[");
		List<Chapter> chapterList = new ArrayList<Chapter>(chapters);
		for(int i = 0; i < chapterList.size(); ++i) {
			if(i >= 1) builder.append(",");
			builder.append(chapterList.get(i).toString());
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
	
	public String outputJson()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"name\":"+StringUtil.toValidJsonValue(name)+",");
		List<SeriesName> names = new ArrayList<SeriesName>(seriesNames);
		builder.append("\"names\":[");
		for(int i = 0; i < names.size(); ++i) {
			if(i > 0) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(names.get(i).getName()));
		}
		builder.append("],");
		if(author != null) builder.append("\"author\":"+StringUtil.toValidJsonValue(author.getName())+",");
		if(artist != null) builder.append("\"artist\":"+StringUtil.toValidJsonValue(artist.getName())+",");
		builder.append("\"description\":"+StringUtil.toValidJsonValue(description)+",");
		builder.append("\"chapters\":");
		builder.append("[");
		List<Chapter> chapterList = new ArrayList<Chapter>(chapters);
		for(int i = 0; i < chapterList.size(); ++i) {
			if(i >= 1) builder.append(",");
			builder.append("{");
			Chapter chapter = chapterList.get(i);
			builder.append("\"name\":");
			builder.append(StringUtil.toValidJsonValue(chapter.getName()));
			builder.append(",");
			builder.append("\"pageCount\":");
			builder.append(StringUtil.toValidJsonValue(chapter.getPages().size()+""));
			builder.append("}");
		}
		builder.append("],");
		builder.append("\"tags\":");
		builder.append("[");
		List<Tag> tagList = new ArrayList<Tag>(tags);
		for(int i = 0; i < tagList.size(); ++i) {
			if(i >= 1) builder.append(",");
			builder.append(StringUtil.toValidJsonValue(tagList.get(i).getName()));
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
}
