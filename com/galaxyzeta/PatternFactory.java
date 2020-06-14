package com.galaxyzeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.galaxyzeta.entity.Tuple;

/**
 * Store patterns and provide pattern-match services based on Java reflex.
 */
public class PatternFactory {

	private final static String BLANK = "\\s";
	private final static String EOL = ";";
	private final static String COMMENT = "#.*[\n\r]";
	private final static String OPERATOR_TYPE1 = "\\+[^\\+]|\\-[^\\-]|\\>[^\\=]|\\<[^\\=>]";
	private final static String OPERATOR_TYPE2 = "\\+\\+|\\-\\-|\\*|/|>=|<=|<>|=|:=";
	private final static String PARENTHESE = "\\(|\\)";
	private final static String INTEGER = "(?:0|(?:[1-9]\\d*))[^\\w.]";
	//(?:(?:\\d.\\d*)|(?:.\\d+)) ==> previous float sign [\\+\\-\\*/<>=\\s#;]	(?:(?:\\d+\\.\\d*)|(?:\\.\\d+)|(?:\\d+))(?:[eE]\\d+)?[^A-Za-z]
	private final static String FLOAT = "(?:(?:\\d+\\.\\d*)|(?:\\.\\d+)|(?:\\d+[^.]))(?:[eE]\\d*)?[^\\w]";
	private final static String ID = "[_A-Za-z][\\w]*[\\W]";
	private final static String KEYWORD = "(?:begin|end|if|then|else|for|while|do|and|or|not)[\\W]";
	
	// Error pattern - Error message mapping
	private final static ArrayList<Tuple<String, String>> ERROR_LIST = new ArrayList<>();
	
	static {
		ERROR_LIST.add(new Tuple<String, String>("0\\d+", "Invalid Number"));
		ERROR_LIST.add(new Tuple<String, String>("(?:(?:\\d\\.\\d*)|(?:\\.\\d+)|(?:\\d+))[Ee](?:(?:.*\\D.*)|(?:0.+))", "Invalid exponential number."));
		ERROR_LIST.add(new Tuple<String, String>("\\d+[^\\s\\d\\.eE].*", "Neither number nor identifier! "));
		ERROR_LIST.add(new Tuple<String, String>("[^#]?.*[^\\w\\s\\+\\-\\*/<>=:#\\.]", "Illegal characters! "));
		ERROR_LIST.add(new Tuple<String, String>("[^#]?:[^=]", "Single colon detected! Should be := instead."));
		ERROR_LIST.add(new Tuple<String, String>("[^#]?(?:\\.[^eE\\d])|(?:\\.[eE])", "Invalid dot!"));
	}
	/**
	 * Check whether the input string matches a pattern.
	 * @param type The pattern to use.
	 * @param input	The input string to be matched.
	 * @return matched string. Null if the input cannot match.
	 */
	public static String checkPattern(String type, String input) {
		try {
			Field fld = PatternFactory.class.getDeclaredField(type.toUpperCase());
			String regString = (String)fld.get(PatternFactory.class);
			Matcher matcher = Pattern.compile(regString).matcher(input);
			if (matcher.matches()){
				return matcher.group();
			} else {
				return null;
			}
		} catch (NoSuchFieldException e) {
			System.out.println(String.format("[ERROR] NoSuchField!, %s", type));
		} catch (IllegalAccessException e) {
			System.out.println("[ERROR] IllegalAccess!");
		}
		return null;
	}

	public static String checkErrorPattern(String input) {
		for(Tuple<String, String> each: ERROR_LIST) {
			Matcher matcher = Pattern.compile(each.getPos1()).matcher(input);
			if (matcher.matches()){
				return each.getPos2();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		//System.out.println(checkPattern("FLOAT", "3e\r"));
		System.out.println(checkPattern("FLOAT", ".2e "));
	}
}