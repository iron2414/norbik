package org.arrostia.minimax;

import java.util.Map;

public class Result<A, C extends Comparable<? super C>, S> {
	private final Map<Successor<A, S>, C> childScores;
	private final C score;

	public Result(C score, Map<Successor<A, S>, C> childScores) {
		this.childScores=childScores;
		this.score=score;
	}

	public Map<Successor<A, S>, C> getChildScores() {
		return childScores;
	}

	public C getScore() {
		return score;
	}
}
