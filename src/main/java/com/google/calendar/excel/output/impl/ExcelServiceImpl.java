package com.google.calendar.excel.output.impl;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class ExcelServiceImpl implements ExcelService {

	final String staff = "Staff";
	final String project = "Projects";
	final String client = "Clients";
	final String from = "From";
	final String to = "To";
	final String startDate = "Started on";
	final String endDate = "Ended on";
	final String workedHours = "Worked Hours";

	public void generateExcel(String userName, List<String> projectName, List<String> clientName, List calendarName,
			String templatePath, String inOutPath, Map<String, List<DateTime>> excelData, Date startDate, Date endDate)
			throws ExcelFormatException {

		try {
			GenerateOutputExcel generateOutputExcel = new GenerateOutputExcel();
			ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
			Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

			generateOutputExcel.generateExcelFile(templatePath);

			Sheet sheet = generateOutputExcel.getSheet();

			int columnSize = propertyMap.size() + 4;
			int headerRow = getStartHeader(sheet, columnSize);
			// setHeaderValue

			setHeaderValue(sheet, getNameAsString(clientName), userName, getNameAsString(projectName), startDate,
					endDate, headerRow, columnSize);

			Map<String, String> eventKeyValue = new HashMap<>();

			for (Map.Entry<String, List<DateTime>> entry : excelData.entrySet()) {

				eventKeyValue = getDataFromEventName(entry);
				setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

			}

			FileOutputStream outFile = new FileOutputStream(CalendarConstant.DESTINATION_FILE_PATH);
			generateOutputExcel.getWorkbook().write(outFile);
			outFile.close();

		} catch (Exception e) {

			e.printStackTrace();
			throw new ExcelFormatException();
		}

	}

	Map<String, String> getDataFromEventName(Entry<String, List<DateTime>> entry) {
		Map<String, String> map = new HashMap<String, String>();

		String eventData[] = ((String) entry.getKey()).split(" ");

		String actStringPart = "";
		for (String string : eventData) {

			String keyValue[] = string.split(":");
			if (keyValue != null && keyValue.length == 2) {
				map.put(keyValue[0].trim(), keyValue[1].trim());
			} else {
				if(keyValue[0].charAt(0) != '@' || keyValue[0].charAt(0) != '%'){
					actStringPart.concat(" " + keyValue[0]);
				}else{
				map.put(Character.toString(keyValue[0].trim().charAt(0)), keyValue[0].trim().substring(1, keyValue[0].length()));
				}
			}
         if( map.containsKey("ACT")){
        	 map.replace("ACT", map.get("ACT") + actStringPart);
         }
		}
		map.put("Start Date", entry.getValue().get(0).toString());
		map.put("End Date", entry.getValue().get(0).toString());

		return map;

	}

	private void setColumnsValue(Sheet sheet, int columnSize, int headerRow, Map<String, String> excelData,
			Map<String, String> propertyMap) {
		int lastColumnSize = headerRow + excelData.size();
		Row header = sheet.getRow(headerRow);
		for(Entry e : propertyMap.entrySet()) {
			System.out.println("Item : " + e.getKey() + " Count : " + e.getValue());
			for (int i = 0; i < columnSize; i++) {
				final int l = i;
				Cell hearderCall = header.getCell(i);
				if (hearderCall != null) {
					hearderCall.setCellType(CellType.STRING);
				}

				if (hearderCall != null && hearderCall.getStringCellValue().trim().equalsIgnoreCase((String) e.getValue())) {
					for (int j = headerRow + 1; j <= lastColumnSize; j++) {
						final int m = j;
						for(Entry entry : excelData.entrySet()){
							Row valueRow = sheet.getRow(j);
							if (valueRow != null) {
								Cell valueCell = valueRow.getCell(i);
								valueCell.setCellValue(excelData.get(e.getKey()));
							}
						}
					}
				}
			}
		}

	}

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
							valueCell.setCellValue(startDate.toString());
							break;

						case to:
							valueCell = row.getCell(j + 1);
							valueCell.setCellValue(endDate.toString());
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

	private String getNameAsString(List<String> clients) {
		StringBuilder b = new StringBuilder();
		clients.forEach(b::append);
		return b.toString();
	}

}
