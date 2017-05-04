
package com.google.calendar.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.csv.reader.CSVReader;
import com.google.calendar.excel.output.ExcelService;
import com.google.calendar.exception.ExcelFormatException;
import com.google.calendar.exception.InvalidEventException;
import com.google.calendar.factory.ServiceFactory;
import com.google.calendar.service.CalendarService;
import com.google.calendar.util.EventTitleParser;

/**
 * Controller class to Handle incoming request. This servlet reads CSV file as
 * input,process google calender's events and and generate Excel
 *
 * @author DAMCO
 */
public class UploadServlet extends HttpServlet {

    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;

    public final Logger logger = Logger.getLogger(UploadServlet.class);

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
	    throws ServletException, IOException {

	InputStream inputStream = null;
	Event firstEvent = null;
	try {
	    response.setContentType(CalendarConstant.CONTENT_TYPE);
	    final CSVReader csvReader = (CSVReader) ServiceFactory.getInstance(CSVReader.class);

	    logger.info("Start Reading CSV file.............");
	    final Map<String, String> inputMap = csvReader.readCSV(request, response);

	    final List<String> calendarName = Arrays
		    .asList(inputMap.get(CalendarConstant.CALENDAR).split(CalendarConstant.COMMA_SPLITTER));
	    final String templatePath = inputMap.get(CalendarConstant.TEMPLATE) != null
		    ? inputMap.get(CalendarConstant.TEMPLATE) : CalendarConstant.TEMPLATE_FILE_NAME;
	    String resultPath = inputMap.get(CalendarConstant.OUTFILE) != null ? inputMap.get(CalendarConstant.OUTFILE)
		    : CalendarConstant.RESULT_FILE_NAME;
	    final String inOutPath = inputMap.get(CalendarConstant.INOUTMAP) != null
		    ? inputMap.get(CalendarConstant.INOUTMAP) : CalendarConstant.CONFIGURATION_FILE_NAME;

	    // optional need to check for null at the time of logic
	    final List<String> projectNameAsList = inputMap.get(CalendarConstant.PROJECT) != null
		    ? Arrays.asList(inputMap.get(CalendarConstant.PROJECT).split(CalendarConstant.COMMA_SPLITTER))
		    : new ArrayList<>();
	    final List<String> clientNameAsList = inputMap.get(CalendarConstant.CLIENT) != null
		    ? Arrays.asList(inputMap.get(CalendarConstant.CLIENT).split(CalendarConstant.COMMA_SPLITTER))
		    : new ArrayList<>();

	    // created Date format for date 201703010000
	    final SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarConstant.DATE_FORMAT);
	    DateTime from = null;
	    DateTime to = null;

	    final java.util.Calendar today = java.util.Calendar.getInstance();

	    final Date fromDate = inputMap.get(CalendarConstant.FROM) != null
		    ? dateFormat.parse(inputMap.get(CalendarConstant.FROM)) : today.getTime();

	    final java.util.Calendar yearEnd = java.util.Calendar.getInstance();
	    yearEnd.setTime(fromDate);
	    yearEnd.set(yearEnd.get(java.util.Calendar.YEAR), 11, 31);

	    final Date toDate = inputMap.get(CalendarConstant.TO) != null
		    ? dateFormat.parse(inputMap.get(CalendarConstant.TO)) : yearEnd.getTime();

	    from = new DateTime(fromDate);
	    to = new DateTime(toDate);

	    // Build a new authorized API client service.
	    // Note: Do not confuse this class with the
	    // com.google.api.services.calendar.model.Calendar class.
	    final CalendarService calendarService = (CalendarService) ServiceFactory.getInstance(CalendarService.class);

	    logger.info("Fetching all calendars for the user.............");
	    final Calendar service = calendarService.getCalendarService(request, response);

	    final Map<String, Map<String, String>> excelData = new HashMap<>();
	    String pageToken = null;

	    do {
		final CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
		final List<CalendarListEntry> listItems = calendarList.getItems();

		logger.info("Parsing all calendars for the required calendars only.............");
		for (final CalendarListEntry calendarListEntry : listItems) {
		    if (calendarName.contains(calendarListEntry.getSummary())) {
			final Events events = service.events().list(calendarListEntry.getId()).setMaxResults(100)
				.setTimeMin(from).setOrderBy(CalendarConstant.START_TIME).setTimeMax(to)
				.setSingleEvents(true).execute();
			final List<Event> items = events.getItems();
			if (items.isEmpty()) {

			} else {

			    logger.info("Parsing all events for calendars .............");
			    for (final Event event : items) {
				try {
				    firstEvent = parsingEvents(firstEvent, projectNameAsList, clientNameAsList,
					    excelData, event, inOutPath, calendarListEntry.getSummary());
				} catch (final Exception e) {
				    logger.info("Event with uncompiled name is found");
				    excelData.put(event.getSummary(), null);
				}
			    }
			}
		    }
		}
		pageToken = calendarList.getNextPageToken();
	    } while (pageToken != null);

	    final List<Date> dateList = new LinkedList<>();
	    dateList.add(fromDate);
	    dateList.add(toDate);
	    final ExcelService excelService = (ExcelService) ServiceFactory.getInstance(ExcelService.class);
	    excelService.generateExcel(templatePath, inOutPath, excelData, dateList, resultPath);

	    if (firstEvent != null) {
		final File file = new File(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
			? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
		inputStream = new FileInputStream(file);

		if (!resultPath.equals(CalendarConstant.RESULT_FILE_NAME)) {
		    final String[] splitPathArr = resultPath.split("\\\\");
		    resultPath = splitPathArr[splitPathArr.length - 1];
		}
		response.setHeader(CalendarConstant.CONTENT_HEADER, "attachment; filename=" + resultPath);
		final OutputStream outstream = response.getOutputStream();
		IOUtils.copyLarge(inputStream, outstream);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, "");
	    } else {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_NO_EVENT_FOUND);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    }

	} catch (final ParseException e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_PARSING_DATE);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }
	} catch (final ExcelFormatException e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_READING_EXCEL);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }

	} catch (final TokenResponseException e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_GOOGLE_AUTHENTICATION);
	    request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	} catch (final Exception e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);

	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_SCV_VALIDATION);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }

	} finally {
	    if (inputStream != null) {
		inputStream.close();
	    }
	}
    }

    /**
     * This method checks whether the event passed "Has to be added to output
     * file or not" if YES then is added to another Map. Otherwise not.
     *
     * @param firstEvent
     *            used to check whether we have a single event or not.
     * @param projectName
     *            name of project for which output file is to be created
     * @param clientName
     *            name of client for which output file is to be created
     * @param excelData
     *            empty map which will contain event and its key-value pair
     * @param event
     *            event to be checked for adding to excel output file
     * @param configurationFilePath
     * @param calendarName
     * @return
     * @throws InvalidEventException
     */
    private Event parsingEvents(Event firstEvent, final List<String> projectName, final List<String> clientName,
	    final Map<String, Map<String, String>> excelData, final Event event, final String configurationFilePath,
	    final String calendarName) throws InvalidEventException {
	final EventTitleParser eventTitleParser = new EventTitleParser();
	final Map<String, Map<String, String>> eventKeyValue = eventTitleParser.generateMapForEvents(event,
		configurationFilePath, calendarName);
	final String eventSummary = event.getSummary();

	if ((clientName.isEmpty() && projectName.isEmpty())
		|| (clientName.contains(eventKeyValue.get(eventSummary).get(CalendarConstant.CLI_LOWER_CASE)))
		|| (projectName.contains(eventKeyValue.get(eventSummary).get(CalendarConstant.PRJ_LOWER_CASE)))) {
	    excelData.put(eventSummary, eventKeyValue.get(eventSummary));
	    firstEvent = event;
	}
	return firstEvent;
    }
}
