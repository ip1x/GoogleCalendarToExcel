package com.google.calendar.excel.output.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.google.api.client.util.DateTime;
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
			String templatePath, String inOutPath, Map<String, List<DateTime>> excelData,
			Date startDate, Date endDate) throws ExcelFormatException{

		try {
			GenerateOutputExcel generateOutputExcel = new GenerateOutputExcel();
			ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
			Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

			generateOutputExcel.generateExcelFile(templatePath);

			Sheet sheet = generateOutputExcel.getSheet();

			int columnSize = propertyMap.size() + 2;
			int headerRow = getStartHeader(sheet, columnSize);	
			//setHeaderValue
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new ExcelFormatException();
		} 

	


	}

	Map<String, String> getDataFromEventName(Entry<String, String> entry) {
		Map<String, String> map = new HashMap<String, String>();

		String eventData[] = ((String) entry.getKey()).split(" ");

		for (String string : eventData) {

			String keyValue[] = string.split(":");
			if (keyValue != null && keyValue.length == 2) {
				map.put(keyValue[0], keyValue[1]);
			} else {
				map.put(Character.toString(keyValue[0].charAt(0)), keyValue[0].substring(1, keyValue[0].length()));
			}

		}
		return map;

	}

	private void setColumnsValue(Sheet sheet, int columnSize, int headerRow, Map<String, String> excelData,
			Map<String, String> propertyMap) {
		int lastColumnSize = headerRow + excelData.size();
		Row header = sheet.getRow(headerRow);
		propertyMap.forEach((k, v) -> {
			System.out.println("Item : " + k + " Count : " + v);
			for (int i = 0; i < columnSize; i++) {
				final int l = i;
				Cell hearderCall = header.getCell(i);
				if (hearderCall.getStringCellValue().trim().equals(v)) {
					for (int j = headerRow + 1; j <= lastColumnSize; j++) {
						final int m = j;
						excelData.forEach((event, list) -> {
							Row valueRow = sheet.getRow(l);
							if (valueRow != null) {
								Cell valueCell = valueRow.getCell(m);
								valueCell.setCellValue(excelData.get(k));
							}
						});
					}
				}
			}
		});

	}

	private void setHeaderValue(final Sheet sheet, final String clientName, final String userName,
			final String projectNames, final Date startDate, final Date endDate, int headerRow, int columnSize) {
		for (int i = 0; i < headerRow; i++) {
			for (int j = 0; j < columnSize; j++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					Cell cell = row.getCell(j);
					if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
						switch (cell.getStringCellValue().trim()) {
						case staff:
							Cell valueCell = row.getCell(j + 1);
							valueCell.setCellValue(userName);
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

	

}
