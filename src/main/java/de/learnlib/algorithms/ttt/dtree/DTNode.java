package de.learnlib.algorithms.ttt.dtree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.stree.STNode;

public class DTNode<I,O,SP,TP> {
	
	private Map<O,DTNode<I,O,SP,TP>> children;
	private STNode<I> discriminator;
	private HypothesisState<I,O,SP,TP> hypothesisState;
	private final DTNode<I,O,SP,TP> parent;
	private final int depth;
	private final int id;

	public DTNode(int id, DTNode<I,O,SP,TP> parent, HypothesisState<I,O,SP,TP> hypothesisState) {
		this.id = id;
		this.hypothesisState = hypothesisState;
		if(hypothesisState != null)
			hypothesisState.setDTLeaf(this);
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	public DTNode(int id, DTNode<I,O,SP,TP> parent, STNode<I> discriminator) {
		this.id = id;
		this.children = new HashMap<>();
		this.discriminator = discriminator;
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	public int getId() {
		return id;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public DTNode<I, O, SP, TP> getParent() {
		return parent;
	}
	
	
	public boolean isLeaf() {
		return (children == null);
	}
	
	public HypothesisState<I,O,SP,TP> makeInner(STNode<I> discriminator) {
		assert (hypothesisState != null);
		HypothesisState<I,O,SP,TP> state = hypothesisState;
		hypothesisState = null;
		this.children = new HashMap<>();
		this.discriminator = discriminator;
		return state;
	}
	
	
	
	public HypothesisState<I,O,SP,TP> getHypothesisState() {
		return hypothesisState;
	}
	
	public void setHypothesisState(HypothesisState<I,O,SP,TP> hypState) {
		this.hypothesisState = hypState;
		hypState.setDTLeaf(this);
	}
	
	public STNode<I> getDiscriminator() {
		return discriminator;
	}
	
	public void setDiscriminator(STNode<I> discriminator) {
		this.discriminator = discriminator;
	}
	
	public DTNode<I,O,SP,TP> getChild(O outcome) {
		return children.get(outcome);
	}
	
	public void addChild(O outcome, DTNode<I,O,SP,TP> child) {
		children.put(outcome, child);
	}
	
	public Map<O,DTNode<I,O,SP,TP>> getChildMap() {
		return (children == null) ? null : Collections.unmodifiableMap(children);
	}
}
