package com.google.calendar.excel.output.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.google.api.client.util.DateTime;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.excel.output.ExcelService;
import com.google.calendar.exception.ExcelFormatException;
import com.google.calendar.util.ConfigurationFileParser;
import com.google.calendar.util.GenerateOutputExcel;
import com.google.common.base.Joiner;

/**
 * This class will generate excel file and populate event details from Google
 * calendars
 * 
 * @author DAMCO
 *
 */
public class ExcelServiceImpl implements ExcelService {
	
	

	static final String STAFF = "Staff";
	static final String PROJECT = "Projects";
	static final String CLIENT = "Clients";
	static final String FROM = "From";
	static final String TO = "To";
	static final String STARTDATE = "Started on";
	static final String ENDDATE = "Ended on";
	static final String WORKEDHOURS = "Worked Hours";
	static final String ACT = "ACT";

	public final Logger logger = Logger.getLogger(ExcelServiceImpl.class);
	
	/* (non-Javadoc)
	 * @see com.google.calendar.excel.output.ExcelService#generateExcel(java.lang.String, java.util.List, java.util.List, java.lang.String, java.lang.String, java.util.Map, java.util.Date, java.util.Date)
	 */
	public void generateExcel(String userName, List<String> projectName, List<String> clientName, 
			String templatePath, String inOutPath, Map<String, List<DateTime>> excelData, List<Date> dateList)
			throws ExcelFormatException {

		FileOutputStream outFile = null;
		try {

			// Create configuration file object to create map of excel table
			// header name
			ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
			Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

			// Added excel header field which comes from event details
			propertyMap.put(ExcelServiceImpl.STARTDATE, ExcelServiceImpl.STARTDATE);
			propertyMap.put(ExcelServiceImpl.ENDDATE, ExcelServiceImpl.ENDDATE);
			propertyMap.put(ExcelServiceImpl.STAFF,ExcelServiceImpl.STAFF);
			propertyMap.put(ExcelServiceImpl.WORKEDHOURS, ExcelServiceImpl.WORKEDHOURS);

			// Create copy of supplied excel file to populate data in excel
			GenerateOutputExcel generateOutputExcel = new GenerateOutputExcel();
			generateOutputExcel.generateExcelFile(templatePath);

			Sheet sheet = generateOutputExcel.getSheet();

			// Adding two dummy column to error free execution
			int columnSize = propertyMap.size() + 2;

			// Row number of table header
			int headerRow = getStartHeader(sheet, columnSize);

			

			Map<String, Map<String, String>> eventKeyValue = new HashMap<>();
			for (Map.Entry<String, List<DateTime>> entry : excelData.entrySet()) {
				Map<String, String> eventDetails = getDataFromEventName(entry, userName);
				eventKeyValue.put(entry.getKey(), eventDetails);

			}
			
			// basic details of sheet
			setHeaderValue(sheet, getValueFromKeyAsString(eventKeyValue,"CLI"), userName, getValueFromKeyAsString(eventKeyValue,"PRJ"), dateList.get(0),
								dateList.get(1), headerRow, columnSize);
			
			//populate excel table with event details
			setColumnsValue(sheet,columnSize,headerRow,eventKeyValue,propertyMap);

			// update the excel with updated sheet.
			outFile = new FileOutputStream(CalendarConstant.DESTINATION_FILE_PATH);
			generateOutputExcel.getWorkbook().write(outFile);
			outFile.close();

		} catch (Exception e) {
			logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e);
			throw new ExcelFormatException();
		}
		finally {
			if(outFile != null)
				try {
					outFile.close();
				} catch (IOException e) {
					logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e);
				}
			
		}

	}

	/**
	 * Will convert event name into data field.
	 * 
	 * @param entry
	 *            object for event name with date details
	 * @param userName
	 *            google user name
	 * @return Map with key value from event name
	 */
	public Map<String, String> getDataFromEventName(Entry<String, List<DateTime>> entry, String userName) {
		Map<String, String> map = new HashMap<>();

		String[] eventData = ( entry.getKey()).split(" ");

		StringBuilder actStringPart = new StringBuilder("");
		for (String string : eventData) {

			String[] keyValue = string.split(":");
			if (keyValue != null && keyValue.length == 2) {
				map.put(keyValue[0].trim(), keyValue[1].trim());
			} else {
				if (keyValue[0].charAt(0) != '@' && keyValue[0].charAt(0) != '%') {
					actStringPart.append(" " + keyValue[0]);
				} else {
					map.put(Character.toString(keyValue[0].trim().charAt(0)),
							keyValue[0].trim().substring(1, keyValue[0].length()));
				}
			}
			if (map.containsKey(ACT)) {
				map.replace(ACT, map.get(ACT) + actStringPart);
				actStringPart =  new StringBuilder("");
				
			}
		}
		
		long duration  = new Date(entry.getValue().get(1).getValue()).getTime() - new Date(entry.getValue().get(0).getValue()).getTime();

		
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

		map.put(ENDDATE, CalendarConstant.df.format(new Date(entry.getValue().get(1).getValue())));
		map.put(STARTDATE, CalendarConstant.df.format(new Date(entry.getValue().get(0).getValue())));
		map.put(STAFF, userName);
		map.put(WORKEDHOURS, diffInHours +" : " + diffInMinutes);
		

		return map;

	}

	/**
	 * Will set event details in excel table
	 * 
	 * @param sheet
	 *            Excel sheet
	 * @param columnSize
	 *            maximum column to iterate
	 * @param headerRow
	 *            row number where header will find
	 * @param excelData
	 *            Event details of user
	 * @param propertyMap
	 *            provided configuration for excel table number
	 */
	public  void setColumnsValue(Sheet sheet, int columnSize, int headerRowNo, Map<String, Map<String , String>> excelData,
			Map<String, String> propertyMap) {
		int lastColumnSize = headerRowNo + excelData.size();
		Row headerRow = sheet.getRow(headerRowNo);
		for(Entry propertyEntry : propertyMap.entrySet()) {
			for (int i = 0; i < columnSize; i++) {
				
				Cell hearderCell = headerRow.getCell(i);
				if (hearderCell != null) {
					hearderCell.setCellType(CellType.STRING);
				}

				if (hearderCell != null && hearderCell.getStringCellValue().trim().equalsIgnoreCase((String) propertyEntry.getValue())) {
					for (int j = headerRowNo + 1; j <= lastColumnSize; j++) {
						
						for(Entry entry : excelData.entrySet()){
							Row valueRow = sheet.getRow(j++);
							if (valueRow != null) {
								Cell valueCell = valueRow.getCell(i);
								
								valueCell.setCellValue((String) ((Map) entry.getValue()).get(propertyEntry.getKey()));
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Set Basic details of user in excel file
	 * 
	 * @param sheet
	 *            Excel sheet
	 * @param clientName
	 *            Name of Client
	 * @param userName
	 *            google user name
	 * @param projectNames
	 *            name of project
	 * @param startDate
	 *            Start date for event filter
	 * @param endDate
	 *            End date for event filter
	 * @param headerRow
	 *            max row to iterate
	 * @param columnSize
	 *            maximum column to iterate
	 */
	private void setHeaderValue(final Sheet sheet, final String clientName, final String userName,
			final String projectNames, final Date startDate, final Date endDate, int headerRow, int columnSize) {
		for (int i = 0; i < headerRow; i++) {
			for (int j = 0; j < columnSize; j++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					Cell cell = row.getCell(j);

					if (cell != null) {
						cell.setCellType(CellType.STRING);
					}

					Cell valueCell ;
					if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {						
						switch (cell.getStringCellValue().trim()) {
						case STAFF:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(userName);
							break;

						case FROM:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(CalendarConstant.df.format(startDate));
							break;

						case TO:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(CalendarConstant.df.format(endDate));
							break;

						case CLIENT:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(clientName);
							break;
						case PROJECT:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(projectNames);
							break;

						default:
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Will return Row number from where excel header is started
	 * 
	 * @param sheet
	 *            Excel sheet
	 * @param columnSize
	 *            max column size
	 * @return row number
	 */
	private int getStartHeader(Sheet sheet, int columnSize) {
		for (int i = 0; true; i++) {
			for (int j = 0; j < columnSize; j++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					Cell cell = row.getCell(j);
					if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()
							&& cell.getStringCellValue().trim().equals(STARTDATE)) {
						return i;
					}
				}
			}
		}
	}

	/**
	 * Convert list into comma sepertaed string
	 * 
	 * @param clients
	 * @return
	 */
	private String getValueFromKeyAsString(Map<String, Map<String, String>> keyValue , String name) {	
		
		List<String> list = new ArrayList<>();
		for(Entry e : keyValue.entrySet()){
			list.add( (( Map<String, String>)e.getValue()).get(name.trim()));
		}
		return Joiner.on(",").join(list);
	}


}
