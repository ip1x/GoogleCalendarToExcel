package com.google.calendar.excel.output.impl;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.google.api.client.util.DateTime;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.excel.output.ExcelService;
import com.google.calendar.output.exception.ExcelFormatException;
import com.google.calendar.util.ConfigurationFileParser;
import com.google.calendar.util.GenerateOutputExcel;

/**
 * This class will generate excel file and populate event details from Google
 * calendars
 * 
 * @author DAMCO
 *
 */
public class ExcelServiceImpl implements ExcelService {

	final String staff = "Staff";
	final String project = "Projects";
	final String client = "Clients";
	final String from = "From";
	final String to = "To";
	final String startDate = "Started on";
	final String endDate = "Ended on";
	final String workedHours = "Worked Hours";
	final String act = "ACT";

	
	/* (non-Javadoc)
	 * @see com.google.calendar.excel.output.ExcelService#generateExcel(java.lang.String, java.util.List, java.util.List, java.lang.String, java.lang.String, java.util.Map, java.util.Date, java.util.Date)
	 */
	public void generateExcel(String userName, List<String> projectName, List<String> clientName, 
			String templatePath, String inOutPath, Map<String, List<DateTime>> excelData, Date startDate, Date endDate)
			throws ExcelFormatException {

		try {

			// Create configuration file object to create map of excel table
			// header name
			ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
			Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

			// Added excel header field which comes from event details
			propertyMap.put(this.startDate, this.startDate);
			propertyMap.put(this.endDate, this.endDate);
			propertyMap.put(this.staff,this.staff);
			propertyMap.put(this.workedHours, this.workedHours);

			// Create copy of supplied excel file to populate data in excel
			GenerateOutputExcel generateOutputExcel = new GenerateOutputExcel();
			generateOutputExcel.generateExcelFile(templatePath);

			Sheet sheet = generateOutputExcel.getSheet();

			// Adding two dummy column to error free execution
			int columnSize = propertyMap.size() + 2;

			// Row number of table header
			int headerRow = getStartHeader(sheet, columnSize);

			// basic details of sheet
			setHeaderValue(sheet, getNameAsString(clientName), userName, getNameAsString(projectName), startDate,
					endDate, headerRow, columnSize);

			Map<String, Map<String, String>> eventKeyValue = new HashMap<String, Map<String, String>>();
			for (Map.Entry<String, List<DateTime>> entry : excelData.entrySet()) {
				Map<String, String> eventDetails = getDataFromEventName(entry, userName);
				eventKeyValue.put(entry.getKey(), eventDetails);

			}
			
			//populate excel table with event details
			setColumnsValue(sheet,columnSize,headerRow,eventKeyValue,propertyMap);

			// update the excel with updated sheet.
			FileOutputStream outFile = new FileOutputStream(CalendarConstant.DESTINATION_FILE_PATH);
			generateOutputExcel.getWorkbook().write(outFile);
			outFile.close();

		} catch (Exception e) {

			e.printStackTrace();
			throw new ExcelFormatException();
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
		Map<String, String> map = new HashMap<String, String>();

		String eventData[] = ((String) entry.getKey()).split(" ");

		StringBuffer actStringPart = new StringBuffer("");
		for (String string : eventData) {

			String keyValue[] = string.split(":");
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
			if (map.containsKey(act)) {
				map.replace(act, map.get(act) + actStringPart);
				actStringPart =  new StringBuffer("");
				
			}
		}
		
		long duration  = new Date(entry.getValue().get(1).getValue()).getTime() - new Date(entry.getValue().get(0).getValue()).getTime();

		
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

		map.put("Ended on", CalendarConstant.df.format(new Date(entry.getValue().get(1).getValue())));
		map.put("Started on", CalendarConstant.df.format(new Date(entry.getValue().get(0).getValue())));
		map.put("Staff", userName);
		map.put("Staff", userName);
		map.put("Worked Hours", diffInHours +" : " + diffInMinutes);
		

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
						//final int m = j;
						for(Entry entry : excelData.entrySet()){
							Row valueRow = sheet.getRow(j++);
							if (valueRow != null) {
								Cell valueCell = valueRow.getCell(i);
								//valueCell.setCellValue(excelData.get(propertyEntry.getKey()));
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

					Cell valueCell = null;
					if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
						System.out.println(cell.getStringCellValue());
						switch (cell.getStringCellValue().trim()) {
						case staff:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(userName);
							break;

						case from:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(CalendarConstant.df.format(startDate));
							break;

						case to:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(CalendarConstant.df.format(endDate));
							break;

						case client:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(clientName);
							break;
						case project:
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
							&& cell.getStringCellValue().trim().equals(startDate)) {
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
	private String getNameAsString(List<String> list) {
		StringBuilder b = new StringBuilder();
		list.forEach(b::append);
		return b.toString();
	}

}
