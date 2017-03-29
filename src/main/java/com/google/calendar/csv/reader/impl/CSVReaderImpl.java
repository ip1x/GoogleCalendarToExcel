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
	 * maximum file size to be uploaded.
	 */
	private int maxFileSize = 50 * 1024;
	
	/**
	 *  maximum size that will be stored in memory
	 */
	private int maxMemSize = 4 * 1024;

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
		factory.setSizeThreshold(maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File("c:\\temp"));		
		ServletFileUpload upload = new ServletFileUpload(factory);		
		upload.setSizeMax(maxFileSize);

		// Create input from CSV

		BufferedReader br = null;
		String line = "";

		String lastKey = "";
		Map<String, String> inputMap = new LinkedHashMap<String, String>();
		File csvFile = new File("C:\\test.csv");
		try {
			List fileItems = upload.parseRequest(request);
			Iterator i = fileItems.iterator();

			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (!fi.isFormField()) {

					fi.write(csvFile);

				}
			}

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] argument = line.split(",");

				for (int j = 0; j < argument.length; j++) {

					// use : as separator
					String[] argArray = argument[j].split(":", 2);

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
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return inputMap;
	}

}