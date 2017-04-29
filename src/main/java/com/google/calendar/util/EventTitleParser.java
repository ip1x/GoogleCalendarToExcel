package com.google.calendar.util;

import static com.google.calendar.constant.CalendarConstant.ACT;
import static com.google.calendar.constant.CalendarConstant.CHAR_AT_THE_RATE;
import static com.google.calendar.constant.CalendarConstant.CHAR_MODULUS;
import static com.google.calendar.constant.CalendarConstant.CLI;
import static com.google.calendar.constant.CalendarConstant.ENDDATE;
import static com.google.calendar.constant.CalendarConstant.PRJ;
import static com.google.calendar.constant.CalendarConstant.STAFF;
import static com.google.calendar.constant.CalendarConstant.STARTDATE;
import static com.google.calendar.constant.CalendarConstant.TKT;
import static com.google.calendar.constant.CalendarConstant.WBS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	//    public static void main(final String[] args) {
	//	final EventTitleParser eventTitleParser = new EventTitleParser();
	//	Map<String, Map<String, String>> eventKeyValue = new HashMap<String, Map<String, String>>();
	//	try {
	//	    eventKeyValue = eventTitleParser.generateMapForEvents(null, "Rohit");
	//	} catch (final Exception e) {
	//	    System.out.println("Event with uncompiled name is found");
	//	    eventKeyValue.put("", null);
	//	}
	//	System.out.println(eventKeyValue);
	//    }

	public Map<String, Map<String, String>> generateMapForEvents(final Event event, final String userName, final String configurationFilePath)
			throws InvalidEventException {

		// Create configuration file object to create map of excel table
		// header name
		final ConfigurationFileParser configurationFileParser = new ConfigurationFileParser(configurationFilePath);
		final Map<String, String> propertyMap = configurationFileParser.getPropertyMap();
		final List<String> configurationKeyList = new ArrayList<>();
		for (final Map.Entry<String, String> entry : propertyMap.entrySet()) {
			configurationKeyList.add(entry.getKey().toLowerCase());
		}
		final String eventSummary = event.getSummary();
		//eventSummary = "CLI:aaaaJ:pppp      PRJ:     pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
		//eventSummary = "CLI:aaaa TKT:nnnn PRJ:pppp WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
		//eventSummary = ":CLI"; // starting with ":" //invalid
		//eventSummary = "CLI:  PRJ:sagicor"; // valid
		//eventSummary = "CLI:aaaaPRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
		//eventSummary = "CLI:aaaaPRJ:pppp PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
		//eventSummary = "PRJ:propulsor        %:     80   ACT: test on site @:Imola"; // valid
		//eventSummary = "PRJ:propulsor %:80"; // valid
		//eventSummary = "CLI :aaaa PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb";  // space between ":" and "CLI" // invalid
		//eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww rohit:aaaa @:cccc %:bbbb"; // invalid tag "rohit" // invalid
		//eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww rohit:aaaa @:cccc %:bbbb";  // valid tag "rohit"  // valid
		//eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:";  // no value    // valid
		//eventSummary = "CLI:aaaa %: TKT:nnnn WBS:wwww ACT:aaaa @:cccc PRJ:pppp";   // positional   // valid
		//eventSummary = "cli:aaaa %: TkT:nnnn Wbs:wwww ACT:aaaa @:cccc PRJ:pppp";   // case insensitive   // valid


		String lastKey = "";
		final Map<String, String> eventMap = new HashMap<>();

		try {
			final String afterReplaceSpace = eventSummary.trim().replaceAll(" +", " ");
			if ((afterReplaceSpace.length() == 1) || afterReplaceSpace.startsWith(":")) {
				throw new InvalidEventException();
			}
			final String[] trimWithSpace = afterReplaceSpace.split(" ");
			for (final String data : trimWithSpace) {
				if (data.endsWith(":")) {
					eventMap.put(data.substring(0, data.length() - 1).toLowerCase(), "");
					lastKey = data.substring(0, data.length() - 1).toLowerCase();
				} else {
					final String[] dataArray = data.split(":", 2);
					if ((dataArray != null) && (dataArray.length == 2)
							&& configurationKeyList.contains(dataArray[0].toLowerCase())) {
						eventMap.put(dataArray[0].toLowerCase().trim(), dataArray[1].trim());
						lastKey = dataArray[0].toLowerCase().trim();
					} else if ((dataArray != null) && (dataArray.length == 2)
							&& !configurationKeyList.contains(dataArray[0].toLowerCase())) {
						throw new InvalidEventException();
					}
					if ((dataArray != null) && (dataArray.length == 1)) {
						if (configurationKeyList.contains(dataArray[0].toLowerCase())) {
							lastKey = dataArray[0].toLowerCase();
							eventMap.put(lastKey, "");
						} else if (!configurationKeyList.contains(dataArray[0].toLowerCase()) && !"".equals(lastKey)) {
							if (eventMap.get(lastKey) != "") {
								eventMap.replace(lastKey.toLowerCase(),
										eventMap.get(lastKey).concat(" ").concat(dataArray[0].trim()));
							} else {
								eventMap.put(lastKey.toLowerCase(), dataArray[0].trim());
							}

						}
					}
				}
			}
			for (final Map.Entry<String, String> entry : eventMap.entrySet()) {
				if (!configurationKeyList.contains(entry.getKey())) {
					throw new InvalidEventException();
				}
				System.out.println(entry.getKey() + "=" + entry.getValue());
			}
		} catch (final Exception e) {
			throw new InvalidEventException();
		}
		final Map<String, Map<String, String>> eventKeyValue = new HashMap<>();
		eventMap.put(ENDDATE.toLowerCase(), CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getEnd().getDateTime().getValue())));
		eventMap.put(STARTDATE.toLowerCase(), CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getStart().getDateTime().getValue())));
		eventMap.put(STAFF.toLowerCase(), userName);
		eventKeyValue.put(event.getSummary(), eventMap);
		return eventKeyValue;
	}

	//    private void checkForInvalidEvent(final String[] data) throws InvalidEventException {
	//	if ((data.length == 1) || ((data.length == 2) && !String.valueOf(CHAR_AT_THE_RATE).equals(data[0])
	//		&& !String.valueOf(CHAR_MODULUS).equals(data[0]))) {
	//	    throw new InvalidEventException();
	//	}
	//	if (data[0].equals(COL_SPLITTER) || data[data.length - 1].equals(COL_SPLITTER)) {
	//	    throw new InvalidEventException();
	//	}
	//    }

	//    private static void parsingEventForValue(final String[] data, final Map<String, String> map, final String key,
	//	    final String notKey, final int count) {
	//	if (map.get(key) != null) {
	//	    if (notKey != null) {
	//		map.replace(key, map.get(key).concat(COL_SPLITTER).concat(data[count].trim()));
	//	    } else {
	//		map.replace(key, map.get(key).concat(" ").concat(data[count].trim()));
	//	    }
	//	} else {
	//	    map.put(key, data[count].trim());
	//	}
	//    }

	@SuppressWarnings("rawtypes")
	private static Map<String, Map<String, String>> CheckValidityOfEvent(final Event event, final String userName,
			final Map<String, String> map) throws InvalidEventException {
		boolean invalid = false;
		for (final Map.Entry<String, String> entry : map.entrySet()) {
			// TODO: Needs to modify code for lowercase
			switch (entry.getKey().toString().toUpperCase()) {
			case TKT:
			case WBS:
			case CLI:
			case PRJ:
			case ACT:
			case "" + CHAR_AT_THE_RATE:
				break;
			case "" + CHAR_MODULUS:
				entry.setValue(entry.getValue() + "%");
			break;
			default:
				invalid = true;
				break;
			}
			// System.out.println(entry.getKey() + " = " + entry.getValue());
		}

		// final Iterator it = map.entrySet().iterator();
		// boolean invalid = false;
		// while (it.hasNext()) {
		// final Map.Entry pair = (Map.Entry) it.next();
		// // TODO: Needs to modify code for lowercase
		// switch (pair.getKey().toString().toUpperCase()) {
		// case TKT:
		// case WBS:
		// case CLI:
		// case PRJ:
		// case ACT:
		// case "" + CHAR_AT_THE_RATE:
		// case "" + CHAR_MODULUS:
		// break;
		// default:
		// invalid = true;
		// break;
		// }
		// // System.out.println(pair.getKey() + " = " + pair.getValue());
		// }
		if (invalid || map.values().contains(null)) {
			throw new InvalidEventException();
		} else {
			final Map<String, Map<String, String>> eventKeyValue = new HashMap<>();
			map.put(ENDDATE.toLowerCase(),
					CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getEnd().getDateTime().getValue())));
			map.put(STARTDATE.toLowerCase(),
					CalendarConstant.TABLE_DATE_FORMAT.format(new Date(event.getStart().getDateTime().getValue())));
			map.put(STAFF.toLowerCase(), userName);
			eventKeyValue.put("", map);
			return eventKeyValue;
		}
	}
}
