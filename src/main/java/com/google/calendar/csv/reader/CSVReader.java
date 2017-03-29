package com.google.calendar.csv.reader;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
