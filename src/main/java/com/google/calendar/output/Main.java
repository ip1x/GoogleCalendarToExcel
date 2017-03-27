package com.google.calendar.output;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.calendar.util.ConfigurationFileParser;

public class Main {

	public static void main(final String[] args) {
		try {
			final GenerateOutputExcel excelOutput = new GenerateOutputExcel();
			excelOutput.generateExcelFile();

			final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
			configurationFileParser.loadPropertyFile();

			createStaffAndClientRow(excelOutput,configurationFileParser);
			createFromAndProjectsRow(excelOutput, configurationFileParser);
			createToRow(excelOutput, configurationFileParser);

			final FileOutputStream outputStream = new FileOutputStream(GenerateOutputExcel.DESTINATION_FILE_PATH);
			excelOutput.getWorkbook().write(outputStream);
			excelOutput.getWorkbook().close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}


	private static void createStaffAndClientRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row4 = excelOutput.getSheet().getRow(3);

		final Cell staffValue = row4.getCell(1);
		staffValue.setCellValue("Rohit");

		final Cell clientValue = row4.getCell(6);
		clientValue.setCellValue("damco");
	}

	private static void createFromAndProjectsRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row5 = excelOutput.getSheet().getRow(4);

		final Cell fromValue = row5.getCell(1);
		fromValue.setCellValue("12/01/1993");

		final Cell projectsValue = row5.getCell(6);
		projectsValue.setCellValue("GSI");
	}

	private static void createToRow(final GenerateOutputExcel excelOutput, final ConfigurationFileParser configurationFileParser) {
		final Row row6 = excelOutput.getSheet().getRow(5);

		final Cell toValue = row6.getCell(1);
		toValue.setCellValue("01/02/1995");
	}
}
