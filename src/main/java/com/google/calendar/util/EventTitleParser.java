package com.google.calendar.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.api.services.calendar.model.Event;
import com.google.calendar.constant.CalendarConstant;
import com.google.calendar.exception.InvalidEventException;

/**
 * This class is used to make key-value pair for events.
 *
 * @author DAMCO
 *
 */
public class EventTitleParser {

    public final Logger logger = Logger.getLogger(EventTitleParser.class);

    public Map<String, Map<String, String>> generateMapForEvents(final Event event, final String configurationFilePath,
	    final String calendarName) throws InvalidEventException {

	// Create configuration file object to create map of excel table
	// header name
	final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(configurationFilePath);
	final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();
	final List<String> configurationKeyList = new ArrayList<>();
	for (final Map.Entry<String, String> entry : propertyMap.entrySet()) {
	    configurationKeyList.add(entry.getKey().toLowerCase());
	}
	final String eventSummary = event.getSummary();

	final Map<String, String> eventMap = new HashMap<>();

	try {

	    // Remove multiple spaces with a single space from event title
	    final String afterReplaceSpace = eventSummary.trim().replaceAll(" +", " ");
	    if ((afterReplaceSpace.length() == 1) || afterReplaceSpace.startsWith(CalendarConstant.COL_SPLITTER)) {
		throw new InvalidEventException();
	    }
	    final String[] trimWithSpace = afterReplaceSpace.split(" ");
	    parseEventTitleForGeneratingMap(configurationKeyList, eventMap, trimWithSpace);
	    parseMapForInvalidTag(configurationKeyList, eventMap);
	} catch (final Exception e) {
	    logger.error(CalendarConstant.ERROR_INVALID_EVENT_FOUND, e);
	    throw new InvalidEventException();
	}
	final Map<String, Map<String, String>> eventKeyValue = new HashMap<>();
	eventMap.put(CalendarConstant.ENDED_ON_LOWER_CASE,
		CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getEnd().getDateTime().getValue())));
	eventMap.put(CalendarConstant.STARTED_ON_LOWER_CASE,
		CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getStart().getDateTime().getValue())));
	eventMap.put(CalendarConstant.STF_LOWER_CASE, calendarName);
	eventKeyValue.put(event.getSummary(), eventMap);
	return eventKeyValue;
    }

    /**
     *
     * @param configurationKeyList
     * @param eventMap
     * @param trimWithSpace
     * @throws InvalidEventException
     */
    private void parseEventTitleForGeneratingMap(final List<String> configurationKeyList,
	    final Map<String, String> eventMap, final String[] trimWithSpace) throws InvalidEventException {
	String lastKey = "";
	for (final String data : trimWithSpace) {
	    if (data.endsWith(CalendarConstant.COL_SPLITTER)) {
		eventMap.put(data.substring(0, data.length() - 1).toLowerCase(), "");
		lastKey = data.substring(0, data.length() - 1).toLowerCase();
	    } else {
		final String[] dataArray = data.split(CalendarConstant.COL_SPLITTER, 2);
		if ((dataArray != null) && (dataArray.length == 2)
			&& configurationKeyList.contains(dataArray[0].toLowerCase())) {
		    eventMap.put(dataArray[0].toLowerCase().trim(), dataArray[1].trim());
		    lastKey = dataArray[0].toLowerCase().trim();
		} else if ((dataArray != null) && (dataArray.length == 2)
			&& !configurationKeyList.contains(dataArray[0].toLowerCase())) {
		    throw new InvalidEventException();
		}
		putMapValueForKey(configurationKeyList, eventMap, lastKey, dataArray);
	    }
	}
    }

    /**
     *
     * @param configurationKeyList
     * @param eventMap
     * @param lastKey
     * @param dataArray
     */
    private void putMapValueForKey(final List<String> configurationKeyList, final Map<String, String> eventMap,
	    String lastKey, final String[] dataArray) {
	if ((dataArray != null) && (dataArray.length == 1)) {
	    if (configurationKeyList.contains(dataArray[0].toLowerCase())) {
		lastKey = dataArray[0].toLowerCase();
		eventMap.put(lastKey, "");
	    } else if (!configurationKeyList.contains(dataArray[0].toLowerCase()) && !"".equals(lastKey)) {
		if (!"".equals(eventMap.get(lastKey))) {
		    eventMap.replace(lastKey.toLowerCase(),
			    eventMap.get(lastKey).concat(" ").concat(dataArray[0].trim()));
		} else {
		    eventMap.put(lastKey.toLowerCase(), dataArray[0].trim());
		}

	    }
	}
    }

    /**
     *
     * @param configurationKeyList
     * @param eventMap
     * @throws InvalidEventException
     */
    private void parseMapForInvalidTag(final List<String> configurationKeyList, final Map<String, String> eventMap)
	    throws InvalidEventException {
	for (final Map.Entry<String, String> entry : eventMap.entrySet()) {
	    if (!configurationKeyList.contains(entry.getKey())) {
		throw new InvalidEventException();
	    }
	    logger.debug(entry.getKey() + "=" + entry.getValue());
	}
    }
}
