package com.google.calendar.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.calendar.Calendar;

/**
 * This interface provides method to call google calendar api
 *
 * @author DAMCO
 */
@FunctionalInterface
public interface CalendarService {

    /**
     * Return Calendar object with credential and google calendar details
     *
     * @param request
     *            HttpServletRequest object with form parameter
     * @param response
     *            HttpServletResponse object
     * @return Calendar Calendar object with event details
     * @throws IOException
     */
    public Calendar getCalendarService(HttpServletRequest request, final HttpServletResponse response)
	    throws IOException;

}
