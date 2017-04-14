package com.google.calendar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.calendar.constant.CalendarConstant;
import com.google.common.io.Files;

/**
 * Will read excel file and create sheet for Data population.
 *
 * @author DAMCO
 */
public class GenerateOutputExcel {

    private Workbook workbook;

    private Sheet sheet;

    public GenerateOutputExcel() {
	// default constructor
    }

    /**
     * Will read and generate excel file from specified path
     *
     * @param path
     *            template path to read from
     * @throws URISyntaxException
     * @throws IOException
     * @throws EncryptedDocumentException
     * @throws InvalidFormatException
     */
    public void generateExcelFile(final String path, String resultPath)
	    throws URISyntaxException, IOException, EncryptedDocumentException, InvalidFormatException {

	copyTemplateFile(path, resultPath);
	workbook = WorkbookFactory.create(new FileInputStream( CalendarConstant.RESULT_FILE_NAME.equals( resultPath ) ? CalendarConstant.DESTINATION_FILE_PATH : resultPath));
	sheet = workbook.getSheetAt(0);
    }

    /**
     * Create a copy of excel file to write data on it.
     *
     * @param path
     *            url
     * @throws URISyntaxException
     * @throws IOException
     */
    private void copyTemplateFile(final String path, String resultPath) throws URISyntaxException, IOException {
	File templateFile;
	
	if (CalendarConstant.TEMPLATE_FILE_NAME.equalsIgnoreCase(path)) {
	    final URL url = getClass().getClassLoader().getResource(CalendarConstant.TEMPLATE_FILE_NAME);
	    templateFile = new File(url.toURI());
	} else {
	    templateFile = new File(path);
	}
	
	File newFile = new File( CalendarConstant.RESULT_FILE_NAME.equals( resultPath ) ? CalendarConstant.DESTINATION_FILE_PATH : resultPath);
	Files.copy(templateFile, newFile);
    }

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