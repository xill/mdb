package com.xill.mangadb.util;

import java.util.Comparator;

public class NaturalComparator<T> implements Comparator<T> {
	
	@Override
	public int compare(T baseValA, T baseValB) {
		String strA = baseValA.toString();
		String strB = baseValB.toString();

		// character index
		int indA = 0;
		int indB = 0;
		// empty / zeros counted
		int emptyCountA = 0;
		int emptyCountB = 0;
		// current characters
		char charA = 0;
		char charB = 0;

		while (true) {
			// reset zero count
			emptyCountA = 0;
			emptyCountB = 0;

			// get new characters.
			try { charA = strA.charAt(indA); } catch (Exception e) { charA = 0; }
			try { charB = strB.charAt(indB); } catch (Exception e) { charB = 0; }

			// ignore leading. Loop while actual characters are found.
			while (Character.isSpaceChar(charA) || charA == '0') {
				// count zeros
				if (charA == '0')
					emptyCountA++;
				else
					emptyCountA = 0;

				// get next character
				try { charA = strA.charAt(++indA); } catch (Exception e) { charA = 0; }
			}

			// ignore leading. Loop while actual characters are found.
			while (Character.isSpaceChar(charB) || charB == '0') {
				if (charB == '0')
					emptyCountB++;
				else
					emptyCountB = 0;

				// get next character
				try { charB = strB.charAt(++indB); } catch (Exception e) { charB = 0; }
			}

			// test current number character
			if (Character.isDigit(charA) && Character.isDigit(charB)) {
				int result;
				if ((result = compareTo(strA.substring(indA),
						strB.substring(indB))) != 0)
					return result;
			}

			// check for identical
			if (charA == 0 && charB == 0)
				return emptyCountA - emptyCountB;
			
			// check order if characters are different.
			if (charA < charB) return -1;
			else if (charA > charB) return +1;

			// characters were identical
			// increment character indexes.
			++indA;
			++indB;
		}
	}

	/**
	 * Compare two strings as they were integers.
	 * 
	 * @param strA
	 * @param strB
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 */
	private int compareTo(String strA, String strB) {
		int result = 0;
		int indA = 0;
		int indB = 0;

		while (true) {

			char charA = 0;
			char charB = 0;
			// get next characters
			try { charA = strA.charAt(indA++); } catch (Exception e) { charA = 0; }
			try { charB = strB.charAt(indB++); } catch (Exception e) { charB = 0; }

			if (!Character.isDigit(charA) && !Character.isDigit(charB)) return result;
			else if (!Character.isDigit(charA)) return -1; 
			else if (!Character.isDigit(charB)) return +1; 
			else if (charA < charB) {
				if (result == 0) result = -1;
			} 
			else if (charA > charB) {
				if (result == 0) result = +1;
			} 
			else if (charA == 0 && charB == 0) return result;
		}
	}

}
