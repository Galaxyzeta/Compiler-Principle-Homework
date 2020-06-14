package com.galaxyzeta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Deque;
import java.util.regex.*;
import com.galaxyzeta.entity.Tuple;
import com.galaxyzeta.entity.WordCodec;

import java.util.HashSet;
import java.io.File;
import java.io.FileReader;

public class SyntaxAnalyzer {
	private HashMap<String, ArrayList<ArrayList<String>>> deduceMap = new HashMap<>();
	private Set<String> nonTerminalSet = new HashSet<>();
	private Set<String> terminalSet = new HashSet<>();
	private HashMap<String, HashSet<String>> first = new HashMap<>();
	private HashMap<String, HashSet<String>> follow = new HashMap<>();
	private HashMap<String, ArrayList<HashSet<String>>> select = new HashMap<>();
	private Set<String> keySet;

	private HashMap<Integer, String> symbolTypeRef = new HashMap<>();

	private static final String EPSILON = "@";
	private static final String EOL = "$";

	private Integer analyzePosition = 0;

	private void init(String filePath) {
		File fp = new File(filePath);
		try (FileReader fr = new FileReader(fp);) {
			StringBuilder sb = new StringBuilder();
			// 1. Read Non Terminal Set
			char current;
			while ((current = (char)fr.read()) != '\r') {
				sb.append(current);
			}
			String[] strArr = sb.toString().split("\\|");
			for(String s : strArr) {
				nonTerminalSet.add(s);
			}
			fr.read();	//Read \n
			// 2. Read Terminal Set

			sb = new StringBuilder();
			while ((current = (char)fr.read()) != '\r') {
				sb.append(current);
			}
			strArr = sb.toString().split("\\|");
			for(String s : strArr) {
				terminalSet.add(s);
			}
			fr.read();	//Read \n

			// 3. Read Rule
			sb = new StringBuilder();
			int k;
			Set<Character> pauseSet = new HashSet<Character>();
			pauseSet.add('|');
			pauseSet.add('`');
			pauseSet.add('\r');
			pauseSet.add(':');
			String leftPart = null;
			ArrayList<String> subRightPart = new ArrayList<>();
			ArrayList<ArrayList<String>> rightPart = new ArrayList<>();
			int state = 0;	// 0 means left part; 1 means right part;
			while((k = fr.read()) != -1) {
				current = (char)k;
				if (pauseSet.contains(current)) {
					String tmp = sb.toString();
					if (nonTerminalSet.contains(tmp) || terminalSet.contains(tmp)) {
						if (state == 1) {
							subRightPart.add(tmp);
						} else if (state == 0) {
							leftPart = tmp;
							state = 1;
						}
						sb = new StringBuilder();
					} else {
						throw new SyntaxException("[Error] Unexpected symbol "+tmp);
					}
					switch (current) {
						case ':': {
							if((char)fr.read() != ':') {
								throw new SyntaxException("[Error] There is an error around :");
							}
							break;
						}
						case '\r': {
							// New rule
							if((char)fr.read() != '\n') {
								throw new SyntaxException("[Error] There is an error around \r");
							} else {
								rightPart.add(subRightPart);
								deduceMap.put(leftPart, rightPart);
								rightPart = new ArrayList<>();
								subRightPart = new ArrayList<>();
								state = 0;
							}
							break;
						}
						case '|': {
							// Save sub part
							if(state == 0) {
								throw new SyntaxException("[Error] Encounter | at left part");
							}
							rightPart.add(subRightPart);
							subRightPart = new ArrayList<>();
							break;
						}
						case '`': {
							break;
						}
					}
				} else {
					sb.append(current);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 4. Init Follow/Select List
		this.keySet = deduceMap.keySet();
		for(String s : keySet) {
			follow.put(s, new HashSet<String>());
			select.put(s, new ArrayList<HashSet<String>>());
		}

		// 5. Add EOL into terminal list
		terminalSet.add(EOL);	
	}

	private void leftRecursiveElimination(){
		ArrayList<String> traverse = new ArrayList<>(this.keySet);
		int keyNumbers = traverse.size();
		// For each key
		for(int i=0; i< keyNumbers; i++) {
			// 1. Replace Head Terminal Symbol
			String leftPart = traverse.get(i);
			ArrayList<ArrayList<String>> eachRightPart = deduceMap.get(leftPart);
			int eachRightPartLength = eachRightPart.size();
			// For each right part of same left part
			for(int j=0; j<eachRightPartLength; j++) {
				ArrayList<String> eachSubRightPart = eachRightPart.get(j);
				if (nonTerminalSet.contains(eachSubRightPart.get(0))) {
					// For each previous right part
					for(int k=0; k<i; k++) {
						String prevLeftPart = traverse.get(k);
						if(prevLeftPart.equals(eachSubRightPart.get(0))) {
							ArrayList<ArrayList<String>> prevRightPart = deduceMap.get(prevLeftPart);
							// Replace each one
							// Previous sub right part (right part of a same left part) of a right Part
							int previousRightPartLength = prevRightPart.size();
							for(int m=0; m< previousRightPartLength; m++) {
								ArrayList<String> prevSubRightPart = prevRightPart.get(m);
								ArrayList<String> clonedSubRightPart = (ArrayList<String>)eachSubRightPart.clone();
								clonedSubRightPart.remove(0);
								clonedSubRightPart.addAll(0, prevSubRightPart);
								eachRightPart.add(clonedSubRightPart);
							}
							eachRightPart.remove(j);
						}
					}
				}
			}
			
			// 2. Remove Left Recursion

			// 2.1 Get alpha and beta part
			eachRightPartLength = eachRightPart.size();
			ArrayList<ArrayList<String>> alphaList = new ArrayList<>();
			ArrayList<ArrayList<String>> betaList = new ArrayList<>();
			for(int j=0; j<eachRightPartLength; j++) {
				ArrayList<String> eachSubRightPart = eachRightPart.get(j);
				if(leftPart.equals(eachSubRightPart.get(0))) {
					// Same left part string and right sub part header string
					alphaList.add(eachSubRightPart);
				} else {
					betaList.add(eachSubRightPart);
				}
			}

			// Whether to erase left recursion
			if (alphaList.size() > 0) {
				// 2.2 S -> beta S'
				ArrayList<ArrayList<String>> original = deduceMap.get(leftPart);
				original.clear();
				int betaListLength = betaList.size();
				for(int j=0; j<betaListLength; j++) {
					ArrayList<String> newSubRightPart = new ArrayList<>();
					newSubRightPart.addAll(betaList.get(j));
					newSubRightPart.add(leftPart+'\'');
					original.add(newSubRightPart);
				}
				nonTerminalSet.add(leftPart + '\'');
				
				// 2.3 S' -> S' | epsilon
				int alphaListLength = alphaList.size();
				ArrayList<ArrayList<String>> newRightPartList = new ArrayList<>();
				for(int j=0; j<alphaListLength; j++) {
					ArrayList<String> newSubRightPart = new ArrayList<>();
					ArrayList<String> tmpAlphaList = alphaList.get(j);
					tmpAlphaList.remove(0);
					newSubRightPart.addAll(tmpAlphaList);
					newSubRightPart.add(leftPart+'\'');
					newRightPartList.add(newSubRightPart);
				}
				ArrayList<String> emptyLastList = new ArrayList<>();
				emptyLastList.add("@");
				newRightPartList.add(emptyLastList);
				deduceMap.put(leftPart+'\'', newRightPartList);	
			}

			// 3. Put epsilon to the end;
			for(int j=0; j<eachRightPartLength; j++) {
				ArrayList<String> eachSubList = eachRightPart.get(j);
				if(eachSubList.size() == 1 && eachSubList.get(0).equals(EPSILON)) {
					eachRightPart.remove(j);
					ArrayList<String> emptyLastList = new ArrayList<>();
					emptyLastList.add("@");
					eachRightPart.add(emptyLastList);
					break;
				}
			}
		}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
	}

	private void extractSameLeftElement() {
		ArrayList<String> samePart = new ArrayList<>();
		for(String key: keySet) {
			ArrayList<ArrayList<String>> rightPart = deduceMap.get(key);
			int length = rightPart.size();
			if(length == 1) {
				continue;
			}
			int pos = 0;
			String mem;
			int sameFlag = 1;
			// Seach same part
			while(true) {
				mem = null;
				sameFlag = 1;
				for(int i=0; i<length; i++) {
					try {
						if (mem == null) {
							mem = rightPart.get(i).get(pos);
						} else {
							if(! rightPart.get(i).get(pos).equals(mem)) {
								sameFlag = 0;
								break;
							}
						}
					} catch (IndexOutOfBoundsException iobe) {
						break;
					}
				}
				if (sameFlag == 0) {
					break;
				} else {
					samePart.add(mem);
				}
				pos += 1;
			}
			// Extract same part
			if (samePart.size() > 0) {
				String newKey = key+"\'";
				for(ArrayList<String> subRightPart: rightPart) {
					for(int i=0; i<pos; i++) {
						subRightPart.remove(0);
					}
					subRightPart.add(0, newKey);
				}
				nonTerminalSet.add(newKey);
				ArrayList<ArrayList<String>> impl = new ArrayList<>();
				impl.add(new ArrayList<String>(samePart));
				deduceMap.put(newKey, impl);
			}
		}
	}

	private boolean recursiveTopDownSyntaxAnalyzer(List<Tuple<String, Integer>> tupleList, String startSymbol) {
		ArrayList<ArrayList<String>> rightPart = deduceMap.get(startSymbol);
		int rightPartLength = rightPart.size();
		boolean result = false;
		// Each possible right part
		for(int i=0; i<rightPartLength; i++) {
			ArrayList<String> subRightPart = rightPart.get(i);
			int subRightPartLength = subRightPart.size();
			boolean breakflag = false;
			// Each Symbol
			for(int k=0; k<subRightPartLength; k++) {
				String eachSymbol = subRightPart.get(k);
				// Already finished detection...
				if(this.analyzePosition >= tupleList.size()) {
					// If current eachSymbol is terminal symbol, it must be EPSILON
					if (terminalSet.contains(eachSymbol)) {
						if (eachSymbol.equals(EPSILON)) {
							System.out.println(EPSILON);
							result = true;
							continue;	// Check if next symbol is EPSILON or not.
						} else {
							return false;
						}
					} else {
						boolean containsEpsilon = false;
						// If current symbol deduces ESPILON, return true.
						for (ArrayList<String> cmpRightPart : this.deduceMap.get(eachSymbol)) {
							if (cmpRightPart.size() == 1) {
								for (String s : cmpRightPart) {
									if (s.equals(EPSILON)) {
										containsEpsilon = true;
										break;
									}
								}
							}
						}
						if (containsEpsilon == true) {
							System.out.println(EPSILON);
							return true;
						} else {
							result = recursiveTopDownSyntaxAnalyzer(tupleList, eachSymbol);
							if (result == false) {
								return false;
							}
						}
					}
				}
				
				// Still trying detection...
				if(terminalSet.contains(eachSymbol)) {
					// Same symbol, or same type
					if((eachSymbol.equals(tupleList.get(this.analyzePosition).getPos1()) || 
					eachSymbol.equals(symbolTypeRef.get(tupleList.get(this.analyzePosition).getPos2())))) {
						System.out.println(eachSymbol);
						this.analyzePosition++;
						result = true;
						breakflag = true;
					} else if (eachSymbol.equals(EPSILON)){
						System.out.println(eachSymbol);
						result = true;
						breakflag = true;
					} else {
						break;
					}
				} else {
					result = recursiveTopDownSyntaxAnalyzer(tupleList, eachSymbol);
					if(result == false){
						if(this.analyzePosition >= tupleList.size()) {
							return false;
						}
						break;
					}
				}
			}

			if(result == true || breakflag == true) {
				break;
			}
		}
		return result;
	}

	private ArrayList<Tuple<String, Integer>> readWordTupleFromFile(String filePath) {
		File fp = new File(filePath);
		ArrayList<Tuple<String, Integer>> tupleList = new ArrayList<>();
		try (FileReader fr = new FileReader(fp)) {
			char c; int k;
			Pattern tuplePattern = Pattern.compile("<(.*),(.*)>");
			StringBuilder sb = new StringBuilder();
			while((k = fr.read()) != -1) {
				c = (char)k;
				if(c == '\r') {
					if(fr.read() != (int)'\n') {
						throw new SyntaxException("[Error] There is an error with your word tuple file.");
					} else {
						Matcher tmpMatcher = tuplePattern.matcher(sb.toString());
						if(tmpMatcher.matches()) {
							try {
								tupleList.add(new Tuple<String, Integer>(tmpMatcher.group(1), Integer.parseInt(tmpMatcher.group(2).trim())));
							} catch (Exception e) {
								throw new SyntaxException("[Error] There is an error with your word tuple file.");
							}
						} else {
							throw new SyntaxException("[Error] There is an error with your word tuple file.");
						}
						sb = new StringBuilder();
					}
				} else {
					sb.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return tupleList;
	}

	private void debugger() {
		Tuple<String, Integer> k = new Tuple<String,Integer>("A", 2);
		Tuple<String, Integer> m = new Tuple<String,Integer>("A", 2);
		System.out.println(k.equals(m));
	}

	/**
	 * <p>Get first list of given input.</p>
	 * <p>Example:</p>
	 * <p>calculateFirstList({"A", "B", "C"});</p>
	 * @param inputList An array of all symbols
	 * @return First list of given input list
	 */
	private HashSet<String> calculateFirstList(ArrayList<String> inputList) {
		HashSet<String> totalResult = new HashSet<>();
		HashSet<String> tmpResult = new HashSet<>();
		for(String symbol : inputList) {
			getTerminalRecursive(symbol, tmpResult);
			totalResult.addAll(tmpResult);
			if(! tmpResult.contains(EPSILON)) {
				break;
			}
		}
		return totalResult;
	}

	private void buildFirst() {
		for(String currentKey: this.keySet) {
			HashSet<String> resultList = new HashSet<>();
			getTerminalRecursive(currentKey, resultList);
			this.first.put(currentKey, resultList);
		}
	}

	private int getTerminalRecursive(String symbol, HashSet<String> resultSet) {
		if(terminalSet.contains(symbol)) {
			resultSet.add(symbol);
			return 0;
		}
		ArrayList<ArrayList<String>> rightPart = deduceMap.get(symbol);
		for(ArrayList<String> subRightPart : rightPart) {
			for(String str : subRightPart) {
				if(str.equals(EPSILON)) {
					resultSet.add(EPSILON);
					return 1;
				}
				else if(terminalSet.contains(str)) {
					resultSet.add(str);
					break;
				} else {
					int hasEspilon = getTerminalRecursive(str, resultSet);
					if(hasEspilon == 0) {
						break;
					}
				}
			}
		}
		return 0;
	}

	private void buildFollow(String start) {
		int change = 1;
		HashSet<String> tmp = new HashSet<>();
		tmp.add(EOL);
		follow.put(start, tmp);
		// Iterater again and again until no changes occur
		while(change == 1) {
			change = 0;
			// Insert and check difference.
			int prevHashCode = follow.hashCode();

			for(String leftPart: this.keySet) {
				ArrayList<ArrayList<String>> rightPart = deduceMap.get(leftPart);
				for(ArrayList<String> subRightPart : rightPart) {
					// Iterate every expression
					int length = subRightPart.size();
					for(int i=0; i<length; i++) {
						String currentSymbol = subRightPart.get(i);
						// Skip terminal symbol
						if(terminalSet.contains(currentSymbol)) {
							continue;
						}
						HashSet<String> beta = null;

						// Get beta
						if(i < length - 1) {
							beta = calculateFirstList(new ArrayList<String>(subRightPart.subList(i+1, length)));
						}
						if (beta != null) {
							// A -> alpha B beta
							// ==> Follow(B) <- First(beta)
							if(beta.contains(EPSILON)) {
								beta.remove(EPSILON);
								// A -> alpha B beta(contains NULL)
								// Follow(B) <- Follow(A)
								follow.get(currentSymbol).addAll(follow.get(leftPart));
							}
							follow.get(currentSymbol).addAll(beta);
						} else if(i == length - 1) {
							follow.get(currentSymbol).addAll(follow.get(leftPart));
						}
					}	//Right part
				}	//Subright part
			}	//Keyset iter

			if(prevHashCode != follow.hashCode()) {
				change = 1;
			}
		}
	}

	private void buildSelect(){
		for(String key : keySet) {
			ArrayList<ArrayList<String>> rightPart = deduceMap.get(key);
			int length = rightPart.size();
			for(int i=0; i<length; i++) {
				ArrayList<String> subRightPart = rightPart.get(i);
				HashSet<String> tmpFirst = calculateFirstList(subRightPart);
				if(! tmpFirst.contains(EPSILON)) {
					// If A->alpha, and alpha -x-> EPSILON
					// ==> Select(A) = First(alpha)
					this.select.get(key).add(tmpFirst);
				} else {
					// If A->alpha, and alpha -*-> EPSILON (possible)
					// ==> Select(A) = First(alpha) U Follow(A)
					HashSet<String> tmpFollow = this.follow.get(key);
					HashSet<String> resultSet = new HashSet<>();
					resultSet.addAll(tmpFollow);
					resultSet.addAll(tmpFirst);
					this.select.get(key).add(resultSet);
				}
			}
		}
	}

	private boolean judgeLL1() {
		for(String str: keySet) {
			ArrayList<ArrayList<String>> rightPart = deduceMap.get(str);
			int length = rightPart.size();
			HashSet<String> previous = this.select.get(str).get(0);
			for(int i=1; i<length; i++) {
				HashSet<String> current = this.select.get(str).get(i);
				HashSet<String> clonedCurrent = (HashSet<String>)current.clone();
				clonedCurrent.retainAll(previous);
				if(! clonedCurrent.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean LL1SyntaxAnalyzer(String start, ArrayList<String> input) {
		
		// Parse
		Deque<String> stack = new ArrayDeque<>();
		int length = input.size();
		int pos = 0;
		stack.add(start);
		while(pos < length) {
			String target = input.get(pos);
			String stackTopSymbol = stack.peek();
			if(terminalSet.contains(stackTopSymbol)) {
				if(stackTopSymbol.equals(EPSILON)) {
					stack.pop();
					if(target.equals("$") && stack.isEmpty()) {
						break;
					}
					continue;
				}
				if (stackTopSymbol.equals(input.get(pos))) {
					System.out.println(stackTopSymbol);
					stack.pop();
					pos++;
					continue;
				} else {
					return false;
				}
			}

			ArrayList<ArrayList<String>> rightPart = deduceMap.get(stackTopSymbol);
			int rightPartLength = rightPart.size();
			int match = 0;
			for(int i=0; i<rightPartLength; i++) {
				HashSet<String> tmpSelect = select.get(stackTopSymbol).get(i);
				if(tmpSelect.contains(target)) {
					stack.pop();
					match = 1;
					ArrayList<String> subRightPart = rightPart.get(i);
					int symbolLength = subRightPart.size();
					for(int k=symbolLength-1; k>=0; k--){
						stack.push(subRightPart.get(k));
					}
					break;
				}
			}
			if (match == 0) {
				break;
			}
		}
		if(stack.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private ArrayList<String> convertWordTupleToArrayList(ArrayList<Tuple<String, Integer>> inputTuple) {
		// Convert input
		ArrayList<String> input = new ArrayList<>();
		for(Tuple<String, Integer> tuple : inputTuple) {
			String ref = this.symbolTypeRef.get(tuple.getPos2());
			if(ref != null) {
				input.add(ref);
			} else {
				input.add(tuple.getPos1());
			}
		}
		input.add(EOL);
		return input;
	}

	public static void main(String[] args) {
		SyntaxAnalyzer sa = new SyntaxAnalyzer();

		/* LL1 Analyzer Test */
		/*
		sa.init("./other/deduce.txt");
		sa.extractSameLeftElement();
		sa.leftRecursiveElimination();
		sa.buildFirst();
		sa.buildFollow("E");
		sa.buildSelect();
		System.out.println("DeduceMap:\n"+sa.deduceMap);
		System.out.println("First:\n"+sa.first);
		System.out.println("Follow:\n"+sa.follow);
		System.out.println("Select:\n"+sa.select);
		System.out.println("Is LL1?: "+sa.judgeLL1());

		sa.symbolTypeRef.put(WordCodec.INTEGER.ordinal(), "i");

		ArrayList<Tuple<String, Integer>> tupleList = sa.readWordTupleFromFile("./other/log.txt");
		ArrayList<String> input = sa.convertWordTupleToArrayList(tupleList);
		boolean res = sa.LL1SyntaxAnalyzer("E", input);
		System.out.println(res);
		*/


		/* Extract Left Element Test*/
		/*
		sa.init("./other/deduce2.txt");
		sa.extractSameLeftElement();
		*/

		sa.init("./other/deduce.txt");
		sa.symbolTypeRef.put(WordCodec.INTEGER.ordinal(), "i");
		ArrayList<Tuple<String, Integer>> tupleList = sa.readWordTupleFromFile("./other/log.txt");
		System.out.println(sa.recursiveTopDownSyntaxAnalyzer(tupleList, "E"));
	}
}