package org.arrostia.minimax;

import java.util.Map;

public abstract class AbstractSearchProblem<A, C extends Comparable<? super C>, S>
		implements SearchProblem<A, C, S> {
	public C getMaxScore(Map<Successor<A, S>, C> childScores) {
		C result=null;
		for (C score: childScores.values()) {
			if ((null==result)
					|| (0>result.compareTo(score))) {
				result=score;
			}
		}
		return result;
	}
	
	public C getMinScore(Map<Successor<A, S>, C> childScores) {
		C result=null;
		for (C score: childScores.values()) {
			if ((null==result)
					|| (0<result.compareTo(score))) {
				result=score;
			}
		}
		return result;
	}
}
