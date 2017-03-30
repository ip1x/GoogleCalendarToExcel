package com.google.calendar.csv.reader;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Reader interface for CSV
 * Will contain method to read the csv
 * 
 * @author DAMCO
 *
 */
public interface CSVReader {

	/**
	 * 
	 * Return Map of input parameter taken from CSV file
	 * 
	 * @param request
	 *            HttpServletRequest object with form parameter
	 * @return Map of input parameter
	 */
	public Map<String, String> readCSV(final HttpServletRequest request);

}
