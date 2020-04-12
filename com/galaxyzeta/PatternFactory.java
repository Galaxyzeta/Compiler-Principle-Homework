package com.galaxyzeta;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Store patterns and provide pattern-match services based on Java reflex.
 */
public class PatternFactory {

	private final static String BLANK = "[ \n\t]";
	private final static String EOL = ";";
	private final static String COMMENT = "#.*\n";
	private final static String OPERATOR_TYPE1 = "\\+[^\\+]|\\-[^\\-]|\\>[^\\=]|\\<[^\\=>]";
	private final static String OPERATOR_TYPE2 = "\\+\\+|\\-\\-|\\*|/|>=|<=|<>|=|:=";
	private final static String PARENTHESE = "\\(|\\)";
	private final static String NUMBER = "\\d+[\\D]";
	private final static String ID = "[\\w][\\w]*[\\W]";
	private final static String KEYWORD = "(begin|end|if|then|else|for|while|do|and|or|not)[\\W]";
	
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
			System.out.println("[ERROR] NoSuchField!");
		} catch (IllegalAccessException e) {
			System.out.println("[ERROR] IllegalAccess!");
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(checkPattern("OPERATOR_TYPE1", "-*"));
	}
}