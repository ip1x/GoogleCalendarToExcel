package com.google.calendar.constant;

import java.text.Format;
import java.text.SimpleDateFormat;

public class CalendarConstant {
	
	//CSV file field
	public static String CALENDAR = "CALENDAR";
	public static String TEMPLATE = "TEMPLATE";
	public static String OUTFILE = "OUTFILE";
	public static String INOUTMAP = "INOUTMAP";	
	public static String PROJECT = "PROJECT";
	public static String CLIENT = "CLIENT";	
	public static String FROM = "FROM";
	public static String TO = "TO";
	
	//Default file names for input parameter
	public static String TEMPLATE_FILE_NAME = "Timesheet.xls";	
	public static String RESULT_FILE_NAME = "Results.xls";
	public static String CONFIGURATION_FILE_NAME = "configuration.properties";
	
	static public  Format df = new SimpleDateFormat("MM/dd/yyyy");
	
	/**
	 * Temporary file location. We can improve code quality by putting this on resource folder
	 */
	public static String DESTINATION_FILE_PATH = "C:\\calendar.xls";
	
	/**
	  * Content type of servlet response
	  */
	 public static String CONTENT_TYPE = "application/vnd.ms-excel";
	 
	 /**
	  * Content Header of servlet response
	  */
	 public static String CONTENT_HEADER = "content-disposition";
	
	/**
	 * Date format used inside application
	 */
	public static String DATE_FORMAT = "yyyyMMddHHmm";
	
	/**
	 * On which field shorting will happen on calendar api result
	 */
	public static String START_TIME = "startTime";
	
	// String splitter
	public static String COMMA_SPLITTER = ",";
	public static String COL_SPLITTER = ":";
	
	/**
	 *  maximum size that will be stored in memory
	 */
	public static int MAXMEMSIZE = 4 * 1024;	
	
	/**
	 * maximum file size to be uploaded.
	 */
	public static int MAXFILESIZE = 50 * 1024;
	
	/**
	 * Location to save data that is larger than max Memory Size.
	 */
	public static String TEMP_STORAGE_LOCATION = "c:\\temp";
	/**
	 * Maximum file size to be uploaded
	 */
	public static String TEMP_FILE_LOCATION  = "C:\\test.csv";

	
	


}
