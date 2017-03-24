package com.google.calendar.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFormat {

	public static CellStyle getBoldCellStyle(final XSSFWorkbook workBook) {
		final XSSFCellStyle cellStyle = workBook.createCellStyle();
		final XSSFFont font = workBook.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public static CellStyle getDateCellType(final XSSFWorkbook workBook) {
		final XSSFCellStyle cellStyle = workBook.createCellStyle();
		final CreationHelper createHelper = workBook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		return cellStyle;
	}

	public static CellStyle getLeftAllignCellType(final XSSFWorkbook workBook) {
		final XSSFCellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		return cellStyle;
	}

	public static CellStyle getRightAllignCellType(final XSSFWorkbook workBook) {
		final XSSFCellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		return cellStyle;
	}

	public static CellStyle getRightAllignAndBoldCellType(final XSSFWorkbook workBook) {
		final CellStyle cellStyle = getBoldCellStyle(workBook);
		cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		return cellStyle;
	}

	public static CellStyle getLeftAllignAndDateCellType(final XSSFWorkbook workBook) {
		final CellStyle cellStyle = getDateCellType(workBook);
		cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		return cellStyle;
	}

	public static CellStyle getHeightAndBoldCellType(final XSSFWorkbook workBook) {
		final XSSFCellStyle cellStyle = (XSSFCellStyle) getBoldCellStyle(workBook);
		cellStyle.getFont().setFontHeight(20);
		return cellStyle;
	}
}
