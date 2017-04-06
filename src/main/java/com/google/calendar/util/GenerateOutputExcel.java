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
     * @param path template path to read from
     * @throws URISyntaxException
     * @throws IOException
     * @throws EncryptedDocumentException
     * @throws InvalidFormatException
     */
    public void generateExcelFile(final String path) throws URISyntaxException,
            IOException, EncryptedDocumentException, InvalidFormatException {

        copyTemplateFile(path);
        workbook = WorkbookFactory.create(
                new FileInputStream(CalendarConstant.DESTINATION_FILE_PATH));
        sheet = workbook.getSheetAt(0);
    }

    /**
     * Create a copy of excel file to write data on it.
     *
     * @param path url
     * @throws URISyntaxException
     * @throws IOException
     */
    private void copyTemplateFile(final String path)
            throws URISyntaxException, IOException {
        File templateFile;
        if ("Timesheet.xls".equalsIgnoreCase(path)) {
            final URL url =
                    getClass().getClassLoader().getResource("Timesheet.xls");
            templateFile = new File(url.toURI());
        } else {
            templateFile = new File(path);
        }
        final File newFile = new File(CalendarConstant.DESTINATION_FILE_PATH);
        Files.copy(templateFile, newFile);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(final Workbook workbook) {
        this.workbook = workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(final Sheet sheet) {
        this.sheet = sheet;
    }
}