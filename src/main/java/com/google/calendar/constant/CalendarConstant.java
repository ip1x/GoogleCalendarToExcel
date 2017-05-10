package com.google.calendar.constant;

import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Class containing all constants used at application level.
 *
 * @author DAMCO
 *
 */
public class CalendarConstant {

    /**
     * Temporary file location.
     */
    public static final String DESTINATION_FILE_PATH = "C:\\calendar.xls";

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

    /**
     * maximum size that will be stored in memory
     */
    public static final int MAXMEMSIZE = 4 * 1024;

    /**
     * Maximum file size to be uploaded.
     */
    public static final int MAXFILESIZE = 50 * 1024;

    /**
     * Location to save data that is larger than max Memory Size.
     */
    public static final String TEMP_STORAGE_LOCATION = "c:\\temp";

    /**
     * Home Page
     */
    public static final String HOME_PAGE = "/index.jsp";

    // error messages

    public static final String LOGGER_DEFAULT_MESSAGE = "Error due to : ";

    public static final String ERROR_MESSAGE = "errorMessage";

    public static final String ERROR_IN_LOADING = "Error in loading file";

    public static final String ERROR_IN_PARSING_DATE = "Error in parsing Date";

    public static final String ERROR_IN_READING_EXCEL = "Error in reading excel File";

    public static final String ERROR_IN_SCV_VALIDATION = "Error in CSV validation";

    public static final String ERROR_IN_FILE_SELECTION = "Please Select a valid file";

    public static final String ERROR_IN_GOOGLE_AUTHENTICATION = "Google Authentication failed. Kindly provide valid json file";

    public static final String ERROR_NO_EVENT_FOUND = "No Events Found";

    public static final String ERROR_INVALID_EVENT_FOUND = "Invalid Event Found";

    // String constants used for splitting strings. String can be from INPUT or
    // either from Events title

    public static final String COMMA_SPLITTER = ",";

    public static final String COL_SPLITTER = ":";

    // CSV file field. These are constants created for the TAGS of INPUT file.
    // These are used to create MAP of INPUT file data

    public static final String CALENDAR = "CALENDAR";

    public static final String TEMPLATE = "TEMPLATE";

    public static final String OUTFILE = "OUTFILE";

    public static final String INOUTMAP = "INOUTMAP";

    public static final String PROJECT = "PROJECT";

    public static final String CLIENT = "CLIENT";

    public static final String FROM = "FROM";

    public static final String TO = "TO";

    // Default file names for input parameter if no parameters are provided in
    // the INPUT file

    public static final String TEMPLATE_FILE_NAME = "Timesheet.xls";

    public static final String RESULT_FILE_NAME = "Results.xls";

    public static final String CONFIGURATION_FILE_NAME = "configuration.properties";

    // Date format for output file. 1st format is used while populating
    // fields of "From" or "To" of Output file. And other is used for populating
    // "Start" and "End" date of Event.

    public static final Format EXCEL_HEADER_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static final Format TABLE_DATE_FORMAT = new SimpleDateFormat("M/dd/yy HH:mm");

    // Events Titles. These are Event titles TAG as constants. As some of the
    // TAGS cann't be DYNAMIC and are used in code for FILTERING purpose so have
    // created CONSTANTS.

    public static final String CLI_LOWER_CASE = "cli";

    public static final String PRJ_LOWER_CASE = "prj";

    public static final String ACT_LOWER_CASE = "act";

    public static final String WBS_LOWER_CASE = "wbs";

    public static final String STF_LOWER_CASE = "stf";

    public static final String STARTED_ON_LOWER_CASE = "startedon";

    public static final String ENDED_ON_LOWER_CASE = "endedon";

    // These are the constants for the HEADER of OUTPUT file which is present
    // above TABLE of OUTPUT file.

    public static final String PROJECTS_HEADER = "ProjectsHeader";

    public static final String CLIENTS_HEADER = "ClientsHeader";

    public static final String TO_HEADER = "ToHeader";

    public static final String FROM_HEADER = "FromHeader";

    public static final String STAFF_HEADER = "StaffHeader";

    private CalendarConstant() {
	// default constructor
    }

}
