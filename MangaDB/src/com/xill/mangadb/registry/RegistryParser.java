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
import com.xill.mangadb.db.SeriesName;
import com.xill.mangadb.db.Tag;
import com.xill.mangadb.db.Thumbnail;
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
						String rawTags = (String) prop.get(Series.KEY_TAGS);
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
						String authorStr = prop.getProperty(Series.KEY_AUTHOR);
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
							// TODO
							authorRegistry.put(authorStr, author);
						}
						// read artist
						String artistStr = prop.getProperty(Series.KEY_ARTIST);
						if(artistStr != null && artistStr.length() > 0) {
							artistStr = artistStr.trim();
							// get author if exists.
							Author author = authorRegistry.get(artistStr);
							// if null. create it.
							if(author == null) {
								author = new Author();
								author.setName(artistStr);
							}
							
							// TODO
							author.addSeriesArtist(serie);
							authorRegistry.put(artistStr, author);
						}
						// read series description
						String descriptionStr = prop.getProperty(Series.KEY_DESCRIPTION);
						if(descriptionStr != null && descriptionStr.length() > 0) {
							serie.setDescription(descriptionStr);
						}
						// chapter order
						String chapterOrderStr = prop.getProperty(Series.KEY_CHAPTER_ORDER);
						if(chapterOrderStr != null && chapterOrderStr.length() > 0) {
							serie.setChapterOrder(chapterOrderStr);
						}
						// read names
						String namesStr = prop.getProperty(Series.KEY_NAMES);
						if(namesStr != null && namesStr.length() > 0) {
							String[] names = namesStr.split(",");
							for( String n : names ) {
								SeriesName sn = new SeriesName();
								sn.setName(n);
								serie.addSeriesName(sn);
							}
						}
						// read thumbnail
						String thumbUrl = prop.getProperty(Series.KEY_THUMBNAIL);
						if(thumbUrl != null && thumbUrl.length() > 0) {
							Thumbnail thumb = new Thumbnail();
							thumb.setUrl(thumbUrl);
							serie.setThumbnail(thumb);
						}
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				String[] chapterListing = seriesFolder.list();
				// go through chapters.
				for( String chapterName : chapterListing ) {
					System.out.println(registryLocation + File.separator + seriesName + File.separator + chapterName);
					File chapterFolder = new File(registryLocation + File.separator + seriesName + File.separator + chapterName);
					if(chapterFolder.isDirectory()) {
						Chapter chapter = new Chapter();
						chapter.setName(chapterFolder.getName());

						chapterRegistry.add(chapter);
						// get pages and sort them.
						String[] pageListing = chapterFolder.list();
						List<String> pL = Arrays.asList(pageListing);
						Collections.sort(pL, new NaturalComparator<String>());
						pageListing = pL.toArray(new String[]{});
						// go through pages
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
				serie.setDefaults();
				serie.sortToOrder();
				serie.saveProperties();
				// add parsed series to registry
				seriesRegistry.add(serie);
			}
		}
	}
}
