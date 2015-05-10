package com.xill.mangadb.util;

public class StringUtil {

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
	
	public static String toValidJson(String value) {
		return value.replace("\\", "\\\\");
	}

}
