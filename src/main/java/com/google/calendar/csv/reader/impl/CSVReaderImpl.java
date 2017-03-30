package com.google.calendar.csv.reader.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.csv.reader.CSVReader;

/**
 * This class will used to read the csv file and convert the csv file data into
 * map object
 * 
 * @author DAMCO
 *
 */
public class CSVReaderImpl implements CSVReader {



	/**
	 * 
	 * Return Map of input parameter taken from CSV file
	 * 
	 * @param request
	 *            HttpServletRequest object with form parameter
	 * @return Map of input parameter
	 */
	public Map<String, String> readCSV(final HttpServletRequest request) {

		DiskFileItemFactory factory = new DiskFileItemFactory();		
		factory.setSizeThreshold(CalendarConstant.MAXMEMSIZE);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(CalendarConstant.TEMP_STORAGE_LOCATION));		
		ServletFileUpload upload = new ServletFileUpload(factory);		
		upload.setSizeMax(CalendarConstant.MAXFILESIZE);

		// Create input from CSV

		BufferedReader bufferReader = null;
		String line = "";

		String lastKey = "";
		Map<String, String> inputMap = new LinkedHashMap<String, String>();
		File csvFile = new File(CalendarConstant.TEMP_FILE_LOCATION);
		try {
			List fileItems = upload.parseRequest(request);
			Iterator iterator = fileItems.iterator();

			while (iterator.hasNext()) {
				FileItem fileItem = (FileItem) iterator.next();
				if (!fileItem.isFormField()) {

					fileItem.write(csvFile);

				}
			}

			bufferReader = new BufferedReader(new FileReader(csvFile));
			while ((line = bufferReader.readLine()) != null) {

				// use ',' as separator
				String[] argument = line.split(CalendarConstant.COMMA_SPLITTER);

				for (int j = 0; j < argument.length; j++) {

					// use : as separator
					String[] argArray = argument[j].split(CalendarConstant.COL_SPLITTER, 2);

					if (argArray.length == 1) {
						inputMap.replace(lastKey.trim(), inputMap.get(lastKey).trim().concat("," + argArray[0]));
					} else {
						inputMap.put(argArray[0].trim(), argArray[1]);
						lastKey = argArray[0].trim();
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return inputMap;
	}

}
