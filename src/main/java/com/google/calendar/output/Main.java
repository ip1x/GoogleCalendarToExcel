package com.google.calendar.output;

import java.io.FileOutputStream;
import java.util.ArrayList;
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
import com.google.calendar.excel.output.impl.ExcelServiceImpl;
import com.google.calendar.util.ConfigurationFileParser;
import com.google.calendar.util.GenerateOutputExcel;

public class Main {

	public static void main(final String[] args) {
		try {
			
			
			GenerateOutputExcel generateOutputExcel = new GenerateOutputExcel();
			ConfigurationFileParser configurationFileParser = new ConfigurationFileParser("configuration.properties");
			Map<String, String> propertyMap = configurationFileParser.getPropertyMap();
			
			propertyMap.put("Started on", "Started on");
			propertyMap.put("Ended on", "Ended on");
			propertyMap.put("Staff", "Staff");
			propertyMap.put("Worked Hours", "Worked Hours");
			
			

			String eventName1 = "PRJ:POC @Faridabad WBS:www.damco.com %80 CLI:DAMCO TKT:12345 ACT:staff meeting at sheem";
			
			String eventName2 = "PRJ:POC1 @Faridabad1 %801 CLI:DAMCO1 TKT:13345 ACT:staff meeting at Gulmohar WBS:www.damco.com1";
			
			List l1 = new ArrayList<Date>(); 
			l1.add(new DateTime(new Date()));
			l1.add(new DateTime(new Date()));
			
			Map<String, List<DateTime>> excelData = new HashMap<String, List<DateTime>>();
			excelData.put(eventName1, l1);
			excelData.put(eventName2, l1);
			
			Map<String, Map<String , String>> eventKeyValue = new HashMap<String, Map<String,String>>();
			
			for (Map.Entry<String, List<DateTime>> entry : excelData.entrySet()) {

				Map<String , String> eventKey= getDataFromEventName(entry,"Hemant kuamr");
				eventKeyValue.put(entry.getKey(), eventKey);
				//setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

			}
			
			generateOutputExcel.generateExcelFile("Timesheet.xls");
			Sheet sheet = generateOutputExcel.getSheet();
			ExcelServiceImpl  service = new ExcelServiceImpl();

			int columnSize = propertyMap.size() + 4;
			int headerRow = service.getStartHeader(sheet, columnSize);
			// setHeaderValue

			service.setHeaderValue(sheet, "DAMCO", "Hemant kumar", "POC", new Date(),
					new Date(), headerRow, columnSize);
			
			
			setColumnsValue(sheet,columnSize,headerRow,eventKeyValue,propertyMap);
			
			
			FileOutputStream outFile = new FileOutputStream("/Users/Hemantkumar/Desktop/Java.xls");
			
			generateOutputExcel.getWorkbook().write(outFile);
			outFile.close();
			

		
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}


	public static void setColumnsValue(Sheet sheet, int columnSize, int headerRowNo, Map<String, Map<String , String>> excelData,
			Map<String, String> propertyMap) {
		int lastColumnSize = headerRowNo + excelData.size();
		Row headerRow = sheet.getRow(headerRowNo);
		for(Entry propertyEntry : propertyMap.entrySet()) {
			System.out.println("Item : " + propertyEntry.getKey() + " Count : " + propertyEntry.getValue());
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
	
	static Map<String, String> getDataFromEventName(Entry<String, List<DateTime>> entry, String userName) {
		Map<String, String> map = new HashMap<String, String>();

		String eventData[] = ((String) entry.getKey()).split(" ");

		StringBuffer actStringPart = new StringBuffer("");
		for (String string : eventData) {

			String keyValue[] = string.split(":");
			if (keyValue != null && keyValue.length == 2) {
				map.put(keyValue[0].trim(), keyValue[1].trim());
			} else {
				if(keyValue[0].charAt(0) != '@' && keyValue[0].charAt(0) != '%'){
					actStringPart.append(" " + keyValue[0]);
				}else{
				map.put(Character.toString(keyValue[0].trim().charAt(0)), keyValue[0].trim().substring(1, keyValue[0].length()));
				}
			}
         if( map.containsKey("ACT")){
        	 map.replace("ACT", map.get("ACT") + actStringPart);
         }
		}
		
		System.out.println(CalendarConstant.df.format(new Date()));
		map.put("Ended on", CalendarConstant.df.format(new Date(entry.getValue().get(0).getValue())));
		map.put("Started on", CalendarConstant.df.format(new Date(entry.getValue().get(0).getValue())));
		map.put("Staff", userName);

		return map;

	}
	
}