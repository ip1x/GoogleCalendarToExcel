
package com.google.calendar.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	Event firstEvent = null;
	try {
	    response.setContentType(CalendarConstant.CONTENT_TYPE);
	    final CSVReader csvReader = (CSVReader) ServiceFactory.getInstance(CSVReader.class);

	    logger.info("Start Reading CSV file.............");
	    final Map<String, String> inputMap = csvReader.readCSV(request, response);

	    // Creates different variables for different input parameter from
	    // map.
	    final List<String> calendarName = Arrays
		    .asList(inputMap.get(CalendarConstant.CALENDAR).split(CalendarConstant.COMMA_SPLITTER));
	    final String templatePath = inputMap.get(CalendarConstant.TEMPLATE) != null
		    ? inputMap.get(CalendarConstant.TEMPLATE) : CalendarConstant.TEMPLATE_FILE_NAME;
	    final String resultPath = inputMap.get(CalendarConstant.OUTFILE) != null
		    ? inputMap.get(CalendarConstant.OUTFILE) : CalendarConstant.RESULT_FILE_NAME;
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

	    final java.util.Calendar today = java.util.Calendar.getInstance();

	    final Date fromDate = inputMap.get(CalendarConstant.FROM) != null
		    ? dateFormat.parse(inputMap.get(CalendarConstant.FROM)) : today.getTime();

	    final java.util.Calendar yearEnd = java.util.Calendar.getInstance();
	    yearEnd.setTime(fromDate);
	    yearEnd.set(yearEnd.get(java.util.Calendar.YEAR), 11, 31);

	    final Date toDate = inputMap.get(CalendarConstant.TO) != null
		    ? dateFormat.parse(inputMap.get(CalendarConstant.TO)) : yearEnd.getTime();

	    final DateTime from = new DateTime(fromDate);
	    final DateTime to = new DateTime(toDate);

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

			// Fetches all events from the calendar between start
			// date and end date provided
			final Events events = service.events().list(calendarListEntry.getId()).setMaxResults(100)
				.setTimeMin(from).setOrderBy(CalendarConstant.START_TIME).setTimeMax(to)
				.setSingleEvents(true).execute();
			final List<Event> items = events.getItems();
			if (items.isEmpty()) {
			    logger.info("No Events for the calendar : " + calendarListEntry.getSummary());
			} else {

			    logger.info("Parsing all events for calendars .............");
			    for (final Event event : items) {
				firstEvent = parsingEvent(firstEvent, inOutPath, projectNameAsList, clientNameAsList,
					excelData, calendarListEntry, event);
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
	    logger.info("Starting generating output file .............");
	    excelService.generateExcel(templatePath, inOutPath, excelData, dateList, resultPath);

	    logger.info("Preparing file for download .............");
	    downloadFile(request, response, firstEvent, resultPath);

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
	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_GOOGLE_AUTHENTICATION);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }
	} catch (final Exception e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);

	    try {
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_SCV_VALIDATION);
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }

	}
    }

    /**
     * This method parses Event for a given calendar and applies filter on that
     * event for CLIENT and PROJECT of input file. If that event fulfills the
     * filtering OR condition than only it is used in output file, otherwise
     * not.
     *
     * @param firstEvent
     *            Event used to check if there is at least one event for the
     *            output file.
     * @param inOutPath
     *            Configuration file path containing mapping for output file.
     * @param projectNameAsList
     *            Name of projects mentioned in INPUT file as List.
     * @param clientNameAsList
     *            Name of clients mentioned in INPUT file as List.
     * @param excelData
     *            Map of Events that will be used to generate output file. It
     *            contains "Event summary" as KEY and Event KEY-VALUE pair as
     *            VALUE
     * @param calendarListEntry
     *            Calendar of which event is parsing
     * @param event
     *            Event which is to be parsed
     * @return firstEvent
     */
    private Event parsingEvent(final Event firstEvent, final String inOutPath, final List<String> projectNameAsList,
	    final List<String> clientNameAsList, final Map<String, Map<String, String>> excelData,
	    final CalendarListEntry calendarListEntry, final Event event) {
	Event validEvent = firstEvent;
	try {
	    final EventTitleParser eventTitleParser = new EventTitleParser();

	    // Parses event to generate MAP for Event Tag as KEY and its value
	    // as VALUE
	    final Map<String, Map<String, String>> eventKeyValue = eventTitleParser.generateMapForEvents(event,
		    inOutPath, calendarListEntry.getSummary());
	    final String eventSummary = event.getSummary();

	    // Applies filtering to check whether Event is to be used in output
	    // file or not
	    if ((clientNameAsList.isEmpty() && projectNameAsList.isEmpty())
		    || (clientNameAsList.contains(eventKeyValue.get(eventSummary).get(CalendarConstant.CLI_LOWER_CASE)))
		    || (projectNameAsList
			    .contains(eventKeyValue.get(eventSummary).get(CalendarConstant.PRJ_LOWER_CASE)))) {
		excelData.put(eventSummary, eventKeyValue.get(eventSummary));
		// Puts the Event in a object, that means we have at least one
		// event for the output file.
		validEvent = event;
	    }
	} catch (final Exception e) {
	    logger.info("Event with uncompiled name is found");
	    logger.error(CalendarConstant.ERROR_INVALID_EVENT_FOUND, e);
	    excelData.put(event.getSummary(), null);
	}
	return validEvent;
    }

    /**
     * This method generates output file for downloading. If value for OUTPUT
     * file is passed in the INPUT file then that value is used for downloading
     * file name otherwise an arbitrary predefined name is used for downloading
     * file name.
     *
     * @param request
     *            HttpServlet request containing Client request.
     * @param response
     *            HttpServlet response which will contain the download able file
     * @param firstEvent
     *            Event used to check if there is at least one event for the
     *            output file.If Yes, then file is downloaded otherwise an ERROR
     *            message will be shown.
     * @param resultPath
     *            Output file path where file has to be downloaded
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void downloadFile(final HttpServletRequest request, final HttpServletResponse response,
	    final Event firstEvent, final String resultPath) throws IOException {
	String outputFileName = resultPath;
	InputStream inputStream;
	// If we have at least one event for output file then
	if (firstEvent != null) {
	    // Checks whether any OUTPUT file name was provided in the INPUT
	    // file or not.
	    final File file = new File(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
		    ? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
	    inputStream = new FileInputStream(file);

	    // If OUTPUT file name was provided in the INPUT file then
	    if (!resultPath.equals(CalendarConstant.RESULT_FILE_NAME)) {
		final String[] splitPathArr = resultPath.split("\\\\");
		// Gets Output file name only not its full path
		outputFileName = splitPathArr[splitPathArr.length - 1];
	    }
	    response.setHeader(CalendarConstant.CONTENT_HEADER, "attachment; filename=" + outputFileName);
	    final OutputStream outstream = response.getOutputStream();
	    // Output file for downloading is generated.
	    IOUtils.copyLarge(inputStream, outstream);
	    request.setAttribute(CalendarConstant.ERROR_MESSAGE, "");
	    try {
		inputStream.close();
	    } catch (final Exception e) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    }
	} else {
	    // If don't have any Event for output file.
	    request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_NO_EVENT_FOUND);
	    try {
		// Show error message that "No Events Found"
		request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
	    } catch (ServletException | IOException e1) {
		logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
		request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
	    }
	}
    }
}
