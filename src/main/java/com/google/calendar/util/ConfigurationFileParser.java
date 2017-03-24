package com.google.calendar.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigurationFileParser {

	private final Properties property;
	private final InputStream input;
	private final String configurationFileName = "configuration.properties";
	private static Map<String, String> propertyMap = new HashMap<String, String>();

	public ConfigurationFileParser() {
		property = new Properties();
		input = getClass().getClassLoader().getResourceAsStream(configurationFileName);
		loadPropertyFile();
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPropertyByKey(final String key) {
		return property.getProperty(key);
	}

	public Properties getProperty() {
		return property;
	}
}
