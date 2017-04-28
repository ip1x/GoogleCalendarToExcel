package com.google.calendar.constant;

import static com.google.calendar.constant.CalendarConstant.ACT;
import static com.google.calendar.constant.CalendarConstant.CHAR_AT_THE_RATE;
import static com.google.calendar.constant.CalendarConstant.CHAR_MODULUS;
import static com.google.calendar.constant.CalendarConstant.CLI;
import static com.google.calendar.constant.CalendarConstant.COL_SPLITTER;
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
import com.google.calendar.exception.InvalidEventException;

/**
 * This class is used to make key-value pair for events.
 *
 * @author DAMCO
 *
 */
public class Main {

    public static void main(final String[] args) {
	final Main eventTitleParser = new Main();
	Map<String, Map<String, String>> eventKeyValue = new HashMap<String, Map<String, String>>();
	try {
	    eventKeyValue = eventTitleParser.generateMapForEvents(null, "Rohit");
	} catch (final Exception e) {
	    System.out.println("Event with uncompiled name is found");
	    eventKeyValue.put("", null);
	}
	// System.out.println(eventKeyValue);
    }

    public Map<String, Map<String, String>> generateMapForEvents(final Event event, final String userName)
	    throws InvalidEventException {

	// Create configuration file object to create map of excel table
	// header name
	// final ConfigurationFileParser configurationFileParser = new
	// ConfigurationFileParser("configuration.properties");
	// final Map<String, String> propertyMap =
	// configurationFileParser.getPropertyMap();
	// final List<String> configurationKeyList = new ArrayList<>();
	// for (final Map.Entry<String, String> entry : propertyMap.entrySet())
	// {
	// configurationKeyList.add(entry.getKey());
	// }

	// final StringBuilder regexExpresion = new
	// StringBuilder("(?<=\\G(\\w+(?!\\w+)|");
	// for (final String key : configurationKeyList) {
	// regexExpresion.append(":|" + key);
	// }
	// regexExpresion.append("))\\s*");
	// final String EVENT_PARSER_REGEX1 =
	// "(?<=\\G(\\w+(?!\\w+)|:|CLI:|PRJ:|ACT:|TKT:|WBS:|@|%))\\s*";

	// final String eventSummary = event.getSummary();
	String eventSummary = "CLI:aaaaJ:pppp      PRJ:     pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
	eventSummary = "CLI:aaaa TKT:nnnn PRJ:pppp WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
	eventSummary = ":CLI"; // starting with ":" //invalid
	eventSummary = "CLI:  PRJ:sagicor"; // valid
	eventSummary = "CLI:aaaaPRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
	eventSummary = "CLI:aaaaPRJ:pppp PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb"; // valid
	eventSummary = "PRJ:propulsor        %:     80   ACT: test on site @:Imola"; // valid
	eventSummary = "PRJ:propulsor %:80"; // valid
	eventSummary = "CLI :aaaa PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:bbbb";  // space between ":" and "CLI" // invalid
	eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww rohit:aaaa @:cccc %:bbbb"; // invalid tag "rohit" // invalid
	eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww rohit:aaaa @:cccc %:bbbb";  // valid tag "rohit"  // valid
	eventSummary = "CLI:aaaa PRJ:pppp TKT:nnnn WBS:wwww ACT:aaaa @:cccc %:";  // no value    // valid
	eventSummary = "CLI:aaaa %: TKT:nnnn WBS:wwww ACT:aaaa @:cccc PRJ:pppp";   // positional   // valid
	eventSummary = "cli:aaaa %: TkT:nnnn Wbs:wwww ACT:aaaa @:cccc PRJ:pppp";   // case insensitive   // valid

	final List<String> list = new ArrayList<>();
	list.add("CLI".toLowerCase());
	list.add("PRJ".toLowerCase());
	list.add("WBS".toLowerCase());
	list.add("ACT".toLowerCase());
	list.add("@".toLowerCase());
	list.add("%".toLowerCase());
	list.add("TKT".toLowerCase());
	String lastKey = "";
	final Map<String, String> eventMap = new HashMap<>();

	try {
	    final String afterReplaceSpace = eventSummary.trim().replaceAll(" +", " ");
	    if (afterReplaceSpace.length() == 1 || afterReplaceSpace.startsWith(":")) {
		throw new InvalidEventException();
	    }
	    final String[] trimWithSpace = afterReplaceSpace.split(" ");
	    for (final String data : trimWithSpace) {
		if (data.endsWith(":")) {
		    eventMap.put(data.substring(0, data.length() - 1).toLowerCase(), "");
		    lastKey = data.substring(0, data.length() - 1).toLowerCase();
		} else {
		    final String[] dataArray = data.split(":", 2);
		    if ((dataArray != null) && (dataArray.length == 2) && list.contains(dataArray[0].toLowerCase())) {
			eventMap.put(dataArray[0].toLowerCase().trim(), dataArray[1].trim());
			lastKey = dataArray[0].toLowerCase().trim();
		    } else if ((dataArray != null) && (dataArray.length == 2)
			    && !list.contains(dataArray[0].toLowerCase())) {
			throw new Exception();
		    }
		    if ((dataArray != null) && (dataArray.length == 1)) {
			if (list.contains(dataArray[0].toLowerCase())) {
			    lastKey = dataArray[0].toLowerCase();
			    eventMap.put(lastKey, "");
			} else if (!list.contains(dataArray[0].toLowerCase()) && !"".equals(lastKey)) {
			    if (eventMap.get(lastKey) != "") {
				eventMap.replace(lastKey.toLowerCase(),
					eventMap.get(lastKey).concat(" ").concat(dataArray[0].trim()));
			    } else {
				eventMap.put(lastKey.toLowerCase(), dataArray[0].trim());
			    }

			}
			// eventMap.replace(lastKey,
			// eventMap.get(lastKey).concat("
			// ").concat(dataArray[0].trim()));
		    }
		}
	    }
	    for (final Map.Entry<String, String> entry : eventMap.entrySet()) {
		if (!list.contains(entry.getKey())) {
		    throw new Exception();
		}
		System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
	} catch (final Exception e) {
	    System.out.println(e);
	}

	// eventSummary = "#1!:rohit";
	// (?<=\\G(\\w+(?!\\w+)|:|CLI:|PRJ:|ACT:|TKT:|WBS:|@|%))\\s*
	// final String[] data =
	// eventSummary.split("(?<=\\G(\\w+(?!\\w+)|:|CLI:|PRJ:|ACT:|TKT:|WBS:|@|%))\\s*");

	// final Map<String, String> eventMap = new HashMap<>();
	// String key = "";
	// String notKey = null;
	// for (int count = 0; count < data.length; count++) {
	// try {
	// checkForInvalidEvent(data);
	// if (String.valueOf(CHAR_AT_THE_RATE).equals(data[count])
	// || String.valueOf(CHAR_MODULUS).equals(data[count])
	// || ((data.length != (count + 1)) && COL_SPLITTER.equals(data[count +
	// 1]))) {
	// if (data[count].length() > 3) {
	// final String toLowerCase = data[count].toLowerCase();
	// if (toLowerCase.endsWith(TKT.toLowerCase()) ||
	// toLowerCase.endsWith(CLI.toLowerCase())
	// || toLowerCase.endsWith(PRJ.toLowerCase()) ||
	// toLowerCase.endsWith(WBS.toLowerCase())
	// || toLowerCase.endsWith(ACT.toLowerCase())) {
	// eventMap.put(key.toLowerCase(), data[count].substring(0,
	// data[count].length() - 3));
	// key = data[count].substring(data[count].length() - 3,
	// data[count].length()).trim()
	// .toLowerCase();
	// } else {
	// if (key == "") {
	// key = data[count].trim().toLowerCase();
	// notKey = null;
	// } else {
	// eventMap.put(key.toLowerCase(), data[count].trim());
	// notKey = data[count].trim().toLowerCase();
	// }
	// }
	// } else {
	// key = data[count].trim().toLowerCase();
	// notKey = null;
	// }
	// } else if (!COL_SPLITTER.equals(data[count])) {
	// parsingEventForValue(data, eventMap, key, notKey, count);
	// }
	// } catch (final Exception e) {
	// throw new InvalidEventException();
	// }
	// }
	// return CheckValidityOfEvent(event, userName, eventMap);
	return null;

    }

    private void checkForInvalidEvent(final String[] data) throws InvalidEventException {
	if ((data.length == 1) || ((data.length == 2) && !String.valueOf(CHAR_AT_THE_RATE).equals(data[0])
		&& !String.valueOf(CHAR_MODULUS).equals(data[0]))) {
	    throw new InvalidEventException();
	}
	if (data[0].equals(COL_SPLITTER) || data[data.length - 1].equals(COL_SPLITTER)) {
	    throw new InvalidEventException();
	}
    }

    private static void parsingEventForValue(final String[] data, final Map<String, String> map, final String key,
	    final String notKey, final int count) {
	if (map.get(key) != null) {
	    if (notKey != null) {
		map.replace(key, map.get(key).concat(COL_SPLITTER).concat(data[count].trim()));
	    } else {
		map.replace(key, map.get(key).concat(" ").concat(data[count].trim()));
	    }
	} else {
	    map.put(key, data[count].trim());
	}
    }

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
