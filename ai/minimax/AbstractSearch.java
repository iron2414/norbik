package org.arrostia.minimax;

public abstract class AbstractSearch<A, C extends Comparable<? super C>, S> {
	protected final SearchProblem<A, C, S> problem;

	public AbstractSearch(SearchProblem<A, C, S> problem) {
		this.problem=problem;
	}
	
	public abstract Result<A, C, S> search(int depth);
}
