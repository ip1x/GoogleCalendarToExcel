package com.google.calendar.excel.output;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.calendar.exception.ExcelFormatException;

/**
 * This interface provide method to generate excel file with event details
 *
 * @author DAMCO
 *
 */
public interface ExcelService {

    /**
     * Used to copy template file to a temporary location, then populating the
     * copied file with required date of events.
     *
     * @param templatePath
     *            template file location
     * @param inOutPath
     *            Configuration file location
     * @param excelData
     *            Map containing Event title as KEy and its TAG-VALUE as VALUE.
     * @param dateList
     *            List of date containing "Start date" as first element and
     *            "End date" as Second element
     * @param resultPath
     *            Output file location
     * @throws ExcelFormatException
     */
    public void generateExcel(String templatePath, String inOutPath, Map<String, Map<String, String>> excelData,
	    List<Date> dateList, String resultPath) throws ExcelFormatException;

}
