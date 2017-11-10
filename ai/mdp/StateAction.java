package org.arrostia.mdp;

import java.util.Objects;

public class StateAction<A, S> {
	private final A action;
	private final S state;

	public StateAction(A action, S state) {
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
		StateAction<?, ?> stateAction=(StateAction<?, ?>)other;
		return Objects.equals(action, stateAction.action)
				&& Objects.equals(state, stateAction.state);
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
		return "("+state+", "+action+")";
	}
}
