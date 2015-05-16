package com.xill.mangadb.util;

import org.codehaus.jettison.json.JSONObject;

public class StringUtil {

	/**
	 * TODO
	 * 
	 * @param array
	 * @return
	 */
	public static String[] sort(String[] array) {
		String temp;

		for (int j = 0; j < array.length - 1; j++) {
			for (int i = j + 1; i < array.length; i++) {
				if (array[i].compareTo(array[j]) < 0) {
					temp = array[j];
					array[j] = array[i];
					array[i] = temp;
				}
			}

		}
		return array;
	}
	
	/**
	 * TODO
	 * 
	 * @param compareA
	 * @param compareB
	 * @return
	 */
	public static boolean containsIgnoreCase( String compareA , String compareB ) {
		if(compareA == null) throw new NullPointerException();
		if(compareB == null) throw new NullPointerException();
		
		String a = compareA.toLowerCase();
		String b = compareB.toLowerCase();
		
		return a.contains(b);
	}
	
	/**
	 * TODO
	 * 
	 * @param value
	 * @return
	 */
	public static String toValidJsonValue(String value) {
		return JSONObject.quote(value);
	}

}
