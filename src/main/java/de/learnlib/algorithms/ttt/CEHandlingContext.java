package de.learnlib.algorithms.ttt;

import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.stree.STNode;

final class CEHandlingContext<I,O,SP,TP> {
	private final STNode<I> tmpDiscr;
	private final DTNode<I, O, SP, TP> splitter;
	private final HypothesisState<I, O, SP, TP> oldState;
	private final HypothesisState<I, O, SP, TP> newState;
	
	
	public CEHandlingContext(STNode<I> tmpDiscr, DTNode<I, O, SP, TP> splitter, HypothesisState<I, O, SP, TP> oldState,
			HypothesisState<I, O, SP, TP> newState) {
		this.tmpDiscr = tmpDiscr;
		this.splitter = splitter;
		this.oldState = oldState;
		this.newState = newState;
	}
	
	public HypothesisState<I, O, SP, TP> getOldState() {
		return oldState;
	}
	
	public HypothesisState<I, O, SP, TP> getNewState() {
		return newState;
	}
	
	public DTNode<I, O, SP, TP> getSplitter() {
		return splitter;
	}
	
	public STNode<I> getTempDiscriminator() {
		return tmpDiscr;
	}
}