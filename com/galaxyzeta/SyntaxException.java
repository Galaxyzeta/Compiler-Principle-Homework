package com.galaxyzeta;

public class SyntaxException extends Exception {
	
	private static final long serialVersionUID = -4805764102288415520L;
	private String errorString;

	SyntaxException(String errorString) {
		this.errorString = errorString;
	}
	
	@Override
	public void printStackTrace() {
		System.out.println(errorString);
	}
}