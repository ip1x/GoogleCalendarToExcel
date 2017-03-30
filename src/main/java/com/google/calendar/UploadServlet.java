
package com.google.calendar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.csv.reader.CSVReader;
import com.google.calendar.excel.output.ExcelService;
import com.google.calendar.factory.ServiceFactory;
import com.google.calendar.service.CalendarService;

/**
 * Controller class to Handle incoming request with CSV file
 * Will Read CSV file and generate Excel
 * 
 * @author DAMCO
 *
 */
public class UploadServlet extends HttpServlet {

	
	
	
	/**
	 * default serial version
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Servlet post method to handle incoming post request
	 * 
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType(CalendarConstant.CONTENT_TYPE);	
		CSVReader csvReader = (CSVReader) ServiceFactory.getInstance(CSVReader.class);
		Map<String, String> inputMap = csvReader.readCSV(request);	

		List calendarName = Arrays.asList(inputMap.get("CALENDAR").split(CalendarConstant.COMMA_SPLITTER));
		String templatePath = inputMap.get(CalendarConstant.TEMPLATE) != null ? inputMap.get(CalendarConstant.TEMPLATE) :CalendarConstant.TEMPLATE_FILE_NAME;
		String resultName = inputMap.get(CalendarConstant.OUTFILE) != null ? inputMap.get(CalendarConstant.OUTFILE) : CalendarConstant.RESULT_FILE_NAME;
		String inOutPath = inputMap.get(CalendarConstant.INOUTMAP) != null ? inputMap.get(CalendarConstant.INOUTMAP) : CalendarConstant.CONFIGURATION_FILE_NAME;

		// optional need to check for null at the time of logic
		List<String> projectName = inputMap.get(CalendarConstant.PROJECT) != null ? Arrays.asList(inputMap.get(CalendarConstant.PROJECT).split(CalendarConstant.COMMA_SPLITTER)): null;
		List<String> clientName = inputMap.get(CalendarConstant.CLIENT) != null ? Arrays.asList(inputMap.get(CalendarConstant.CLIENT).split(CalendarConstant.COMMA_SPLITTER)) : null;

		//created Date format for date 201703010000
		SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarConstant.DATE_FORMAT);
		DateTime from = null;
		DateTime to = null;
		try {
			Date fromDate = inputMap.get(CalendarConstant.FROM) != null ? dateFormat.parse(inputMap.get(CalendarConstant.FROM)) : new Date();
			@SuppressWarnings("deprecation")
			Date toDate = inputMap.get(CalendarConstant.TO) != null ? dateFormat.parse(inputMap.get(CalendarConstant.TO))
					: new Date(fromDate.getYear(), 12, 31);

			from = new DateTime(fromDate);
			to = new DateTime(toDate);

			// Build a new authorized API client service.
			// Note: Do not confuse this class with the
			// com.google.api.services.calendar.model.Calendar class.
			CalendarService calendarService = (CalendarService) ServiceFactory.getInstance(CalendarService.class);
			Calendar service = calendarService.getCalendarService();

			Map<String, List<DateTime>> excelData = new HashMap<String, List<DateTime>>();
			String userName = "";
			String pageToken = null;
			
			do {
				CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
				List<CalendarListEntry> listItems = calendarList.getItems();

				for (CalendarListEntry calendarListEntry : listItems) {
					if (calendarName.contains(calendarListEntry.getSummary())) {
						final Events events = service.events().list(calendarListEntry.getId()).setMaxResults(100)
								.setTimeMin(from).setOrderBy(CalendarConstant.START_TIME).setTimeMax(to).setSingleEvents(true)
								.execute();
						final List<Event> items = events.getItems();
						if (items.isEmpty()) {
							System.out.println("No upcoming events found.");
						} else {
							System.out.println("Upcoming events");

							userName = items.get(0).getCreator().getDisplayName();
							
							for (final Event event : items) {								
								DateTime start = event.getStart().getDateTime();
								DateTime end = event.getEnd().getDateTime();								
								if (start == null) {
									start = event.getStart().getDate();
								}
								//put start event date at index 0 and end date at index 1
								List<DateTime> dateList = new LinkedList<DateTime>();
								dateList.add(start);
								dateList.add(end);
								excelData.put(event.getSummary(), dateList);
								System.out.printf("%s (%s)\n", event.getSummary(), start + "and end date" + end);
							}
						}
					}
				}
				pageToken = calendarList.getNextPageToken();
			} while (pageToken != null);

			ExcelService excelService = excelService = (ExcelService) ServiceFactory.getInstance(ExcelService.class);
			excelService.generateExcel(userName,projectName,clientName,calendarName, templatePath, resultName, inOutPath, excelData,fromDate,toDate);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
