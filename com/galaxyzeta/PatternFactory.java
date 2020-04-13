package com.galaxyzeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private final static String NUMBER = "\\d+[\\+\\-\\*/<>=\\s]";
	private final static String ID = "[\\w][\\w]*[\\W]";
	private final static String KEYWORD = "(begin|end|if|then|else|for|while|do|and|or|not)[\\W]";
	
	// Error pattern - Error message mapping
	private final static HashMap<String, String> ERROR_MAP = new HashMap<>();
	
	static {
		ERROR_MAP.put("\\d+[^\\s\\d].*", "Neither number nor identifier! ");
		ERROR_MAP.put("[^#].*[^\\w\\s\\+\\-\\*/<>=:#]", "Illegal characters! ");
		ERROR_MAP.put(":[^=]", "Single colon detected. Should be := instead.");
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
		Set<String> errorPatterns = ERROR_MAP.keySet();
		for(String each: errorPatterns) {
			Matcher matcher = Pattern.compile(each).matcher(input);
			if (matcher.matches()){
				return ERROR_MAP.get(each);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(checkErrorPattern("asd$"));
	}
}