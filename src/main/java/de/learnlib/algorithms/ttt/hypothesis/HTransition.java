package de.learnlib.algorithms.ttt.hypothesis;

import de.learnlib.algorithms.ttt.dtree.DTNode;

public class HTransition<I, O, SP, TP> {
	
	private final HypothesisState<I,O,SP,TP> source;
	private final I symbol;
	private TP property;
	private HypothesisState<I,O,SP,TP> treeTgt;
	private DTNode<I,O,SP,TP> dtTgt;
	

	public HTransition(HypothesisState<I,O,SP,TP> source, I symbol, DTNode<I,O,SP,TP> dtTgt) {
		this.source = source;
		this.symbol = symbol;
		this.dtTgt = dtTgt;
	}
	
	public boolean isTree() {
		return (dtTgt == null);
	}
	
	public HypothesisState<I,O,SP,TP> getSource() {
		return source;
	}
	
	public I getSymbol() {
		return symbol;
	}
	
	public TP getProperty() {
		return property;
	}
	
	public void setProperty(TP property) {
		this.property = property;
	}
	
	public HypothesisState<I,O,SP,TP> getTreeTarget() {
		return treeTgt;
	}
	
	public DTNode<I,O,SP,TP> getDTTarget() {
		return dtTgt;
	}
	
	public void makeTree(HypothesisState<I,O,SP,TP> treeTgt) {
		assert (dtTgt != null);
		this.treeTgt = treeTgt;
		this.dtTgt = null;
	}
}
