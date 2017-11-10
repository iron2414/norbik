package org.arrostia.mdp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class AbstractSearchProblem<A,
		C extends Comparable<? super C>, S> implements SearchProblem<A, C, S> {
	private final Map<S, Map<A, Map<S, C>>> states=new LinkedHashMap<>();

	@Override
	public Collection<A> actions(S state) {
		Map<A, ?> actions=states.get(state);
		if (null==actions) {
			return new LinkedList<>();
		}
		return actions.keySet();
	}
	
	protected void add(S state, A action, S successorState, C probability) {
		Map<A, Map<S, C>> actions=states.get(state);
		if (null==actions) {
			actions=new LinkedHashMap<>();
			states.put(state, actions);
		}
		Map<S, C> successorStates=actions.get(action);
		if (null==successorStates) {
			successorStates=new LinkedHashMap<>();
			actions.put(action, successorStates);
		}
		successorStates.put(successorState, probability);
	}

	@Override
	public Map<S, C> probabilities(S state, A action) {
		Map<A, Map<S,C>> actions=states.get(state);
		if (null==actions) {
			return new LinkedHashMap<>();
		}
		return actions.get(action);
	}

	@Override
	public Collection<S> states() {
		return states.keySet();
	}
}
