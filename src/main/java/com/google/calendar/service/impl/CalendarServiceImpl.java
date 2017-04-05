package com.google.calendar.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.service.CalendarService;

public class CalendarServiceImpl implements CalendarService {
	
	/** Application name. */
	private static final String APPLICATION_NAME = "Google Calendar Sample";

	/** Directory to store user credentials for this application. */
	private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"),
			".credentials/calendar-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;
	
	public final Logger logger = Logger.getLogger(CalendarServiceImpl.class);

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/calendar-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);
	
	
	//Default constrctor
	public CalendarServiceImpl() {
		super();
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (final Exception t) {			
			logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , t);
			System.exit(1);
		}
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public Calendar getCalendarService(HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final Credential credential = authorize(request,response);
		return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}
	
	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public Credential authorize(HttpServletRequest request, final HttpServletResponse response) {
		try {	
			// Load client secrets.
			InputStream in = getClass().getClassLoader().getResourceAsStream("client_secret.json");
			final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
					JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY)
							.setAccessType("offline").build();
			return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
					.authorize("user");
			
			
		} catch (Exception e) {
			logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e);
			try {
				request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_GOOGLE_AUTHENTICATION);
				request.getRequestDispatcher(CalendarConstant.HOME_PAGE).forward(request, response);
			} catch (ServletException | IOException e1) {	
				logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e1);
				request.setAttribute(CalendarConstant.ERROR_MESSAGE, CalendarConstant.ERROR_IN_LOADING);
				
			}

		}
		return null;
	}
	
}
