package com.galaxyzeta.entity;

/**
 * Word lexual Tuple.
 */
public class Tuple<M,N> {
	private M pos1;
	private N pos2;
	public Tuple(M pos1, N pos2){
		this.pos2 = pos2;
		this.pos1 = pos1;
	}
	/**
	 * @return the pos1
	 */
	public M getPos1() {
		return pos1;
	}
	/**
	 * @return the pos2
	 */
	public N getPos2() {
		return pos2;
	}
	/**
	 * @param pos1 the pos1 to set
	 */
	public void setPos1(M pos1) {
		this.pos1 = pos1;
	}
	/**
	 * @param pos2 the pos2 to set
	 */
	public void setPos2(N pos2) {
		this.pos2 = pos2;
	}

	@Override
	public String toString() {
		return String.format("<%s, %s>", this.pos1, this.pos2);
	}

	@Override
	public int hashCode() {
		return this.pos1.hashCode() + this.pos2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Tuple<M,N> k = (Tuple<M,N>)obj;
			if(k.pos1.equals(this.pos1) && k.pos2.equals(this.pos2)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}