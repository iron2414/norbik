package org.arrostia.mdp;

import java.util.Map;

public class MDPSolver<A, C extends Comparable<? super C>, S> {
	private final SearchProblem<A, C, S> problem;

	public MDPSolver(SearchProblem<A, C, S> problem) {
		this.problem=problem;
	}
	
	private boolean isSame(QValues<A, C, S> qValues0,
			QValues<A, C, S> qValues1, C epsilon) {
		if (qValues0.size()!=qValues1.size()) {
			return false;
		}
		for (StateAction<A, S> stateAction: qValues0.stateActions()) {
			S state=stateAction.getState();
			A action=stateAction.getAction();
			C qValue0=qValues0.get(state, action);
			C qValue1=qValues1.get(state, action);
			if (null==qValue1) {
				return false;
			}
			C delta=problem.abs(problem.subtract(qValue0, qValue1));
			if (0<=delta.compareTo(epsilon)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isSame(Values<C, S> values0, Values<C, S> values1,
			C epsilon) {
		if (values0.size()!=values1.size()) {
			return false;
		}
		for (S state: values0.states()) {
			C value0=values0.get(state);
			C value1=values1.get(state);
			if (null==value1) {
				return false;
			}
			C delta=problem.abs(problem.subtract(value0, value1));
			if (0<=delta.compareTo(epsilon)) {
				return false;
			}
		}
		return true;
	}
	
	public Policy<A, S> policy(QValues<A, C, S> qValues) {
		Policy<A, S> result=new Policy<>();
		for (S state: problem.states()) {
			A bestAction=null;
			C bestQValue=null;
			for (A action: problem.actions(state)) {
				C qValue=qValues.get(state, action, problem.zero());
				if ((null==bestQValue)
						|| (0>bestQValue.compareTo(qValue))) {
					bestAction=action;
					bestQValue=qValue;
				}
			}
			if (null!=bestAction) {
				result.set(state, bestAction);
			}
		}
		return result;
	}
	
	public Values<C, S> policyEvaluation(Policy<A, S> policy, C epsilon) {
		Values<C, S> values=new Values<>();
		while (true) {
			Values<C, S> newValues=new Values<>();
			for (S state: problem.states()) {
				A action=policy.get(state);
				if (null==action) {
					newValues.set(state, problem.zero());
				}
				else {
					C qValue=qValue(values, state, action);
					newValues.set(state, qValue);
				}
			}
			if (isSame(values, newValues, epsilon)) {
				return newValues;
			}
			values=newValues;
		}
	}
	
	public Policy<A, S> policyExtraction(Values<C, S> values) {
		Policy<A, S> result=new Policy<>();
		for (S state: problem.states()) {
			A bestAction=null;
			C bestQValue=null;
			for (A action: problem.actions(state)) {
				C qValue=qValue(values, state, action);
				if ((null==bestAction)
						|| (0>bestQValue.compareTo(qValue))) {
					bestAction=action;
					bestQValue=qValue;
				}
			}
			if (null!=bestAction) {
				result.set(state, bestAction);
			}
		}
		return result;
	}
	
	public ValuesPolicy<A, C, S> policyIteration(C epsilon) {
		Values<C, S> values=new Values<>();
		Policy<A, S> policy=policyExtraction(new Values<C, S>());
		while (true) {
			Values<C, S> newValues=policyEvaluation(policy, epsilon);
			Policy<A, S> newPolicy=policyExtraction(newValues);
			if (isSame(values, newValues, epsilon)) {
				return new ValuesPolicy<>(newPolicy, newValues);
			}
			policy=newPolicy;
			values=newValues;
		}
	}
	
	/*public static <A, C extends Comparable<? super C>, S> void qLearning(
			SearchProblem<A, C, S> problem,
			Map<StateAction<A, S>, C> qValuesToUpdate, S state, A action,
			S successorState, C reward, C alpha) {
		StateAction<A, S> stateAction=new StateAction<>(action, state);
		C value=qValuesToUpdate.get(stateAction);
		if (null==value) {
			value=problem.zeroCost();
		}
		C successorValue=
		
	}*/
	
	public C qValue(Values<C, S> values, S state, A action) {
		C qValue=problem.zero();
		for (Map.Entry<S, C> entry:
				problem.probabilities(state, action).entrySet()) {
			S successorState=entry.getKey();
			C probability=entry.getValue();
			C reward=problem.reward(state, action, successorState);
			C gamma=problem.gamma();
			C value=values.get(successorState, problem.zero());
			qValue=problem.add(
					qValue,
					problem.multiply(
						probability,
						problem.add(
							reward,
							problem.multiply(
								gamma,
								value))));
		}
		return qValue;
	}
	
	public QValues<A, C, S> qValueIteration(C epsilon) {
		QValues<A, C, S> qValues=new QValues<>();
		while (true) {
			Policy<A, S> policy=policy(qValues);
			Values<C, S> values=values(qValues, policy);
			QValues<A, C, S> newQValues=qValues(values, policy);
			if (isSame(qValues, newQValues, epsilon)) {
				return newQValues;
			}
			qValues=newQValues;
		}
	}
	
	public QValues<A, C, S> qValues(Values<C, S> values,
			Policy<A, S> policy) {
		QValues<A, C, S> result=new QValues<>();
		for (S state: problem.states()) {
			for (A action: problem.actions(state)) {
				result.set(state, action, qValue(values, state, action));
			}
		}
		return result;
	}
	
	public ValuesPolicy<A, C, S> valueIteration(C epsilon) {
		QValues<A, C, S> qValues=qValueIteration(epsilon);
		Policy<A, S> policy=policy(qValues);
		Values<C, S> values=values(qValues, policy);
		return new ValuesPolicy<>(policy, values);
	}
	
	public Values<C, S> values(QValues<A, C, S> qValues,
			Policy<A, S> policy) {
		Values<C, S> result=new Values<>();
		for (S state: problem.states()) {
			A action=policy.get(state);
			if (null!=action) {
				result.set(state,
						qValues.get(state, action, problem.zero()));
			}
		}
		return result;
	}
}
