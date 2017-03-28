/**
 *
 */
package com.google.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * @author User
 *
 */
public class UploadServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1875242968807644942L;
	/** Application name. */
	private static final String APPLICATION_NAME = "Google Calendar Sample";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/calendar-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/calendar-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);

	private int maxFileSize = 50 * 1024;
	private int maxMemSize = 4 * 1024;

	@Override
	public void init() throws ServletException {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (final Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File("c:\\temp"));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(maxFileSize);

		BufferedReader br = null;
		String line = "";

		String lastKey = "";
		Map<String, String> inputMap = new LinkedHashMap<String, String>();
		File csvFile = new File("C:\\test.csv");
		try {
			List fileItems = upload.parseRequest(request);
			Iterator i = fileItems.iterator();

			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (!fi.isFormField()) {

					fi.write(csvFile);

				}
			}

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] argument = line.split(",");
				// inputMap.put(argument[0].split(":")[0],
				// argument[0].split(":")[1]);

				for (int j = 0; j < argument.length; j++) {

					// use : as separator
					String[] argArray = argument[j].split(":", 2);

					if (argArray.length == 1) {
						inputMap.replace(lastKey.trim(), inputMap.get(lastKey).trim().concat("," + argArray[0]));
					} else {
						inputMap.put(argArray[0].trim(), argArray[1]);
						lastKey = argArray[0].trim();
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Create input from CSV

		List calendarName = Arrays.asList(inputMap.get("CALENDAR").split(","));
		String templatePath = inputMap.get("TEMPLATE") != null ? inputMap.get("TEMPLATE") : "Timesheet.xls";
		String resultName = inputMap.get("OUTFILE") != null ? inputMap.get("OUTFILE") : "Results.xls";
		String inOutPath = inputMap.get("INOUTMAP") != null ? inputMap.get("INOUTMAP") : "configuration.properties";

		// optional need to check for null at the time of logic
		String projectName = inputMap.get("PROJECT");
		String clientName = inputMap.get("CLIENT");

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		DateTime to = null;
		DateTime from = null;
		try {
			Date toDate = inputMap.get("TO") != null ? df.parse(inputMap.get("TO")) : new Date();
			@SuppressWarnings("deprecation")
			Date endDate = inputMap.get("FROM") != null ? df.parse(inputMap.get("FROM"))
					: new Date(new Date().getYear(), 12, 31);

			to = new DateTime(toDate);
			from = new DateTime(endDate);

			// Build a new authorized API client service.
			// Note: Do not confuse this class with the
			// com.google.api.services.calendar.model.Calendar class.
			final com.google.api.services.calendar.Calendar service = getCalendarService();

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

			// Set response content type
			response.setContentType("text/html");

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

	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public Credential authorize() {
		// Load client secrets.

		try {
			// ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			// File file = new
			// File(classLoader.getResource("client_secret.json").getFile());
			InputStream in = getClass().getClassLoader().getResourceAsStream("client_secret.json");

			// final InputStream in = new FileInputStream(file);
			// final InputStream in =
			// GoogleCalenderService.class.getResourceAsStream("/client_secret.json");
			final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
					JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY)
							.setAccessType("offline").build();
			final Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
					.authorize("user");
			System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
			return credential;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return null;
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public Calendar getCalendarService() throws IOException {
		final Credential credential = authorize();
		return new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}
}
