package de.learnlib.algorithms.ttt.dfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SplitData<I> {
	
	private final Set<Boolean> marks = new HashSet<>();
	private final Map<Boolean, IncomingList<I>> incomingTransitions
		= new HashMap<>();
	
	private Boolean stateLabel;
	
	public boolean mark(boolean label) {
		return marks.add(label);
	}
	
	public boolean hasStateLabel() {
		return (stateLabel != null);
	}
	
	public void setStateLabel(boolean label) {
		this.stateLabel = label;
	}
	
	public boolean getStateLabel() {
		return stateLabel;
	}
	
	public IncomingList<I> getIncoming(boolean label) {
		IncomingList<I> list = incomingTransitions.get(label);
		if(list == null) {
			list = new IncomingList<>();
			incomingTransitions.put(label, list);
		}
		
		return list;
	}
	
	public boolean isMarked(boolean label) {
		return marks.contains(label);
	}
	
	
}
