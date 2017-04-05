package com.google.calendar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.controller.UploadServlet;

/**
 * This class will read properties file and convert it into map object
 * 
 * @author DAMCO
 *
 */
public class ConfigurationFileParser {

	/**
	 * Property class which load with all key value pair
	 */
	private  Properties property;
	
	/**
	 * InputStream with loaded configuration file 
	 */
	private InputStream input = null;
	
	public final Logger logger = Logger.getLogger(ConfigurationFileParser.class);
	

	
	/**
	 * Will contain all key pair.
	 */
	private  Map<String, String> propertyMap = new HashMap<>();

	/**
	 * Constructor which initialize the property object 
	 * 
	 * @param path Location of configuration file
	 */
	public ConfigurationFileParser(String path) {
		try {
			property = new Properties();
			if(path != null && path.equals(CalendarConstant.CONFIGURATION_FILE_NAME)){
			input = getClass().getClassLoader().getResourceAsStream(CalendarConstant.CONFIGURATION_FILE_NAME);
			}else{
				input=	new FileInputStream(new File(path));
			}
			loadPropertyFile();
		} catch (Exception e) {
			logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e);
		}
	}

	/**
	 * Will load the properties file from disk or classpath
	 */
	public void loadPropertyFile() {
		try {
			property.load(input);
			final Enumeration<?> e = property.propertyNames();
			while (e.hasMoreElements()) {
				final String key = (String) e.nextElement();
				final String value = property.getProperty(key);
				propertyMap.put(key, value);
			}
		} catch (final IOException e) {
			logger.error(CalendarConstant.LOGGER_DEFAULT_MESSAGE , e);
		}
	}

	/**
	 * getter 
	 * 
	 * @param key
	 * @return
	 */
	public String getPropertyByKey(final String key) {
		return property.getProperty(key);
	}

	/**
	 * setter
	 * 
	 * @return
	 */
	public Properties getProperty() {
		return property;
	}
	
	/**
	 * getter 
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
}
