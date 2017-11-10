package org.arrostia.minimax;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AlphaBetaPruningSearch<A, C extends Comparable<? super C>, S>
		extends AbstractSearch<A, C, S> {
	public AlphaBetaPruningSearch(SearchProblem<A, C, S> problem) {
		super(problem);
	}
	
	private static <C extends Comparable<? super C>> boolean lessOrEquals(
			C value0, C value1) {
		int cc=value0.compareTo(value1);
		return 0>=cc;
	}
	
	private static <C extends Comparable<? super C>> C max(C value0,
			C value1) {
		int cc=value0.compareTo(value1);
		if (0<=cc) {
			return value0;
		}
		return value1;
	}
	
	private static <C extends Comparable<? super C>> C min(C value0,
			C value1) {
		int cc=value0.compareTo(value1);
		if (0>=cc) {
			return value0;
		}
		return value1;
	}

	@Override
	public Result<A, C, S> search(int depth) {
		S state=problem.getStartState();
		if (0>=depth) {
			return new Result<>(
					problem.getScore(state),
					new HashMap<Successor<A, S>, C>());
		}
		Map<Successor<A, S>, C> childScores=new LinkedHashMap<>();
		for (Successor<A, S> successor: problem.getSuccessorStates(state)) {
			S state2=successor.getState();
			C score=search(depth-1, state2, new ExtendedCost<C>(true),
					new ExtendedCost<C>(false));
			childScores.put(successor, score);
		}
		return new Result<>(
				problem.getScore(state, childScores),
				childScores);
	}
	
	private C search(int depth, S state, ExtendedCost<C> alpha,
			ExtendedCost<C> beta) {
		if (0>=depth) {
			return problem.getScore(state);
		}
		Collection<Successor<A, S>> successors
				=problem.getSuccessorStates(state);
		if (successors.isEmpty()) {
			return problem.getScore(state);
		}
		switch (problem.getChoiceType(state)) {
			case MAX:
				for (Successor<A, S> successor: successors) {
					C score=search(depth-1, successor.getState(), alpha,
							beta);
					alpha=max(alpha, new ExtendedCost<>(score));
					if (lessOrEquals(beta, alpha)) {
						break;
					}
				}
				return alpha.getRegularScore();
			case MIN:
				for (Successor<A, S> successor: successors) {
					C score=search(depth-1, successor.getState(), alpha,
							beta);
					beta=min(beta, new ExtendedCost<>(score));
					if (lessOrEquals(beta, alpha)) {
						break;
					}
				}
				return beta.getRegularScore();
			case UNKNOWN:
			default:
				Map<Successor<A, S>, C> childScores=new LinkedHashMap<>();
				for (Successor<A, S> successor: successors) {
					S state2=successor.getState();
					C score=search(depth-1, state2, alpha, beta);
					childScores.put(successor, score);
				}
				return problem.getScore(state, childScores);
		}
	}
}
