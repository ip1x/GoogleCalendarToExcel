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
 * This class is used to make key-value pair for events tags and their values
 * respectively.
 *
 * @author DAMCO
 *
 */
public class EventTitleParser {

    public final Logger logger = Logger.getLogger(EventTitleParser.class);

    /**
     * This method parses event to generate MAP for event. That MAP contains
     * Event title as key and Event key-value pair as value. If an Event is
     * invalid then an InvalidEventException is thrown.
     *
     * @param event
     *            Event to be parsed for key value pair
     * @param configurationFilePath
     *            Path of the configuration file where all mapping for the
     *            output file is placed
     * @param calendarName
     *            Name of calendar of which event is parsing
     * @return Map of event containing event title as key and Event
     *         key-value pair as value.
     * @throws InvalidEventException
     *             thrown if event is Invalid
     */
    public Map<String, Map<String, String>> generateMapForEvents(final Event event, final String configurationFilePath,
	    final String calendarName) throws InvalidEventException {

	// Reads Configuration file for valid event title tags
	final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(configurationFilePath);
	final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();
	final List<String> configurationKeyList = new ArrayList<>();

	// Parses each TAGS fetched from configuration file and generate a List
	// for that.
	for (final Map.Entry<String, String> entry : propertyMap.entrySet()) {
	    configurationKeyList.add(entry.getKey().toLowerCase());
	}
	final String eventSummary = event.getSummary();

	final Map<String, String> eventMap = new HashMap<>();

	try {

	    // Remove multiple spaces with a single space from event title
	    final String afterReplaceSpace = eventSummary.trim().replaceAll(" +", " ");
	    if ((afterReplaceSpace.length() == 1) || afterReplaceSpace.startsWith(CalendarConstant.COL_SPLITTER)) {
		// If length is 1 or starts with ":", then Event title is
		// invalid
		throw new InvalidEventException();
	    }
	    // Splits Event title with space
	    final String[] trimWithSpace = afterReplaceSpace.split(" ");
	    parseEventTitleForGeneratingMap(configurationKeyList, eventMap, trimWithSpace);
	    parseMapForInvalidTag(configurationKeyList, eventMap);
	} catch (final Exception e) {
	    throw new InvalidEventException();
	}
	final Map<String, Map<String, String>> eventKeyValue = new HashMap<>();

	// Put value of "Started on", "Ended on" and "Staff" value in Map, it is
	// done separately because these values does not come in event title.
	eventMap.put(CalendarConstant.ENDED_ON_LOWER_CASE,
		CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getEnd().getDateTime().getValue())));
	eventMap.put(CalendarConstant.STARTED_ON_LOWER_CASE,
		CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getStart().getDateTime().getValue())));
	eventMap.put(CalendarConstant.STF_LOWER_CASE, calendarName);
	// checks for value of "WBS" tag of event title
	if (eventMap.get(CalendarConstant.WBS_LOWER_CASE) != null
		&& !"".equals(eventMap.get(CalendarConstant.WBS_LOWER_CASE))) {
	    if (eventMap.get(CalendarConstant.ACT_LOWER_CASE) != null
		    && !"".equals(eventMap.get(CalendarConstant.ACT_LOWER_CASE))) {
		// If value of WBS and ACT is not null and empty, then append
		// value of WBS with ACT
		eventMap.put(CalendarConstant.ACT_LOWER_CASE, eventMap.get(CalendarConstant.ACT_LOWER_CASE).concat(",")
			.concat(eventMap.get(CalendarConstant.WBS_LOWER_CASE)));
	    } else {
		// Otherwise put value of WBS in ACT
		eventMap.put(CalendarConstant.ACT_LOWER_CASE, eventMap.get(CalendarConstant.WBS_LOWER_CASE));
	    }
	}

	// Put event title as key and generated Map of event title key-value as
	// value.
	eventKeyValue.put(event.getSummary(), eventMap);
	return eventKeyValue;
    }

    /**
     * This method parses each string after splitting it with space. This method
     * creates MAP for key-value pair for the event tag and value respectively
     *
     * @param configurationKeyList
     *            List of all keys that can be valid tags and are present in
     *            configuration file
     * @param eventMap
     *            Map that will have key-value pair of event title
     * @param trimWithSpace
     *            String array after splitting with space.
     * @throws InvalidEventException
     */
    private void parseEventTitleForGeneratingMap(final List<String> configurationKeyList,
	    final Map<String, String> eventMap, final String[] trimWithSpace) throws InvalidEventException {
	String lastKey = "";
	logger.info("Parsing Event titles..............");
	for (final String data : trimWithSpace) {
	    // Parses each String after splitting the event title with Space.
	    if (data.endsWith(CalendarConstant.COL_SPLITTER)) {
		// If String ends with ":" then key will be string with last ":"
		// removed
		eventMap.put(data.substring(0, data.length() - 1).toLowerCase(), "");
		lastKey = data.substring(0, data.length() - 1).toLowerCase();
	    } else {
		// String is further split with ":" with limit 2.
		final String[] dataArray = data.split(CalendarConstant.COL_SPLITTER, 2);
		if ((dataArray != null) && (dataArray.length == 2)
			&& configurationKeyList.contains(dataArray[0].toLowerCase())) {
		    // If after split, we have 2 strings where 1st one is
		    // contained in valid tag list, then put it in MAP with 1st
		    // string as key and 2nd one as value.
		    eventMap.put(dataArray[0].toLowerCase().trim(), dataArray[1].trim());
		    lastKey = dataArray[0].toLowerCase().trim();
		} else if ((dataArray != null) && (dataArray.length == 2)
			&& !configurationKeyList.contains(dataArray[0].toLowerCase())) {
		    // If after split, we have 2 strings where 1st one is not
		    // contained in valid tag list, then throw
		    // InvalidEventException
		    throw new InvalidEventException();
		}
		putValueForKey(configurationKeyList, eventMap, lastKey, dataArray);
	    }
	}
    }

    /**
     * If after splitting with ":" and limit 2, there is a chance that we get
     * only one string, that means there is no ":" in that string. In that case
     * this method puts that string as value of valid key in the MAP, otherwise
     * not.
     *
     * @param configurationKeyList
     *            List of all keys that can be valid tags and are present in
     *            configuration file
     * @param eventMap
     *            Map that will have key-value pair of event title
     * @param lastKey
     *            Last key i.e event tag that program has parsed till now.
     * @param dataArray
     *            Array of string after splitting with ":" and limit 2
     */
    private void putValueForKey(final List<String> configurationKeyList, final Map<String, String> eventMap,
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
     * This method parses MAP to check the existence of invalid tag. If invalid
     * tag is found then, InvalidEventException is thrown otherwise not.
     *
     * @param configurationKeyList
     *            List of all keys that can be valid tags and are present in
     *            configuration file
     * @param eventMap
     *            Map that will have key-value pair of event title
     * @throws InvalidEventException
     */
    private void parseMapForInvalidTag(final List<String> configurationKeyList, final Map<String, String> eventMap)
	    throws InvalidEventException {
	// Parses MAP for existence of invalid tag
	logger.info("Event with valid key-value pair are found..............");
	for (final Map.Entry<String, String> entry : eventMap.entrySet()) {
	    if (!configurationKeyList.contains(entry.getKey())) {
		// If invalid tag is found throws exception.
		throw new InvalidEventException();
	    }
	}
    }
}
