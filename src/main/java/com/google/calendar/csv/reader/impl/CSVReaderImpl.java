package com.google.calendar.csv.reader.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.csv.reader.CSVReader;

/**
 * This class will used to read the csv file and convert the csv file data into
 * map object
 *
 * @author DAMCO
 */
public class CSVReaderImpl implements CSVReader {

    public final Logger logger = Logger.getLogger(CSVReaderImpl.class);

    /**
     * Return Map of input parameter taken from CSV file
     *
     * @param request HttpServletRequest object with form parameter
     * @return Map of input parameter
     */
    @Override
    public Map<String, String> readCSV(final HttpServletRequest request,
            final HttpServletResponse response) {

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
        Map<String, String> inputMap = new LinkedHashMap<>();
      
        try {
            List fileItems = upload.parseRequest(request);
            Iterator iterator = fileItems.iterator();
            InputStream inputStream = null;
            while (iterator.hasNext()) {
                FileItem fileItem = (FileItem) iterator.next();
                if (!fileItem.isFormField()) {

                    inputStream = fileItem.getInputStream();

                } else {
                    throw new FileNotFoundException();
                }
            }

            bufferReader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"));
            if ((line = bufferReader.readLine()) != null) {

                // use ',' as separator
                String[] argument = line.split(CalendarConstant.COMMA_SPLITTER);

                for (int j = 0; j < argument.length; j++) {

                    // use : as separator
                    String[] argArray =
                            argument[j].split(CalendarConstant.COL_SPLITTER, 2);

                    if (argArray.length == 1) {
                        inputMap.replace(lastKey.trim(), inputMap.get(lastKey)
                                .trim().concat("," + argArray[0]));
                    } else {
                        inputMap.put(argArray[0].trim(), argArray[1]);
                        lastKey = argArray[0].trim();
                    }
                }

            } else {

                request.setAttribute(CalendarConstant.ERROR_MESSAGE,
                        CalendarConstant.ERROR_IN_FILE_SELECTION);
                request.getRequestDispatcher(CalendarConstant.HOME_PAGE)
                        .forward(request, response);
            }

        } catch (Exception e) {

            logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
            try {
                request.setAttribute(CalendarConstant.ERROR_MESSAGE,
                        CalendarConstant.ERROR_IN_FILE_SELECTION);
                request.getRequestDispatcher(CalendarConstant.HOME_PAGE)
                        .forward(request, response);
            } catch (ServletException | IOException e1) {
                logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e1);
                request.setAttribute(CalendarConstant.ERROR_MESSAGE,
                        CalendarConstant.ERROR_IN_LOADING);
            }
        } finally {
            if (bufferReader != null) {
                try {
                    bufferReader.close();
                } catch (IOException e) {
                    logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE, e);
                }
            }
        }

        return inputMap;
    }

}
