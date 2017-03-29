/**
 *
 */
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
import com.google.calendar.csv.reader.CSVReader;
import com.google.calendar.csv.reader.impl.CSVReaderImpl;
import com.google.calendar.service.CalendarService;
import com.google.calendar.service.impl.CalendarServiceImpl;

/**
 * @author User
 *
 */
public class UploadServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1875242968807644942L;

	CalendarService calendarService;
	CSVReader csvReader;


	@Override
	public void init() throws ServletException {
		calendarService = new CalendarServiceImpl();
		csvReader = new CSVReaderImpl();
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");

		
		Map<String, String> inputMap = csvReader.readCSV(request);	

		List calendarName = Arrays.asList(inputMap.get("CALENDAR").split(","));
		String templatePath = inputMap.get("TEMPLATE") != null ? inputMap.get("TEMPLATE") : "Timesheet.xls";
		String resultName = inputMap.get("OUTFILE") != null ? inputMap.get("OUTFILE") : "Results.xls";
		String inOutPath = inputMap.get("INOUTMAP") != null ? inputMap.get("INOUTMAP") : "configuration.properties";

		// optional need to check for null at the time of logic
		List<String> projectName = inputMap.get("PROJECT") != null ? Arrays.asList(inputMap.get("PROJECT").split(",")): null;
		List<String> clientName = inputMap.get("CLIENT") != null ? Arrays.asList(inputMap.get("CLIENT").split(",")) : null;

		//created Date format for date 201703010000
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		DateTime from = null;
		DateTime to = null;
		try {
			Date fromDate = inputMap.get("FROM") != null ? df.parse(inputMap.get("FROM")) : new Date();
			@SuppressWarnings("deprecation")
			Date toDate = inputMap.get("TO") != null ? df.parse(inputMap.get("TO"))
					: new Date(fromDate.getYear(), 12, 31);

			from = new DateTime(fromDate);
			to = new DateTime(toDate);

			// Build a new authorized API client service.
			// Note: Do not confuse this class with the
			// com.google.api.services.calendar.model.Calendar class.
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
								.setTimeMin(from).setOrderBy("startTime").setTimeMax(to).setSingleEvents(true)
								.execute();
						final List<Event> items = events.getItems();
						if (items.size() == 0) {
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


		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// do nothing.
	}

	
}
