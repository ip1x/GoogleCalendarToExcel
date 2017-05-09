package com.google.calendar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.exception.ExcelFormatException;
import com.google.common.io.Files;

/**
 * Will read excel file and create sheet for Data population.
 *
 * @author DAMCO
 */
public class GenerateOutputExcel {

    private Workbook workbook;

    private Sheet sheet;

    public final Logger logger = Logger.getLogger(GenerateOutputExcel.class);

    public GenerateOutputExcel() {
	// default constructor
    }

    /**
     * Will read template file and then generate a copy of that at specified
     * path
     *
     * @param path
     *            template file path whose copy is to be created
     * @param resultPath
     *            path of the output file which will initially be the copy of
     *            template file
     * @throws ExcelFormatException
     */
    public void generateExcelFile(final String path, final String resultPath) throws ExcelFormatException {

	try {
	    copyTemplateFile(path, resultPath);
	    workbook = WorkbookFactory.create(new FileInputStream(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
		    ? CalendarConstant.DESTINATION_FILE_PATH : resultPath));
	    sheet = workbook.getSheetAt(0);
	} catch (final Exception e) {
	    logger.error(e);
	    throw new ExcelFormatException();
	}
    }

    /**
     * Create a copy of template excel file to write data on it.
     *
     * @param path
     *            template file path whose copy is to be created
     * @param resultPath
     *            path of the output file which will initially be the copy of
     *            template file
     * @throws URISyntaxException
     * @throws IOException
     */
    private void copyTemplateFile(final String path, final String resultPath) throws URISyntaxException, IOException {
	File templateFile;

	if (CalendarConstant.TEMPLATE_FILE_NAME.equalsIgnoreCase(path)) {
	    final URL url = getClass().getClassLoader().getResource(CalendarConstant.TEMPLATE_FILE_NAME);
	    templateFile = new File(url.toURI());
	} else {
	    templateFile = new File(path);
	}

	final File newFile = new File(CalendarConstant.RESULT_FILE_NAME.equals(resultPath)
		? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
	Files.copy(templateFile, newFile);
    }

    /**
     * getter
     *
     * @return
     */
    public Workbook getWorkbook() {
	return workbook;
    }

    /**
     * set workbook object
     *
     * @return
     */
    public void setWorkbook(final Workbook workbook) {
	this.workbook = workbook;
    }

    /**
     * getter
     *
     * @return
     */
    public Sheet getSheet() {
	return sheet;
    }

    /**
     * set sheet object
     *
     * @param sheet
     */
    public void setSheet(final Sheet sheet) {
	this.sheet = sheet;
    }
}