package com.google.calendar.service;

import java.io.IOException;

import com.google.api.services.calendar.Calendar;

public interface CalendarService {
	

	/**
	 * Return Calendar object with credential and google calendar details
	 * 
	 * @return Calendar
	 * @throws IOException
	 */
	public Calendar getCalendarService() throws IOException;

}
