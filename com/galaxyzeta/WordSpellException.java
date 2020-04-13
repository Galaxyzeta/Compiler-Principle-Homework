package com.galaxyzeta;

public class WordSpellException extends Exception {

	private String errorMessage;
	private int lineNumber;
	private String errorString;

	WordSpellException(String errorMessage, String errorString, int lineNumber){
		this.errorMessage = errorMessage;
		this.errorString = errorString;
		this.lineNumber = lineNumber;
	}
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void printStackTrace() {
		System.out.println(String.format("[CRITICAL]You have an error in your program in LINE %d.\n--Error String: %s\n--Reason: %s", lineNumber , errorString, errorMessage));
	}

}