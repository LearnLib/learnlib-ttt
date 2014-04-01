package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;

public class DTNode<I> {

	private final DTNode<I> parent;
	private final boolean parentEdgeLabel;
	private final int depth;
	
	List<TTTTransitionDFA<I>> incoming;
	
	// INNER NODE DATA
	private Word<I> discriminator;
	private DTNode<I> falseChild;
	private DTNode<I> trueChild;
	
	// LEAF NODE DATA
	TTTStateDFA<I> state;
	
	
	public DTNode() {
		this(null, false);
	}
	
	public DTNode(DTNode<I> parent, boolean parentEdgeLabel) {
		this.parent = parent;
		this.parentEdgeLabel = parentEdgeLabel;
		this.depth = (parent != null) ? parent.depth + 1 : 0; 
	}
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	
	public boolean isInner() {
		return (discriminator != null);
	}
	
	public boolean isLeaf() {
		return (discriminator == null);
	}
	
	public DTNode<I> getFalseChild() {
		return falseChild;
	}
	
	public DTNode<I> getTrueChild() {
		return trueChild;
	}
	
	
	public DTNode<I> getChild(boolean value) {
		assert isInner();
		
		return value ? trueChild : falseChild;
	}
	
	public DTNode<I> getParent() {
		return parent;
	}
	
	public boolean getParentEdgeLabel() {
		return parentEdgeLabel;
	}
	
	public int getDepth() {
		return depth;
	}
	
	void addIncoming(TTTTransitionDFA<I> transition) {
		if(incoming == null) {
			incoming = new ArrayList<>();
		}
		incoming.add(transition);
	}
	
	List<TTTTransitionDFA<I>> fetchIncoming() {
		if(incoming == null) {
			return Collections.emptyList();
		}
		
		
		List<TTTTransitionDFA<I>> result = incoming;
		incoming = null;
		return result;
	}
	
}
