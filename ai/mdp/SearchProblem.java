package org.arrostia.mdp;

import java.util.Collection;
import java.util.Map;

public interface SearchProblem<A, C extends Comparable<? super C>, S> {
	Collection<A> actions(S state);
	C abs(C cost);
	C add(C cost0, C cost1);
	C gamma();
	C multiply(C cost0, C cost1);
	C one();
	Map<S, C> probabilities(S state, A action);
	C reward(S state, A action, S successorState);
	Collection<S> states();
	C subtract(C cost0, C cost1);
	C zero();
}
