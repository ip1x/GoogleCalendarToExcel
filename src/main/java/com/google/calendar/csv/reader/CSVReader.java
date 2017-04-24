package com.google.calendar.csv.reader;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the interface for CSV file reader.
 *
 * @author DAMCO
 */
public interface CSVReader {

    /**
     * Return Map of input parameter taken from CSV file
     *
     * @param request
     *            HttpServletRequest object with form parameter
     * @param response
     *            HttpServletResponse object
     * @return Map of input parameter
     */
    public Map<String, String> readCSV(final HttpServletRequest request, final HttpServletResponse response);

}
