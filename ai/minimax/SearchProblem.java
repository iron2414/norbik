package org.arrostia.minimax;

import java.util.Collection;
import java.util.Map;

public interface SearchProblem<A, C extends Comparable<? super C>, S> {
	public static enum ChoiceType {
		MAX, MIN, UNKNOWN
	}
	
	C addCosts(C cost0, C cost1);
	ChoiceType getChoiceType(S state);
	C getScore(S state);
	C getScore(S state, Map<Successor<A, S>, C> childScores);
	S getStartState();
	Collection<Successor<A, S>> getSuccessorStates(S state);
	C getZeroCost();
}
