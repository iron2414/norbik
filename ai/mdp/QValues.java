package org.arrostia.mdp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class QValues<A, C extends Comparable<? super C>, S> {
	private final Map<S, Map<A, C>> qValues;
	
	public QValues() {
		qValues=new LinkedHashMap<>();
	}
	
	public QValues(QValues<A, C, S> qValues) {
		this.qValues=new LinkedHashMap<>(qValues.qValues.size());
		for (Map.Entry<S, Map<A, C>> entry: qValues.qValues.entrySet()) {
			this.qValues.put(entry.getKey(),
					new LinkedHashMap<>(entry.getValue()));
		}
	}
	
	public Collection<A> actions(S state) {
		return new LinkedList<>(get(state).keySet());
	}
	
	private Map<A, C> get(S state) {
		Map<A, C> result=qValues.get(state);
		if (null==result) {
			result=new LinkedHashMap<>();
			qValues.put(state, result);
		}
		return result;
	}
	
	public C get(S state, A action) {
		return get(state).get(action);
	}
	
	public C get(S state, A action, C defaultValue) {
		C result=get(state, action);
		if (null==result) {
			return defaultValue;
		}
		return result;
	}
	
	public void qLearn(SearchProblem<A, C, S> problem, S state, A action,
			S successorState, C reward, C alpha) {
		C oldPart=get(state, action, problem.zero());
		oldPart=problem.multiply(oldPart,
				problem.subtract(problem.one(), alpha));
		C newPart=null;
		for (A successorAction: actions(successorState)) {
			C value=get(successorState, successorAction,
					problem.zero());
			if ((null==newPart)
					|| (0>newPart.compareTo(value))) {
				newPart=value;
			}
		}
		if (null==newPart) {
			newPart=problem.zero();
		}
		newPart=problem.multiply(newPart, problem.gamma());
		newPart=problem.add(newPart, reward);
		newPart=problem.multiply(newPart, alpha);
		set(state, action, problem.add(oldPart, newPart));
	}
	
	public void remove(S state, A action) {
		get(state).remove(action);
	}
	
	public void set(S state, A action, C value) {
		get(state).put(action, value);
	}
	
	public int size() {
		int result=0;
		for (Map<?, ?> map: qValues.values()) {
			result+=map.size();
		}
		return result;
	}
	
	public Collection<StateAction<A, S>> stateActions() {
		Collection<StateAction<A, S>> result=new LinkedList<>();
		for (S state: qValues.keySet()) {
			for (A action: qValues.get(state).keySet()) {
				result.add(new StateAction<>(action, state));
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		return qValues.toString();
	}
}
