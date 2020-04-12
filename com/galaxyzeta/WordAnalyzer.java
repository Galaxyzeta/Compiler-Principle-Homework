package com.galaxyzeta;

import java.util.ArrayList;
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

	private String program;
	private LinkedList<ParseResult> tupleResult = new LinkedList<>();
	private boolean verbose = true;

	private String inputPath = null;
	private String outputPath = null;
	
	private static HashMap<String, Integer> codecMap = new HashMap<>();
	private static HashMap<String, Integer> fix = new HashMap<>();
	private static LinkedList<Character> blanklist = new LinkedList<>(Arrays.asList(' ', '\t', '\n', (char)13));

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
	public void readProgram() {
		StringBuilder sb = new StringBuilder();
		boolean blankFlag = false;
		try (FileReader fr = new FileReader(inputPath)) {
			int c = -1;
			int blocked = 0;
			boolean quitFlag = false;
			while(quitFlag == false) {
				//EOF detect
				if(blocked == 0) {
					c = fr.read();
				} else {
					blocked -= 1;
				}
				if(c == -1) {
					quitFlag = true;
				}
				char chr = (char)c;
				if(c == 13 || c == -1) {
					chr = '\n';
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

				// Iterate over a priority base array. Try to fit into each pattern.
				ArrayList<String> arr = new ArrayList<>(Arrays.asList("KEYWORD", "ID", "NUMBER", "OPERATOR_TYPE1","OPERATOR_TYPE2", "COMMENT", "PARENTHESE", "EOL", "BLANK"));
				for (String each : arr) {
					String match = PatternFactory.checkPattern(each, str);
					if (match != null) {
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
			}
			System.out.println(tupleResult);

			// Write result into a file
			try (FileWriter fw = new FileWriter(new File(outputPath))){
				sb = new StringBuilder();
				for (ParseResult pr : tupleResult) {
					sb.append(pr.toString());
					sb.append('\n');
				}
				fw.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			sb.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.program = sb.toString();
	}

	/**
	 * Set program directly for debug usage.
	*/
	public void readProgramDebug(String program) {
		this.program = program;
	}

	public static void main(String[] args) {
		System.out.println((int)'\n');
		//String s = "+-*/123+++++--- asdbegin begin if (;)#if else then\nasd";
		WordAnalyzer wa = new WordAnalyzer("other/input.txt", "other/log.txt");
		wa.readProgram();
		//wa.readProgramDebug(")\na:=333");
	}
}