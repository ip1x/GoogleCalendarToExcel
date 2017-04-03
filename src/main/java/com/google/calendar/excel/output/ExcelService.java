package com.google.calendar.excel.output;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.api.client.util.DateTime;
import com.google.calendar.output.exception.ExcelFormatException;

/**
 * This interface provide method to generate excel file with event details
 * 
 * @author DAMCO
 *
 */
public interface ExcelService {

	/**
	 * @param userName
	 *            google user name
	 * @param projectName
	 *            Project name
	 * @param clientName
	 *            client name
	 * @param templatePath
	 *            template file location
	 * @param inOutPath
	 *            Configuration file location
	 * @param excelData
	 *            event details
	 * @param startDate
	 *            event filter starting date
	 * @param endDate
	 *            event filter end date
	 * @throws ExcelFormatException
	 */
	public void generateExcel(String userName, List<String> projectName, List<String> clientName, String templatePath,
			String inOutPath, Map<String, List<DateTime>> excelData, Date startDate, Date endDate)
			throws ExcelFormatException;
	/**
	 * Will convert event name into data field.
	 * 
	 * @param entry
	 *            object for event name with date details
	 * @param userName
	 *            google user name
	 * @return Map with key value from event name
	 */
	 Map<String, String> getDataFromEventName(Entry<String, List<DateTime>> entry, String userName);

}
