package com.google.calendar.output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.calendar.util.ConfigurationFileParser;
import com.google.calendar.util.ExcelFormat;

public class Main {

	private static String FILE_PATH = "Z:/calendar.xlsx";

	public static void main(final String[] args) throws IOException {

		final GenerateOutputExcel excelOutput = new GenerateOutputExcel();
		final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		configurationFileParser.loadPropertyFile();
		excelOutput.getSheet().setDisplayGridlines(false);
		setTitleOfSheet(excelOutput, configurationFileParser);
		createStaffAndClientRow(excelOutput,configurationFileParser);
		createFromAndProjectsRow(excelOutput, configurationFileParser);
		createToRow(excelOutput, configurationFileParser);

		try {
			final FileOutputStream outputStream = new FileOutputStream(FILE_PATH);
			excelOutput.getWorkbook().write(outputStream);
			excelOutput.getWorkbook().close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}
	private static void setTitleOfSheet(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row4 = excelOutput.getSheet().createRow(0);
		final Cell staff = row4.createCell(0);
		staff.setCellValue(configurationFileParser.getPropertyByKey("Title"));
		staff.setCellStyle(ExcelFormat.getHeightAndBoldCellType(excelOutput.getWorkbook()));
	}

	private static void createStaffAndClientRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row4 = excelOutput.getSheet().createRow(3);
		final Cell staff = row4.createCell(0);
		staff.setCellValue("Staff");
		staff.setCellStyle(ExcelFormat.getRightAllignAndBoldCellType(excelOutput.getWorkbook()));

		final Cell staffValue = row4.createCell(1);
		staffValue.setCellValue("Rohit");
		staffValue.setCellStyle(ExcelFormat.getLeftAllignCellType(excelOutput.getWorkbook()));

		final Cell client = row4.createCell(5);
		client.setCellValue("Client");
		client.setCellStyle(ExcelFormat.getRightAllignAndBoldCellType(excelOutput.getWorkbook()));

		final Cell clientValue = row4.createCell(6);
		clientValue.setCellValue("damco");
		clientValue.setCellStyle(ExcelFormat.getLeftAllignCellType(excelOutput.getWorkbook()));
	}

	private static void createFromAndProjectsRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row5 = excelOutput.getSheet().createRow(4);
		final Cell from = row5.createCell(0);
		from.setCellValue("From");
		from.setCellStyle(ExcelFormat.getRightAllignAndBoldCellType(excelOutput.getWorkbook()));

		final Cell fromValue = row5.createCell(1);
		fromValue.setCellValue("12/01/1993");
		fromValue.setCellStyle(ExcelFormat.getLeftAllignAndDateCellType(excelOutput.getWorkbook()));

		final Cell projects = row5.createCell(5);
		projects.setCellValue("Projects");
		projects.setCellStyle(ExcelFormat.getRightAllignAndBoldCellType(excelOutput.getWorkbook()));

		final Cell projectsValue = row5.createCell(6);
		projectsValue.setCellValue("GSI");
		projectsValue.setCellStyle(ExcelFormat.getLeftAllignCellType(excelOutput.getWorkbook()));
	}

	private static void createToRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row6 = excelOutput.getSheet().createRow(5);
		final Cell to = row6.createCell(0);
		to.setCellValue("To");
		to.setCellStyle(ExcelFormat.getRightAllignAndBoldCellType(excelOutput.getWorkbook()));

		final Cell toValue = row6.createCell(1);
		toValue.setCellValue("01/02/1995");
		toValue.setCellStyle(ExcelFormat.getLeftAllignAndDateCellType(excelOutput.getWorkbook()));
	}
}
