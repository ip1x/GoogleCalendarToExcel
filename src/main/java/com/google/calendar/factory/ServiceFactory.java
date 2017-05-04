package com.google.calendar.factory;

import com.google.calendar.csv.reader.CSVReader;
import com.google.calendar.csv.reader.impl.CSVReaderImpl;
import com.google.calendar.excel.output.ExcelService;
import com.google.calendar.excel.output.impl.ExcelServiceImpl;
import com.google.calendar.service.CalendarService;
import com.google.calendar.service.impl.CalendarServiceImpl;

/**
 * Factory class to centralized and hide object implementation
 *
 * @author DAMCO
 */
public class ServiceFactory {

    /**
     * private constructor to stop object creation
     */
    private ServiceFactory() {
	// Private constructor
    }

    /**
     * Will return interface implementation class object depends upon argument
     * passed.
     *
     * @param className
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Object getInstance(final Class className) {

	if (CalendarService.class.equals(className)) {
	    return new CalendarServiceImpl();
	} else if (CSVReader.class.equals(className)) {
	    return new CSVReaderImpl();
	} else if (ExcelService.class.equals(className)) {
	    return new ExcelServiceImpl();
	} else {
	    throw new RuntimeException();
	}

    }

}
