package com.google.calendar.excel.output.impl;

import static com.google.calendar.constant.CalendarConstant.ACT;
import static com.google.calendar.constant.CalendarConstant.CLI;
import static com.google.calendar.constant.CalendarConstant.CLIENTS;
import static com.google.calendar.constant.CalendarConstant.COMMA_SPLITTER;
import static com.google.calendar.constant.CalendarConstant.ENDDATE;
import static com.google.calendar.constant.CalendarConstant.FROM_HEADER;
import static com.google.calendar.constant.CalendarConstant.PRJ;
import static com.google.calendar.constant.CalendarConstant.PROJECTS;
import static com.google.calendar.constant.CalendarConstant.STAFF;
import static com.google.calendar.constant.CalendarConstant.STARTDATE;
import static com.google.calendar.constant.CalendarConstant.TO_HEADER;
import static com.google.calendar.constant.CalendarConstant.USERS;

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
     * String, java.util.List, java.util.List, java.lang.String,
     * java.lang.String, java.util.Map, java.util.Date, java.util.Date)
     */
    @Override
    public void generateExcel(final String userName, final List<String> projectName, final List<String> clientName,
	    final String templatePath, final String inOutPath, final Map<String, Map<String, String>> excelData,
	    final List<Date> dateList, final String resultPath) throws ExcelFormatException {

	FileOutputStream outFile = null;
	try {

	    // Create configuration file object to create map of excel table
	    // header name
	    final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(inOutPath);
	    final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();

	    // Added excel header field which comes from event details
	    propertyMap.put(STARTDATE, STARTDATE);
	    propertyMap.put(ENDDATE, ENDDATE);
	    propertyMap.put(STAFF, STAFF);
	    // propertyMap.put(WORKEDHOURS, WORKEDHOURS);

	    // Create copy of supplied excel file to populate data in excel
	    generateOutputExcel = new GenerateOutputExcel();
	    generateOutputExcel.generateExcelFile(templatePath, resultPath);

	    final Sheet sheet = generateOutputExcel.getSheet();

	    // Adding two dummy column to error free execution
	    final int columnSize = propertyMap.size() + 2;

	    // Row number of table header
	    final int headerRow = getStartHeader(sheet, columnSize, propertyMap);

	    final Map<String, Map<String, String>> eventKeyValue = new HashMap<String, Map<String, String>>();
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
		    headerRow, columnSize);

	    // populate excel table with event details
	    setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

	    // generateOutputExcel.getWorkbook().getCreationHelper().createFormulaEvaluator().evaluateAll();

	    generateOutputExcel.getWorkbook().setForceFormulaRecalculation(true);

	    // auto width setting for column of output file
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
		generateOutputExcel.getSheet().autoSizeColumn(columnIndex);
	    }

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
     * @param headerRow
     *            row number where header will find
     * @param excelData
     *            Event details of user
     * @param propertyMap
     *            provided configuration for excel table number
     */
    public void setColumnsValue(final Sheet sheet, final int columnSize, final int headerRowNo,
	    final Map<String, Map<String, String>> excelData, final Map<String, String> propertyMap) {
	final int lastRowSize = headerRowNo + excelData.size();
	final Row headerRow = sheet.getRow(headerRowNo);
	for (final Entry propertyEntry : propertyMap.entrySet()) {
	    for (int i = 0; i < columnSize; i++) {

		final Cell hearderCell = headerRow.getCell(i);
		if (hearderCell != null) {
		    hearderCell.setCellType(CellType.STRING);
		}

		if (hearderCell != null && hearderCell.getStringCellValue().trim()
			.equalsIgnoreCase((String) propertyEntry.getValue())) {
		    for (int j = headerRowNo + 1; j <= lastRowSize; j++) {

			for (final Entry entry : excelData.entrySet()) {
			    final Row valueRow = sheet.getRow(j++);
			    if (valueRow != null) {
				final Cell valueCell = valueRow.getCell(i);
				valueCell.setCellValue((String) ((Map) entry.getValue())
					.get(propertyEntry.getKey().toString().toLowerCase()));
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
	    final String projectNames, final Date startDate, final Date endDate, final int headerRow,
	    final int columnSize) {
	for (int i = 0; i < headerRow; i++) {
	    for (int j = 0; j < columnSize; j++) {
		final Row row = sheet.getRow(i);
		if (row != null) {
		    final Cell cell = row.getCell(j);

		    if (cell != null) {
			cell.setCellType(CellType.STRING);
		    }

		    Cell valueCell;
		    if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
			switch (cell.getStringCellValue().trim()) {
			    case USERS:
				valueCell = row.getCell(j + 1);
				valueCell.setCellValue(userName);
				break;

			    case FROM_HEADER:
				valueCell = row.getCell(j + 1);
				valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(startDate));
				break;

			    case TO_HEADER:
				valueCell = row.getCell(j + 1);
				valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(endDate));
				break;

			    case CLIENTS:
				valueCell = row.getCell(j + 1);
				valueCell.setCellValue(clientName);
				break;
			    case PROJECTS:
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
    private int getStartHeader(final Sheet sheet, final int columnSize, final Map<String, String> inputMap) {
	for (int i = 0; true; i++) {
	    for (int j = 0; j < columnSize; j++) {
		final Row row = sheet.getRow(i);
		if (row != null) {
		    final Cell cell = row.getCell(j);
		    if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()
			    && inputMap.values().contains(cell.getStringCellValue().trim())) {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getValueFromKeyAsString(final Map<String, Map<String, String>> keyValue, final String name) {

	final Set<String> set = new HashSet();
	for (final Entry entry : keyValue.entrySet()) {
	    set.add(((Map<String, String>) entry.getValue()).get(name.trim()));
	}
	set.removeIf(Objects::isNull);
	return Joiner.on(COMMA_SPLITTER).join(set);
    }

}
