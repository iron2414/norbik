package org.arrostia.mdp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Values<C extends Comparable<? super C>, S> {
	private final Map<S, C> values;
	
	public Values() {
		values=new LinkedHashMap<>();
	}
	
	public Values(Values<C, S> values) {
		this.values=new LinkedHashMap<>(values.values);
	}
	
	public C get(S state) {
		return values.get(state);
	}
	
	public C get(S state, C defaultValue) {
		C result=get(state);
		if (null==result) {
			return defaultValue;
		}
		return result;
	}
	
	public void remove(S state) {
		values.remove(state);
	}
	
	public void set(S state, C value) {
		values.put(state, value);
	}
	
	public int size() {
		return values.size();
	}
	
	public Collection<S> states() {
		return new LinkedList<>(values.keySet());
	}
	
	@Override
	public String toString() {
		return values.toString();
	}
}
