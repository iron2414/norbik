package org.arrostia.minimax;

import java.util.Objects;

public class Successor<A, S> {
	private final A action;
	private final S state;

	public Successor(A action, S state) {
		this.action=action;
		this.state=state;
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
		Successor<?, ?> successor=(Successor<?, ?>)other;
		return Objects.equals(action, successor.action)
				&& Objects.equals(state, successor.state);
	}

	public A getAction() {
		return action;
	}

	public S getState() {
		return state;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(action, state);
	}

	@Override
	public String toString() {
		return action+"->"+state;
	}
}
