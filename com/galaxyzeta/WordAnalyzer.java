package com.galaxyzeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.galaxyzeta.entity.WordCodec;
import com.galaxyzeta.entity.ParseResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A word analyzer that can be applied to extract word pattern tuples.
 */
public class WordAnalyzer {

	private LinkedList<ParseResult> tupleResult = new LinkedList<>();
	private boolean verbose = true;

	private String inputPath = null;
	private String outputPath = null;
	
	private static HashMap<String, Integer> codecMap = new HashMap<>();
	private static HashMap<String, Integer> fix = new HashMap<>();
	private static LinkedList<Character> blanklist = new LinkedList<>(Arrays.asList(' ', '\t', '\n', '\r'));
	private static LinkedList<String> priorityList = new LinkedList<>(Arrays.asList("KEYWORD", "ID", "NUMBER", "OPERATOR_TYPE1","OPERATOR_TYPE2", "COMMENT", "PARENTHESE", "EOL", "BLANK"));

	static {
		// Put fore-read fix into hashmap
		fix.put("KEYWORD", -1);
		fix.put("NUMBER", -1);
		fix.put("OPERATOR_TYPE1", -1);
		fix.put("ID", -1);
		fix.put("COMMENT", -1);

		// Record codec for each pattern. -1 means useless codec that will NOT be saved into final result.
		codecMap.put("KEYWORD", WordCodec.KEYWORD.ordinal());
		codecMap.put("NUMBER", WordCodec.NUMBER.ordinal());
		codecMap.put("ID", WordCodec.ID.ordinal());
		codecMap.put("OPERATOR_TYPE1", WordCodec.OPERATOR.ordinal());
		codecMap.put("OPERATOR_TYPE2", WordCodec.OPERATOR.ordinal());
		codecMap.put("COMMENT", -1);
		codecMap.put("PARENTHESE", WordCodec.OPERATOR.ordinal());
		codecMap.put("EOL", WordCodec.OPERATOR.ordinal());
		codecMap.put("BLANK", -1);
	}

	WordAnalyzer() {
	}

	WordAnalyzer(String inputPath, String outputPath) {
		this.outputPath = outputPath;
		this.inputPath = inputPath;
	}

	/**
	 * Read program from file.
	 */
	public void readProgram() throws WordSpellException {
		StringBuilder sb = new StringBuilder();
		boolean blankFlag = false;
		int lineNumber = 1;
		try (FileReader fr = new FileReader(inputPath)) {
			int c = -1;
			int blocked = 0;
			boolean quitFlag = false;
			char chr = '\n';
			while(quitFlag == false) {
				//EOF detect
				if(blocked == 0) {
					c = fr.read();
				} else {
					blocked --;
				}
				if(c == -1) {
					quitFlag = true;
					chr = '\n';
				} else {
					chr = (char)c;
					if (chr == '\n') {
						lineNumber ++;
						System.out.println("[INFO]LineNumber="+lineNumber);
					}
				}
				// Judge useless blank to save time.
				if (blanklist.contains(chr)) {
					if (blankFlag == true) {
						continue;
					} else {
						blankFlag = true;
					}
				} else {
					blankFlag = false;
				}

				// Read a character to buffer zone.
				sb.append(chr);
				String str = sb.toString();

				boolean breakFlag = false;
				// Iterate over a priority base array. Try to fit into each pattern.
				for (String each : priorityList) {
					String match = PatternFactory.checkPattern(each, str);
					if (match != null) {
						breakFlag = true;
						// Handle fore read.
						if (fix.containsKey(each)) {
							blocked = 1;
							match = match.substring(0, match.length() + fix.get(each));
						}
						// Clean up buffer zone.
						sb = new StringBuilder();
						// Show debug message if VERBOSE == true
						if (verbose == true) {
							System.out.println(String.format("[INFO]Detected %s , Mode = %s", match, each));
						}
						// Save result into a map. Only useful results can be saved.
						int codec = codecMap.get(each);
						if (codec>=0) {
							tupleResult.add(new ParseResult(match, codec));
						}
						break;
					}
				}
				if (breakFlag == true) {
					continue;
				}

				//Error detect
				String errorMessage = PatternFactory.checkErrorPattern(str);
				if (errorMessage != null) {
					throw new WordSpellException(errorMessage, str, lineNumber);
				}

			}
			System.out.println(tupleResult);

			// Write result into a file
			FileWriter fw = new FileWriter(new File(outputPath));
			sb = new StringBuilder();
			for (ParseResult pr : tupleResult) {
				sb.append(pr.toString());
				sb.append('\n');
			}
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		System.out.println((int)'\n');
		//String s = "+-*/123+++++--- asdbegin begin if (;)#if else then\nasd";
		WordAnalyzer wa = new WordAnalyzer("other/input.txt", "other/log.txt");
		try {
			wa.readProgram();
		} catch (WordSpellException e) {
			e.printStackTrace();
		}
	}
}