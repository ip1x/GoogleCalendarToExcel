package com.google.calendar.excel.output.impl;

import static com.google.calendar.constant.CalendarConstant.ACT;
import static com.google.calendar.constant.CalendarConstant.CLI;
import static com.google.calendar.constant.CalendarConstant.CLIENTS_HEADER;
import static com.google.calendar.constant.CalendarConstant.COMMA_SPLITTER;
import static com.google.calendar.constant.CalendarConstant.FROM_HEADER;
import static com.google.calendar.constant.CalendarConstant.PRJ;
import static com.google.calendar.constant.CalendarConstant.PROJECTS_HEADER;
import static com.google.calendar.constant.CalendarConstant.STAFF_HEADER;
import static com.google.calendar.constant.CalendarConstant.TO_HEADER;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

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
 */
public class ExcelServiceImpl implements ExcelService {

    public final Logger logger = Logger.getLogger(ExcelServiceImpl.class);

    private GenerateOutputExcel generateOutputExcel;

    @Override
    public void generateExcel(final String userName, final String templatePath, final String inOutPath,
	    final Map<String, Map<String, String>> excelData, final List<Date> dateList, final String resultPath)
	    throws ExcelFormatException {

	FileOutputStream outFile = null;
	try {

	    // Create configuration file object to create map of excel table
	    // header name
	    final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
	    final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

	    // Added excel header field which comes from event details
	    // propertyMap.put(STARTDATE, STARTDATE);
	    // propertyMap.put(ENDDATE, ENDDATE);
	    // propertyMap.put(STAFF, STAFF);
	    // propertyMap.put(WORKEDHOURS, WORKEDHOURS);

	    // Create copy of supplied excel file to populate data in excel
	    generateOutputExcel = new GenerateOutputExcel();
	    generateOutputExcel.generateExcelFile(templatePath, resultPath);

	    final Sheet sheet = generateOutputExcel.getSheet();

	    // Adding two dummy column to error free execution
	    final int columnSize = propertyMap.size() + 2;

	    // Row number of table header
	    final int headerRow = getStartHeader(sheet, columnSize, propertyMap);

	    final Map<String, Map<String, String>> eventKeyValue = new HashMap<>();
	    for (final Map.Entry<String, Map<String, String>> entry : excelData.entrySet()) {
		final Map<String, String> eventDetails = new HashMap<>();
		if (entry.getValue() == null) {
		    eventDetails.put(ACT.toLowerCase(), entry.getKey());
		    eventKeyValue.put(entry.getKey(), eventDetails);
		} else {

		    // eventDetails = getDataFromEventName(entry, userName);
		    eventKeyValue.put(entry.getKey(), entry.getValue());

		}

	    }

	    // basic details of sheet
	    setHeaderValue(sheet, getValueFromKeyAsString(eventKeyValue, CLI.toLowerCase()), userName,
		    getValueFromKeyAsString(eventKeyValue, PRJ.toLowerCase()), dateList.get(0), dateList.get(1),
		    headerRow, columnSize, propertyMap);

	    // setHeaderValue(sheet, clientNameAsString, userName,
	    // projectNameAsString, dateList.get(0), dateList.get(1),
	    // headerRow, columnSize);

	    // populate excel table with event details
	    setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

	    // generateOutputExcel.getWorkbook().getCreationHelper().createFormulaEvaluator().evaluateAll();

	    generateOutputExcel.getWorkbook().setForceFormulaRecalculation(true);

	    // update the excel with updated sheet.
	    outFile = new FileOutputStream(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
		    ? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
	    // HSSFFormulaEvaluator.evaluateAllFormulaCells(generateOutputExcel.getWorkbook());
	    generateOutputExcel.getWorkbook().write(outFile);
	    // HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
	    outFile.close();

	} catch (final Exception e) {
	    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
	    throw new ExcelFormatException();
	} finally {
	    if (outFile != null) {
		try {
		    outFile.close();
		} catch (final IOException e) {
		    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
		}
	    }

	}

    }

    // /**
    // * Will convert event name into data field.
    // *
    // * @param entry
    // * object for event name with date details
    // * @param userName
    // * google user name
    // * @return Map with key value from event name
    // */
    // @Override
    // public Map<String, String> getDataFromEventName(final Entry<String,
    // List<DateTime>> entry, final String userName) {
    // final Map<String, String> map = new HashMap<>();
    //
    // final String[] eventData = (entry.getKey()).split(" ");
    // String lastKey = "";
    // for (final String string : eventData) {
    // final String[] keyValue = string.split(COL_SPLITTER);
    // if (keyValue != null && keyValue.length == 2) {
    // map.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
    // lastKey = keyValue[0].trim().toLowerCase();
    // } else if (keyValue.length == 1) {
    // if ("".equals(keyValue[0])) {
    // continue;
    // }
    // if (keyValue[0].charAt(0) == CHAR_AT_THE_RATE || keyValue[0].charAt(0) ==
    // CHAR_MODULUS) {
    // map.put(Character.toString(keyValue[0].trim().charAt(0)),
    // keyValue[0].trim().substring(1, keyValue[0].length()));
    // } else {
    // map.replace(lastKey, map.get(lastKey).concat(" ").concat(keyValue[0]));
    // }
    // }
    // }
    //
    // final long duration = new
    // Date(entry.getValue().get(1).getValue()).getTime()
    // - new Date(entry.getValue().get(0).getValue()).getTime();
    //
    // final long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
    //
    // map.put(ENDDATE.toLowerCase(),
    // CalendarConstant.TABLE_DATE_FORMAT.format(new
    // Date(entry.getValue().get(1).getValue())));
    // map.put(STARTDATE.toLowerCase(),
    // CalendarConstant.TABLE_DATE_FORMAT.format(new
    // Date(entry.getValue().get(0).getValue())));
    // map.put(STAFF.toLowerCase(), userName);
    // // map.put(WORKEDHOURS.toLowerCase(),
    // // diffInMinutes / 60 + COL_SPLITTER + String.format("%02d",
    // // diffInMinutes % 60));
    //
    // return map;
    //
    // }

    /**
     * Will set event details in excel table
     *
     * @param sheet
     *            Excel sheet
     * @param columnSize
     *            maximum column to iterate
     * @param headerRowNo
     *            row number where header will find
     * @param excelData
     *            Event details of user
     * @param propertyMap
     *            provided configuration for excel table number
     */
    @SuppressWarnings({ "rawtypes" })
    public void setColumnsValue(final Sheet sheet, final int columnSize, final int headerRowNo,
	    final Map<String, Map<String, String>> excelData, final Map<String, String> propertyMap) {
	final int lastRowSize = headerRowNo + excelData.size();
	final Row headerRow = sheet.getRow(headerRowNo);
	for (final Entry propertyEntry : propertyMap.entrySet()) {
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {

		final Cell hearderCell = headerRow.getCell(columnIndex);
		if (hearderCell != null) {
		    hearderCell.setCellType(CellType.STRING);
		}

		if ((hearderCell != null) && hearderCell.getStringCellValue().trim()
			.equalsIgnoreCase((String) propertyEntry.getValue())) {
		    for (int rowIndex = headerRowNo + 1; rowIndex <= lastRowSize; rowIndex++) {

			for (final Entry entry : excelData.entrySet()) {
			    final Row valueRow = sheet.getRow(rowIndex++);
			    if (valueRow != null) {
				final Cell cell = valueRow.getCell(columnIndex);
				final String data = (String) ((Map) entry.getValue())
					.get(propertyEntry.getKey().toString().toLowerCase());
				if (data != null && !data.isEmpty()) {
				    cell.setCellValue((String) ((Map) entry.getValue())
					    .get(propertyEntry.getKey().toString().toLowerCase()));
				}
			    }
			}
		    }

		    // auto width setting for column of output file
		    generateOutputExcel.getSheet().autoSizeColumn(columnIndex);
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
     * @param propertyMap
     *            Map containing properties for dynamic headers of output file
     */
    private void setHeaderValue(final Sheet sheet, final String clientName, final String userName,
	    final String projectNames, final Date startDate, final Date endDate, final int headerRow,
	    final int columnSize, final Map<String, String> propertyMap) {
	for (int rowIndex = 0; rowIndex < headerRow; rowIndex++) {
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
		final Row row = sheet.getRow(rowIndex);
		if (row != null) {
		    final Cell cell = row.getCell(columnIndex);

		    if (cell != null) {
			cell.setCellType(CellType.STRING);
		    }

		    Cell valueCell;
		    if ((cell != null) && (cell.getStringCellValue() != null) && !cell.getStringCellValue().isEmpty()) {
			final String cellValue = cell.getStringCellValue().trim();
			if (cellValue.equals(propertyMap.get(STAFF_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(userName);
			} else if (cellValue.equals(propertyMap.get(FROM_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(startDate));
			} else if (cellValue.equals(propertyMap.get(TO_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(endDate));
			} else if (cellValue.equals(propertyMap.get(CLIENTS_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(clientName);
			} else if (cellValue.equals(propertyMap.get(PROJECTS_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(projectNames);
			}
			// switch (cell.getStringCellValue().trim()) {
			//
			//
			// valueCell = row.getCell(columnIndex + 1);
			// valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(endDate));
			// break;
			//
			// case propertyMap.get("ClientsHeader"):
			// valueCell = row.getCell(columnIndex + 1);
			// valueCell.setCellValue(clientName);
			// break;
			// case propertyMap.get("ProjectsHeader"):
			// valueCell = row.getCell(columnIndex + 1);
			// valueCell.setCellValue(projectNames);
			// break;
			//
			// default:
			// break;
			// }
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
     * @param inputMap
     * @return row number
     */
    private int getStartHeader(final Sheet sheet, final int columnSize, final Map<String, String> inputMap) {
	for (int rowIndex = 0; true; rowIndex++) {
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
		final Row row = sheet.getRow(rowIndex);
		if (row != null) {
		    final Cell cell = row.getCell(columnIndex);
		    final Cell nextCell = row.getCell(columnIndex + 1);
		    if ((cell != null) && (cell.getStringCellValue() != null) && !cell.getStringCellValue().isEmpty()
			    && (nextCell != null) && (nextCell.getStringCellValue() != null)
			    && inputMap.values().contains(cell.getStringCellValue().trim())
			    && inputMap.values().contains(nextCell.getStringCellValue().trim())) {
			return rowIndex;
		    }
		}
	    }
	}
    }

    /**
     * Convert event key-value list into comma separated value
     * for the "eventKey" provided
     *
     * @param keyValue
     * @param eventKey
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getValueFromKeyAsString(final Map<String, Map<String, String>> keyValue, final String eventKey) {

	final Set<String> set = new HashSet();
	for (final Entry entry : keyValue.entrySet()) {
	    set.add(((Map<String, String>) entry.getValue()).get(eventKey.trim()));
	}
	set.removeIf(Objects::isNull);
	return Joiner.on(COMMA_SPLITTER).join(set);
    }

}
