package com.google.calendar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
	
	/**
	 * Default Configuration file name
	 */
	private final String CONFIGURATIONFILENAME = "configuration.properties";
	
	/**
	 * Will contain all key pair.
	 */
	private static Map<String, String> propertyMap = new HashMap<String, String>();

	/**
	 * Constructor which initialize the property object 
	 * 
	 * @param path
	 */
	public ConfigurationFileParser(String path) {
		try {
			property = new Properties();
			if(path == null || path.equals("")){
			input = getClass().getClassLoader().getResourceAsStream(CONFIGURATIONFILENAME);
			}else{
				input=	new FileInputStream(new File(path));
			}
			loadPropertyFile();
		} catch (Exception e) {
			System.out.println("Error in reading properties file");
			e.printStackTrace();
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
			System.out.println("Error in parsing properties file");
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
}
