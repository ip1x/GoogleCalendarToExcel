package com.google.calendar.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.io.Files;

public class GenerateOutputExcel {

	private Workbook workbook;
	private Sheet sheet;
	private static String DESTINATION_FILE_PATH = "Z:/calendar.xlsx";

	public GenerateOutputExcel() {
	}

	public void generateExcelFile() throws Exception {
		copyTemplateFile();
		workbook = WorkbookFactory.create(new FileInputStream(DESTINATION_FILE_PATH));
		sheet = workbook.getSheetAt(0);
	}

	private void copyTemplateFile() throws URISyntaxException, IOException {
		final File newFile = new File(DESTINATION_FILE_PATH);
		final URL url = Main.class.getClassLoader().getResource("template.xls");
		final File templateFile = new File(url.toURI());
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
