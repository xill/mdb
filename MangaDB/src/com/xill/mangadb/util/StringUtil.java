package com.xill.mangadb.util;

import org.codehaus.jettison.json.JSONObject;

public class StringUtil {

	/**
	 * Sort string array.
	 * 
	 * @param array - array to sort.
	 * @return - sorted array.
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
	 * String contains function which ignores letter case.
	 * 
	 * @param compareA
	 * @param compareB
	 * @return - true if B contains A. false otherwise.
	 */
	public static boolean containsIgnoreCase( String compareA , String compareB ) {
		if(compareA == null) throw new NullPointerException();
		if(compareB == null) throw new NullPointerException();
		
		String a = compareA.toLowerCase();
		String b = compareB.toLowerCase();
		
		return a.contains(b);
	}
	
	/**
	 * Add json quotes to string
	 * 
	 * @param value - string to modify
	 * @return - modified string.
	 */
	public static String toValidJsonValue(String value) {
		return JSONObject.quote(value);
	}

}
