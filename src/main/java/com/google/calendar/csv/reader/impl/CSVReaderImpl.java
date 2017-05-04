package com.google.calendar.csv.reader.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.csv.reader.CSVReader;

/**
 * This class will used to read the csv file and convert the csv file data into
 * map object
 *
 * @author DAMCO
 */
public class CSVReaderImpl implements CSVReader {

    public final Logger logger = Logger.getLogger(CSVReaderImpl.class);

    /*
     * (non-Javadoc)
     *
     * @see com.google.calendar.csv.reader.CSVReader#readCSV(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, String> readCSV(final HttpServletRequest request, final HttpServletResponse response) {

	final DiskFileItemFactory factory = new DiskFileItemFactory();
	factory.setSizeThreshold(CalendarConstant.MAXMEMSIZE);

	// Location to save data that is larger than maxMemSize.
	factory.setRepository(new File(CalendarConstant.TEMP_STORAGE_LOCATION));
	final ServletFileUpload upload = new ServletFileUpload(factory);
	upload.setSizeMax(CalendarConstant.MAXFILESIZE);

	// Create input from CSV
	BufferedReader bufferReader = null;
	final Map<String, String> inputMap = new LinkedHashMap<>();

	try {
	    final List fileItems = upload.parseRequest(request);
	    final Iterator iterator = fileItems.iterator();
	    InputStream inputStream = null;
	    while (iterator.hasNext()) {
		final FileItem fileItem = (FileItem) iterator.next();
		if (!fileItem.isFormField()) {

		    inputStream = fileItem.getInputStream();

		} else {
		    throw new FileNotFoundException();
		}
	    }

	    bufferReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

	    logger.info("Reading CSV file.............");
	    generateMapForInput(request, response, bufferReader, inputMap);

	} catch (final Exception e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_FILE_SELECTION);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }
	} finally {
	    if (bufferReader != null) {
		try {
		    bufferReader.close();
		} catch (final IOException e) {
		    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
		}
	    }
	}

	return inputMap;
    }

    /**
     *
     * @param request
     * @param response
     * @param bufferReader
     * @param lastKey
     * @param inputMap
     * @throws IOException
     * @throws ServletException
     */
    private void generateMapForInput(final HttpServletRequest request, final HttpServletResponse response,
	    final BufferedReader bufferReader, final Map<String, String> inputMap)
	    throws IOException, ServletException {
	String line;
	String lastKey = "";
	if ((line = bufferReader.readLine()) != null) {

	    final String[] argument = line.split(" ");
	    for (final String string : argument) {
		final String[] keyValue = string.split(CalendarConstant.COL_SPLITTER, 2);
		if ((keyValue != null) && (keyValue.length == 2)) {
		    inputMap.put(keyValue[0].trim(), keyValue[1].trim());
		    lastKey = keyValue[0].trim();
		}
		if ((keyValue != null) && (keyValue.length == 1)) {
		    inputMap.replace(lastKey, inputMap.get(lastKey).concat(" ").concat(keyValue[0].trim()));
		}
	    }

	} else {
	    request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_FILE_SELECTION);
	    request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	}
    }

}
