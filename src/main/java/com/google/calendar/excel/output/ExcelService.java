package com.google.calendar.excel.output;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.DateTime;
import com.google.calendar.output.exception.ExcelFormatException;

public interface ExcelService {
	
	public void generateExcel(String userName, List<String> projectName, List<String> clientName, List calendarName, String templatePath, String inOutPath, Map<String, List<DateTime>> excelData,Date startDate, Date endDate) throws  ExcelFormatException;

}
