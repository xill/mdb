package com.xill.mangadb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * General mangadb properties wrapper class.
 */
public class Options {

	private static String propertyFileName = "mangadb.properties";
	private static Properties properties = null;
	
	public static String REGISTRY_URL = "registryUrl";
	
	/**
	 * Load properties file data.
	 */
	public static void load() {
		properties = new Properties();
		try {
			File baseFile = new File(propertyFileName);
			if(!baseFile.exists()) baseFile.createNewFile();
			
			properties.load(new FileInputStream(baseFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save properties data to file.
	 */
	public static void save() {
		try {
			FileOutputStream os = new FileOutputStream(new File(propertyFileName));
			properties.store(os, "MangaDB properties file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {
		return (String) properties.get(key);
	}
	
	public static void set(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public static void setIfNotSet(String key, String value) {
		if(properties.get(key) == null) {
			set(key,value);
		}
	}
}
