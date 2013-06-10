package de.learnlib.algorithms.ttt.dtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.stree.STNode;

public class DTNode<I,O,SP,TP> {

	// GENERAL FIELDS
	private final DTNode<I,O,SP,TP> parent;
	private final O output;
	private final int depth;
	private final int id;
	private final List<HTransition<I,O,SP,TP>> nonTreeIncoming
		= new ArrayList<>();
	
	// INNER NODE FIELDS
	private STNode<I> discriminator;
	private Map<O,DTNode<I,O,SP,TP>> children;
	
	// LEAF FIELDS
	private TempDTNode<I, O, SP, TP> tempRoot;

	public DTNode(int id, DTNode<I,O,SP,TP> parent, O output, TempDTNode<I, O, SP, TP> tempRoot) {
		this.id = id;
		this.tempRoot = tempRoot;
		this.parent = parent;
		this.output = output;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	public DTNode(int id, DTNode<I,O,SP,TP> parent, O output, STNode<I> discriminator) {
		this.id = id;
		this.children = new HashMap<>();
		this.discriminator = discriminator;
		this.parent = parent;
		this.output = output;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	public int getId() {
		return id;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public O getOutput() {
		return output;
	}
	
	public DTNode<I, O, SP, TP> getParent() {
		return parent;
	}
	
	
	public boolean isLeaf() {
		return (children == null);
	}
	
	public void makeInner(STNode<I> discriminator) {
		if(children != null)
			throw new IllegalArgumentException("Cannot make node an inner node: already is");
		
		this.tempRoot = null;
		this.discriminator = discriminator;
		this.children = new HashMap<>();
	}
	
	
	public TempDTNode<I, O, SP, TP> getTempRoot() {
		return tempRoot;
	}
	
	public void setTempRoot(TempDTNode<I, O, SP, TP> tempRoot) {
		this.tempRoot = tempRoot;
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
	
	public List<HTransition<I, O, SP, TP>> getNonTreeIncoming() {
		return nonTreeIncoming;
	}
	
	public void clearNonTreeIncoming() {
		nonTreeIncoming.clear();
	}
}
