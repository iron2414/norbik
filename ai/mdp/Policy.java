package org.arrostia.mdp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Policy<A, S> {
	private final Map<S, A> policy;
	
	public Policy() {
		policy=new LinkedHashMap<>();
	}
	
	public Policy(Policy<A, S> values) {
		this.policy=new LinkedHashMap<>(values.policy);
	}
	
	public A get(S state) {
		return policy.get(state);
	}
	
	public A get(S state, A defaultValue) {
		A result=get(state);
		if (null==result) {
			return defaultValue;
		}
		return result;
	}
	
	public void remove(S state) {
		policy.remove(state);
	}
	
	public void set(S state, A action) {
		policy.put(state, action);
	}
	
	public int size() {
		return policy.size();
	}
	
	public Collection<S> states() {
		return new LinkedList<>(policy.keySet());
	}
	
	@Override
	public String toString() {
		return policy.toString();
	}
}
