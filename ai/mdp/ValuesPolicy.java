package org.arrostia.mdp;

public class ValuesPolicy<A, C extends Comparable<? super C>, S> {
	private final Policy<A, S> policy;
	private final Values<C, S> values;

	public ValuesPolicy(Policy<A, S> policy, Values<C, S> values) {
		this.policy=policy;
		this.values=values;
	}

	public Policy<A, S> getPolicy() {
		return policy;
	}

	public Values<C, S> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		return "("+policy+", "+values+")";
	}
}
