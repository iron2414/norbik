package org.arrostia.mdp;

import java.util.HashMap;
import java.util.Map;

public abstract class Features<A, C extends Comparable<? super C>, S> {
	protected final SearchProblem<A, C, S> problem;
	private final Map<Object, C> weights=new HashMap<>();

	public Features(SearchProblem<A, C, S> problem) {
		this.problem=problem;
	}
	
	public void appoximateQLearn(S state, A action, S successorState,
			C reward, C alpha) {
		C value=null;
		for (A successorAction: problem.actions(successorState)) {
			C value2=qValue(successorState, successorAction);
			if ((null==value)
					|| (0>value.compareTo(value2))) {
				value=value2;
			}
		}
		if (null==value) {
			value=problem.zero();
		}
		value=problem.multiply(value, problem.gamma());
		value=problem.add(value, reward);
		value=problem.subtract(value, qValue(state, action));
		value=problem.multiply(value, alpha);
		for (Map.Entry<String, C> entry: features(state, action).entrySet()) {
			String feature=entry.getKey();
			C value2=entry.getValue();
			value2=problem.multiply(value2, value);
			value2=problem.add(value2, weight(feature));
			weight(feature, value2);
		}
	}
	
	public abstract Map<String, C> features(S state, A action);
	
	public A policy(S state) {
		A bestAction=null;
		C bestQValue=null;
		for (A action: problem.actions(state)) {
			C qValue=qValue(state, action);
			if ((null==bestQValue)
					|| (0>bestQValue.compareTo(qValue))) {
				bestAction=action;
				bestQValue=qValue;
			}
		}
		return bestAction;
	}
	
	public C qValue(S state, A action) {
		C result=problem.zero();
		for (Map.Entry<String, C> entry: features(state, action).entrySet()) {
			String feature=entry.getKey();
			C value=entry.getValue();
			value=problem.multiply(value, weight(feature));
			result=problem.add(result, value);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return weights.toString();
	}
	
	public C value(S state) {
		C best=null;
		for (A action: problem.actions(state)) {
			C qValue=qValue(state, action);
			if ((null==best)
					|| (0>best.compareTo(qValue))) {
				best=qValue;
			}
		}
		return best;
	}
	
	public C weight(Object feature) {
		C weight=weights.get(feature);
		if (null==weight) {
			weight=problem.zero();
		}
		return weight;
	}
	
	public void weight(Object feature, C weight) {
		weights.put(feature, weight);
	}
}
