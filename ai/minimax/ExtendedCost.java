package org.arrostia.minimax;

import java.util.Objects;

public class ExtendedCost<C extends Comparable<? super C>>
		implements Comparable<ExtendedCost<C>>{
	private final boolean infinite;
	private final boolean negative;
	private final C regularScore;

	public ExtendedCost(boolean negative) {
		this.infinite=true;
		this.negative=negative;
		this.regularScore=null;
	}

	public ExtendedCost(C regularScore) {
		this.infinite=false;
		this.negative=false;
		this.regularScore=Objects.requireNonNull(regularScore);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this==other) {
			return true;
		}
		if ((null==other)
				|| (!getClass().equals(other.getClass()))) {
			return false;
		}
		ExtendedCost<?> extendedCost=(ExtendedCost<?>)other;
		return (infinite==extendedCost.infinite)
				&& (negative==extendedCost.negative)
				&& Objects.equals(regularScore, extendedCost.regularScore);
	}

	@Override
	public int compareTo(ExtendedCost<C> extendedCost) {
		if (infinite) {
			return compareToInfinite(extendedCost);
		}
		if (extendedCost.infinite) {
			return -1*extendedCost.compareToInfinite(this);
		}
		return regularScore.compareTo(extendedCost.regularScore);
	}
	
	private int compareToInfinite(ExtendedCost<C> extendedCost) {
		if (negative) {
			if (extendedCost.infinite
					&& extendedCost.negative) {
				return 0;
			}
			return -1;
		}
		if (extendedCost.infinite
				&& (!extendedCost.negative)) {
			return 0;
		}
		return 1;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(infinite, negative, regularScore);
	}

	public C getRegularScore() {
		return regularScore;
	}
	
	@Override
	public String toString() {
		if (infinite) {
			if (negative) {
				return "-inf";
			}
			else {
				return "inf";
			}
		}
		else {
			return regularScore.toString();
		}
	}
}
