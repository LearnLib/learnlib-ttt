package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;

public class DTNode<I> {
	
	private final DTNode<I> parent;
	private final boolean parentLabel;
	
	
	
	private Word<I> discriminator;
	private boolean temp;
	
	private TTTStateDFA<I> state;
	
	
	private DTNode<I> falseChild;
	private DTNode<I> trueChild;
	
	
	
	public DTNode() {
		this(null, false);
	}
	
	public DTNode(DTNode<I> parent, boolean parentLabel) {
		this.parent = parent;
		this.parentLabel = parentLabel;
	}
	
	public boolean isTemp() {
		return temp;
	}
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	public boolean isLeaf() {
		return (state != null);
	}
	
	public boolean isInner() {
		return (state == null);
	}
}
