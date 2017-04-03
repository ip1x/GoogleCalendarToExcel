package com.google.calendar.service;

import java.io.IOException;

import com.google.api.services.calendar.Calendar;

/**
 * 
 * This interface provides method to call google calendar api
 * 
 * @author DAMCO
 *
 */
public interface CalendarService {

	/**
	 * Return Calendar object with credential and google calendar details
	 * 
	 * @return Calendar Calendar object with event details
	 * @throws IOException
	 */
	public Calendar getCalendarService() throws IOException;

}
