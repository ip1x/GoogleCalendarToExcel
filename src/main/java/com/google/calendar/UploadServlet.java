/**
 *
 */
package com.google.calendar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author User
 *
 */
public class UploadServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1875242968807644942L;
	String message;

	@Override
	public void init() throws ServletException
	{
		// Do required initialization
		message = "Hello World";
	}

	@Override
	public void doPost(final HttpServletRequest request,
			final HttpServletResponse response)
					throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		final PrintWriter out = response.getWriter();
		out.println("<h1>" + message + "</h1>");
	}

	@Override
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response)
					throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		final PrintWriter out = response.getWriter();
		out.println("<h1> Hello </h1>");
	}

	@Override
	public void destroy()
	{
		// do nothing.
	}
}
