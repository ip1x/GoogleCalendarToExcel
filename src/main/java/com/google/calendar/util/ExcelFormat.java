package com.google.calendar.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelFormat {

	public static CellStyle getBoldCellStyle(final Workbook workBook) {
		final CellStyle cellStyle = workBook.createCellStyle();
		final Font font = workBook.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	public static CellStyle getDateCellType(final Workbook workBook) {
		final CellStyle cellStyle = workBook.createCellStyle();
		final CreationHelper createHelper = workBook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		return cellStyle;
	}

	public static CellStyle getLeftAllignCellType(final Workbook workBook) {
		final CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		return cellStyle;
	}

	public static CellStyle getRightAllignCellType(final Workbook workBook) {
		final CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		return cellStyle;
	}

	public static CellStyle getRightAllignAndBoldCellType(final Workbook workBook) {
		final CellStyle cellStyle = getBoldCellStyle(workBook);
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		return cellStyle;
	}

	public static CellStyle getLeftAllignAndDateCellType(final Workbook workBook) {
		final CellStyle cellStyle = getDateCellType(workBook);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		return cellStyle;
	}

	//	public static CellStyle getHeightAndBoldCellType(final Workbook workBook) {
	//		final CellStyle cellStyle = getBoldCellStyle(workBook);
	//		cellStyle.getFont().setFontHeight(20);
	//		return cellStyle;
	//	}
}
