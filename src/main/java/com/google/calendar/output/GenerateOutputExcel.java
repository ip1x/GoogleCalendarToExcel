package com.google.calendar.output;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateOutputExcel {

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	public GenerateOutputExcel() {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Google calendar");
	}

	public XSSFWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(final XSSFWorkbook workbook) {
		this.workbook = workbook;
	}

	public XSSFSheet getSheet() {
		return sheet;
	}

	public void setSheet(final XSSFSheet sheet) {
		this.sheet = sheet;
	}
}
