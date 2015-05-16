package com.xill.mangadb.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.xill.mangadb.db.Author;
import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.Page;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.db.Tag;
import com.xill.mangadb.util.NaturalComparator;
import com.xill.mangadb.util.Options;

public class RegistryParser {

	private List<Series> seriesRegistry = null;
	private List<Chapter> chapterRegistry = null;
	private List<Page> pageRegistry = null;
	private Map<String,Author> authorRegistry = null;
	
	public List<Series> getSeriesRegistry() {
		return seriesRegistry;
	}
	
	public List<Chapter> getChapterRegistry() {
		return chapterRegistry;
	}
	
	public List<Page> getPageRegistry() {
		return pageRegistry;
	}
	
	public void clearRegister() {
		seriesRegistry.clear();
		chapterRegistry.clear();
		pageRegistry.clear();
	}
	
	public void parseInternalRegistry() {
		String registryLocation = Options.get(Options.REGISTRY_URL);
		seriesRegistry = new ArrayList<Series>();
		chapterRegistry = new ArrayList<Chapter>();
		pageRegistry = new ArrayList<Page>();
		authorRegistry = new HashMap<String,Author>();
		
		File registryBase = new File(registryLocation);
		String[] seriesListing = registryBase.list();
		for(String seriesName : seriesListing) {
			System.out.println(seriesName);
			System.out.println(registryLocation + File.separator + seriesName);
			File seriesFolder = new File(registryLocation + File.separator + seriesName);
			if(seriesFolder.isDirectory()) {
				Series serie = new Series();
				serie.setName(seriesFolder.getName());
				
				// read stored property file
				File serieProp = new File(registryLocation + File.separator + seriesName + File.separator + "series.properties");
				if(serieProp.exists()) {
					Properties prop = new Properties();
					try {
						prop.load(new FileReader(serieProp));
						// read tags
						String rawTags = (String) prop.get("tags");
						if(rawTags != null && rawTags.length() > 0) {
							String[] tags = rawTags.split(",");
							for( String tag : tags ) {
								String tagName = tag.trim();
								Tag tagObj = new Tag();
								tagObj.setName(tagName);
								serie.addTag(tagObj);
							}
						}
						// read author
						String authorStr = prop.getProperty("author");
						if(authorStr != null && authorStr.length() > 0) {
							authorStr = authorStr.trim();
							// get author if exists.
							Author author = authorRegistry.get(authorStr);
							// if null. create it.
							if(author == null) {
								author = new Author();
								author.setName(authorStr);
							}
							
							author.addSeries(serie);
							authorRegistry.put(authorStr, author);
						}
						// read artist
						String artistStr = prop.getProperty("artist");
						if(artistStr != null && artistStr.length() > 0) {
							artistStr = artistStr.trim();
							// get author if exists.
							Author author = authorRegistry.get(artistStr);
							// if null. create it.
							if(author == null) {
								author = new Author();
								author.setName(artistStr);
							}
							
							author.addSeriesArtist(serie);
							authorRegistry.put(artistStr, author);
						}
						// read series description
						String descriptionStr = prop.getProperty("description");
						if(descriptionStr != null && descriptionStr.length() > 0) {
							serie.setDescription(descriptionStr);
						}
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// perform a natural sort on file listing.
				List<String> cL = Arrays.asList(seriesFolder.list());
				Collections.sort(cL, new NaturalComparator());
				String[] chapterListing = cL.toArray(new String[]{});

				for( String chapterName : chapterListing ) {
					System.out.println(registryLocation + File.separator + seriesName + File.separator + chapterName);
					File chapterFolder = new File(registryLocation + File.separator + seriesName + File.separator + chapterName);
					if(chapterFolder.isDirectory()) {
						Chapter chapter = new Chapter();
						chapter.setName(chapterFolder.getName());

						chapterRegistry.add(chapter);
						String[] pageListing = chapterFolder.list();
						for( String pageName : pageListing ) {
							String pagePath = seriesName + File.separator + chapterName + File.separator + pageName;
							System.out.println(pagePath);
							Page page = new Page();
							page.setPage(pagePath);
							
							chapter.addPage(page);
							pageRegistry.add(page);
						}
						serie.addChapter(chapter);
					}
				}
				seriesRegistry.add(serie);
			}
		}
	}
}
