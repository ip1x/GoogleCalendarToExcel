package com.google.calendar.constant;

import java.text.Format;
import java.text.SimpleDateFormat;

public class CalendarConstant {
	
	//CSV file field
	public static final String CALENDAR = "CALENDAR";
	public static final String TEMPLATE = "TEMPLATE";
	public static final String OUTFILE = "OUTFILE";
	public static final String INOUTMAP = "INOUTMAP";	
	public static final String PROJECT = "PROJECT";
	public static final String CLIENT = "CLIENT";	
	public static final String FROM = "FROM";
	public static final String TO = "TO";
	
	//Default file names for input parameter
	public static final String TEMPLATE_FILE_NAME = "Timesheet.xls";	
	public static final String RESULT_FILE_NAME = "Results.xls";
	public static final String CONFIGURATION_FILE_NAME = "configuration.properties";
	
	public static final Format df = new SimpleDateFormat("MM/dd/yyyy");
	
	/**
	 * Temporary file location. We can improve code quality by putting this on resource folder
	 */
	public static final String DESTINATION_FILE_PATH = "/Users/pavankumar/Desktop/Testing/Java.txt";
	
	/**
	  * Content type of servlet response
	  */
	 public static final String CONTENT_TYPE = "application/vnd.ms-excel";
	 
	 /**
	  * Content Header of servlet response
	  */
	 public static final String CONTENT_HEADER = "content-disposition";
	
	/**
	 * Date format used inside application
	 */
	public static final String DATE_FORMAT = "yyyyMMddHHmm";
	
	/**
	 * On which field shorting will happen on calendar api result
	 */
	public static final String START_TIME = "startTime";
	
	// String splitter
	public static final String COMMA_SPLITTER = ",";
	public static final String COL_SPLITTER = ":";
	
	/**
	 *  maximum size that will be stored in memory
	 */
	public static final int MAXMEMSIZE = 4 * 1024;	
	
	/**
	 * maximum file size to be uploaded.
	 */
	public static final int MAXFILESIZE = 50 * 1024;
	
	/**
	 * Location to save data that is larger than max Memory Size.
	 */
	public static final String TEMP_STORAGE_LOCATION = "c:\\temp";
	/**
	 * Maximum file size to be uploaded
	 */
	public static final String TEMP_FILE_LOCATION  = "C:\\test.csv";

	
	


}
