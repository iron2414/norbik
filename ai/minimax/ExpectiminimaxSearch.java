package org.arrostia.minimax;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpectiminimaxSearch<A, C extends Comparable<? super C>, S>
		extends AbstractSearch<A, C, S> {
	public ExpectiminimaxSearch(SearchProblem<A, C, S> problem) {
		super(problem);
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
			C score=search(depth-1, state2);
			childScores.put(successor, score);
		}
		return new Result<>(
				problem.getScore(state, childScores),
				childScores);
	}
	
	private C search(int depth, S state) {
		if (0>=depth) {
			return problem.getScore(state);
		}
		Collection<Successor<A, S>> successors
				=problem.getSuccessorStates(state);
		if (successors.isEmpty()) {
			return problem.getScore(state);
		}
		Map<Successor<A, S>, C> childScores=new LinkedHashMap<>();
		for (Successor<A, S> successor: successors) {
			S state2=successor.getState();
			C score=search(depth-1, state2);
			childScores.put(successor, score);
		}
		return problem.getScore(state, childScores);
	}
}
