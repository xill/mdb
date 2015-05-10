package com.xill.mangadb.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.Page;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.util.Options;
import com.xill.mangadb.util.StringUtil;

public class RegistryParser {

	private List<Series> seriesRegistry = null;
	private List<Chapter> chapterRegistry = null;
	private List<Page> pageRegistry = null;
	
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
		
		File registryBase = new File(registryLocation);
		String[] seriesListing = registryBase.list();
		for(String seriesName : seriesListing) {
			System.out.println(seriesName);
			System.out.println(registryLocation + File.separator + seriesName);
			File seriesFolder = new File(registryLocation + File.separator + seriesName);
			if(seriesFolder.isDirectory()) {
				Series serie = new Series();
				serie.setName(seriesFolder.getName());
				String[] chapterListing = StringUtil.sort(seriesFolder.list());
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
