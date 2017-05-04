package com.google.calendar.excel.output.impl;

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

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.calendar.excel.output.ExcelService#generateExcel(java.lang.
     * String, java.lang.String, java.util.Map, java.util.List,
     * java.lang.String)
     */
    @Override
    public void generateExcel(final String templatePath, final String inOutPath,
	    final Map<String, Map<String, String>> excelData, final List<Date> dateList, final String resultPath)
	    throws ExcelFormatException {

	FileOutputStream outFile = null;
	try {

	    // Create configuration file object to create map of excel table
	    // header name
	    final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
	    final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

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
		    eventDetails.put(CalendarConstant.ACT_LOWER_CASE, entry.getKey());
		    eventKeyValue.put(entry.getKey(), eventDetails);
		} else {

		    // eventDetails = getDataFromEventName(entry, userName);
		    eventKeyValue.put(entry.getKey(), entry.getValue());

		}

	    }

	    // basic details of sheet
	    setHeaderValue(sheet, getValueFromKeyAsString(eventKeyValue, CalendarConstant.CLI_LOWER_CASE),
		    getValueFromKeyAsString(eventKeyValue, CalendarConstant.PRJ_LOWER_CASE),
		    getValueFromKeyAsString(eventKeyValue, CalendarConstant.STF_LOWER_CASE), dateList, headerRow,
		    columnSize, propertyMap);

	    // populate excel table with event details
	    setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

	    generateOutputExcel.getWorkbook().setForceFormulaRecalculation(true);

	    // update the excel with updated sheet.
	    outFile = new FileOutputStream(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
		    ? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
	    generateOutputExcel.getWorkbook().write(outFile);
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
     * @param projectNames
     *            name of project
     * @param calenderName
     * @param dateList
     *
     * @param headerRow
     *            max row to iterate
     * @param columnSize
     *            maximum column to iterate
     * @param propertyMap
     *            Map containing properties for dynamic headers of output file
     */
    private void setHeaderValue(final Sheet sheet, final String clientName, final String projectNames,
	    final String calenderName, final List<Date> dateList, final int headerRow, final int columnSize,
	    final Map<String, String> propertyMap) {
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
			if (cellValue.equals(propertyMap.get(CalendarConstant.STAFF_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(calenderName);
			} else if (cellValue.equals(propertyMap.get(CalendarConstant.FROM_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(dateList.get(0)));
			} else if (cellValue.equals(propertyMap.get(CalendarConstant.TO_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(dateList.get(1)));
			} else if (cellValue.equals(propertyMap.get(CalendarConstant.CLIENTS_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(clientName);
			} else if (cellValue.equals(propertyMap.get(CalendarConstant.PROJECTS_HEADER))) {
			    valueCell = row.getCell(columnIndex + 1);
			    valueCell.setCellValue(projectNames);
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
	return Joiner.on(CalendarConstant.COMMA_SPLITTER).join(set);
    }

}
