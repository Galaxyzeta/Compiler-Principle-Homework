package com.galaxyzeta.entity;

/**
 * Word lexual ParseResult.
 */
public class ParseResult {
	private int codec;
	private String string;
	public ParseResult(String string, int codec){
		this.string = string;
		this.codec = codec;
	}
	/**
	 * @return the codec
	 */
	public int getCodec() {
		return codec;
	}
	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}
	/**
	 * @param codec the codec to set
	 */
	public void setCodec(int codec) {
		this.codec = codec;
	}
	/**
	 * @param string the string to set
	 */
	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String toString() {
		return String.format("<%s, %s>", this.string, this.codec);
	}
}