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

		    eventKeyValue.put(entry.getKey(), entry.getValue());

		}

	    }

	    // basic details of sheet
	    setHeaderValue(getValueFromKeyAsString(eventKeyValue, CalendarConstant.CLI_LOWER_CASE),
		    getValueFromKeyAsString(eventKeyValue, CalendarConstant.PRJ_LOWER_CASE),
		    getValueFromKeyAsString(eventKeyValue, CalendarConstant.STF_LOWER_CASE), dateList, headerRow,
		    columnSize, propertyMap);

	    // populate excel table with event details
	    setColumnsValue(sheet, columnSize, headerRow, eventKeyValue, propertyMap);

	    // Used to calculate "Worked Hours" on the basis of formula set in
	    // "Template" file
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
     * This method will take one TAG of Event title from created MAP and search
     * it with ROW and COLUMN, if the selected TAG is find, then it starts
     * populating all Event values for that TAG in that Column. This is
     * basically done for TABLE of OUTPUT file.
     *
     * @param sheet
     *            Excel sheet in which data has to be populated
     * @param columnSize
     *            maximum column number to be iterated for populating data
     * @param headerRowNo
     *            row number at which header of TABLE is occurring
     * @param excelData
     *            Map of Event containing "Event summary" as KEY and Event
     *            KEY-VALUE pair as VALUE.
     * @param propertyMap
     *            IN-OUT MAP containing data regarding OUTPUT file table.
     */
    @SuppressWarnings("rawtypes")
    public void setColumnsValue(final Sheet sheet, final int columnSize, final int headerRowNo,
	    final Map<String, Map<String, String>> excelData, final Map<String, String> propertyMap) {
	// Maximum row to be parsed for populating data as it cann't be greater
	// than "Table Header Row number" + "Number of Events"
	final int lastRowSize = headerRowNo + excelData.size();
	final Row headerRow = sheet.getRow(headerRowNo);
	// Parses all Configuration MAP data so that selected Column can be
	// populated in the output file.
	for (final Entry propertyEntry : propertyMap.entrySet()) {
	    // Parses Columns for data
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {

		final Cell hearderCell = headerRow.getCell(columnIndex);
		if (hearderCell != null) {
		    hearderCell.setCellType(CellType.STRING);
		}

		if ((hearderCell != null) && hearderCell.getStringCellValue().trim()
			.equalsIgnoreCase((String) propertyEntry.getValue())) {
		    // If The "Column" CELL contains the VALUE equal to
		    // IN-OUT MAP's value, that means one column has been
		    // found where we can populate the data.
		    populateTableColumns(sheet, headerRowNo, excelData, lastRowSize, propertyEntry, columnIndex);
		}
	    }
	}

    }

    /**
     * This method is used to populate Data to OUTPUT TABLE.
     * When a particular column of TABLE of OUTPUT file is found to be
     * populated, then all events are parsed and populate their data to the
     * respective column
     *
     * @param sheet
     *            Excel sheet in which data has to be populated
     * @param headerRowNo
     *            row number at which header of TABLE is occurring
     * @param excelData
     *            Map of Event containing "Event summary" as KEY and Event
     *            KEY-VALUE pair as VALUE.
     * @param lastRowSize
     *            Last row number till which data can be populated.
     * @param propertyEntry
     *            Individual element of Configuration IN-OUT Map.
     * @param columnIndex
     *            Column index at which data has been found.
     */
    @SuppressWarnings("rawtypes")
    private void populateTableColumns(final Sheet sheet, final int headerRowNo,
	    final Map<String, Map<String, String>> excelData, final int lastRowSize, final Entry propertyEntry,
	    final int columnIndex) {
	for (int rowIndex = headerRowNo + 1; rowIndex <= lastRowSize; rowIndex++) {

	    // Parses Each row of the particular column for populating
	    // particular TAG value from each Event.
	    for (final Entry entry : excelData.entrySet()) {
		final Row valueRow = sheet.getRow(rowIndex++);
		if (valueRow != null) {
		    // Cell is fetched
		    final Cell cell = valueRow.getCell(columnIndex);
		    // data for that particular TAG from MAP is fetched
		    final String data = (String) ((Map) entry.getValue())
			    .get(propertyEntry.getKey().toString().toLowerCase());
		    if (data != null && !data.isEmpty()) {
			// If data is not NULL and is NOT EMPTY, then value is
			// set to the CELL
			cell.setCellValue(
				(String) ((Map) entry.getValue()).get(propertyEntry.getKey().toString().toLowerCase()));
		    }
		}
	    }
	}

	// auto width setting for column of output file
	generateOutputExcel.getSheet().autoSizeColumn(columnIndex);
    }

    /**
     * This method traverses all ROW and COLUMN to find the respective LABEL
     * whose value is to be populated by Event Values
     *
     * @param clientName
     *            Name of Client to be populated in HEADER portion of OUTPUT
     *            file for Label "Clients"
     * @param projectNames
     *            Name of Projects to be populated in HEADER portion of OUTPUT
     *            file for Label "Projects"
     * @param calenderName
     *            Name of Calendars to be populated in HEADER portion of OUTPUT
     *            file for Label "Staff"
     * @param dateList
     *            List of date containing "Start date" as first element and
     *            "End date" as Second element
     * @param headerRow
     *            row number at which header of TABLE is occurring
     * @param columnSize
     *            maximum column to iterate for populating Events
     * @param propertyMap
     *            IN-OUT MAP containing data regarding OUTPUT file.
     */
    private void setHeaderValue(final String clientName, final String projectNames, final String calenderName,
	    final List<Date> dateList, final int headerRow, final int columnSize,
	    final Map<String, String> propertyMap) {
	for (int rowIndex = 0; rowIndex < headerRow; rowIndex++) {
	    // Parses Each row of the particular column for populating data for
	    // each LABEL
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
		// Parses Each column of the particular row for populating data
		// for each LABEL
		final Row row = generateOutputExcel.getSheet().getRow(rowIndex);
		if (row != null) {
		    // if row exists
		    populateFieldsAboveTable(clientName, projectNames, calenderName, dateList, propertyMap, columnIndex,
			    row);
		}
	    }
	}
    }

    /**
     * This method is used to populate the HEADER fields of OUTPUT file Which is
     * ABOVE TABLE. All values that are required to populate is passed in the
     * Arguments.
     *
     * @param clientName
     *            Name of Client to be populated in HEADER portion of OUTPUT
     *            file for Label "Clients"
     * @param projectNames
     *            Name of Projects to be populated in HEADER portion of OUTPUT
     *            file for Label "Projects"
     * @param calenderName
     *            Name of Calendars to be populated in HEADER portion of OUTPUT
     *            file for Label "Staff"
     * @param dateList
     *            List of date containing "Start date" as first element and
     *            "End date" as Second element
     * @param propertyMap
     *            IN-OUT MAP containing data regarding OUTPUT file table.
     * @param columnIndex
     *            index of column at which data is found for a particular LABEL
     * @param row
     *            Row at which data is found for a particular LABEL.
     */
    private void populateFieldsAboveTable(final String clientName, final String projectNames, final String calenderName,
	    final List<Date> dateList, final Map<String, String> propertyMap, final int columnIndex, final Row row) {
	final Cell cell = row.getCell(columnIndex);

	if (cell != null) {
	    cell.setCellType(CellType.STRING);
	}

	Cell valueCell;
	if ((cell != null) && (cell.getStringCellValue() != null) && !cell.getStringCellValue().isEmpty()) {

	    // If Cell value is NOT NULL and is IS NOT EMPTY
	    final String cellValue = cell.getStringCellValue().trim();
	    if (cellValue.equals(propertyMap.get(CalendarConstant.STAFF_HEADER))) {
		// If LABEL is equal to VALUE of "StaffHeader" KEY, put its
		// value to next cell of output file
		valueCell = row.getCell(columnIndex + 1);
		valueCell.setCellValue(calenderName);
	    } else if (cellValue.equals(propertyMap.get(CalendarConstant.FROM_HEADER))) {
		// If LABEL is equal to VALUE of "FromHeader" KEY, put its value
		// to next cell of output file
		valueCell = row.getCell(columnIndex + 1);
		valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(dateList.get(0)));
	    } else if (cellValue.equals(propertyMap.get(CalendarConstant.TO_HEADER))) {
		// If LABEL is equal to VALUE of "ToHeader" KEY, put its value
		// to next cell of output file
		valueCell = row.getCell(columnIndex + 1);
		valueCell.setCellValue(CalendarConstant.EXCEL_HEADER_DATE_FORMAT.format(dateList.get(1)));
	    } else if (cellValue.equals(propertyMap.get(CalendarConstant.CLIENTS_HEADER))) {
		// If LABEL is equal to VALUE of "ClientsHeader" KEY, put its
		// value to next cell of output file
		valueCell = row.getCell(columnIndex + 1);
		valueCell.setCellValue(clientName);
	    } else if (cellValue.equals(propertyMap.get(CalendarConstant.PROJECTS_HEADER))) {
		// If LABEL is equal to VALUE of "ProjectsHeader" KEY, put its
		// value to next cell of output file
		valueCell = row.getCell(columnIndex + 1);
		valueCell.setCellValue(projectNames);
	    }
	}
    }

    /**
     * This method is used to find the ROW number from where HEADERS of the
     * Table of OUTPUT file starts
     *
     * @param sheet
     *            Excel sheet in which data has to be populated
     * @param columnSize
     *            maximum column to iterate for populating Events till which we
     *            have to ITERATE
     * @param inputMap
     *            IN-OUT MAP containing data regarding OUTPUT file.
     * @return row number Index of ROW from where HEADERS of the Table of OUTPUT
     *         file starts
     */
    private int getStartHeader(final Sheet sheet, final int columnSize, final Map<String, String> inputMap) {
	for (int rowIndex = 0; rowIndex < 40; rowIndex++) {
	    // Iterate ROW till "40" rows.
	    for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
		// Iterate COLUMN till "columnSize" column.
		final Row row = sheet.getRow(rowIndex);
		if (row != null) {
		    final Cell cell = row.getCell(columnIndex);
		    final Cell nextCell = row.getCell(columnIndex + 1);
		    // Checks the CELL value for NULL and EMPTY
		    final boolean isCellNull = (cell != null) && (cell.getStringCellValue() != null)
			    && !cell.getStringCellValue().isEmpty();
		    // Checks the next CELL value for NULL
		    final boolean isNextCellNull = (nextCell != null) && (nextCell.getStringCellValue() != null);
		    if (isCellNull && isNextCellNull && inputMap.values().contains(cell.getStringCellValue().trim())
			    && inputMap.values().contains(nextCell.getStringCellValue().trim())) {
			// If current and Next cell are NOT EMPTY and contains
			// data which also is contained in the IN-OUT MAP for
			// output file, then that row is our desired row
			return rowIndex;
		    }
		}
	    }
	}
	return 40;
    }

    /**
     * Map of Event containing "Event summary" as KEY and Event
     * KEY-VALUE pair as VALUE are parsed for the given "TAG" and when data is
     * available for the given TAG, then they are concatenated by "," and are
     * stored in a different String
     *
     * @param keyValue
     *            Map of Event containing "Event summary" as KEY and Event
     *            KEY-VALUE pair as VALUE.
     * @param eventKey
     *            TAG of Event for which VALUES are to be fetched and are to be
     *            concatenated by ","
     * @return String of VALUES of Event values separated by ","
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getValueFromKeyAsString(final Map<String, Map<String, String>> keyValue, final String eventKey) {

	final Set<String> set = new HashSet();
	for (final Entry entry : keyValue.entrySet()) {
	    // Parses EACH eventMap VALUE for the given TAG.
	    set.add(((Map<String, String>) entry.getValue()).get(eventKey.trim()));
	    // If found then, its VALUE is added to the SET of String
	}
	set.removeIf(Objects::isNull);
	// values of SET is concatenated by "," and is returned
	return Joiner.on(CalendarConstant.COMMA_SPLITTER).join(set);
    }

}
