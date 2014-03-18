package de.learnlib.algorithms.ttt.dfa;

import com.github.misberner.jdtree.binary.BDTNode;

import net.automatalib.words.Word;

public class TTTTransitionDFA<I> {
	
	final TTTStateDFA<I> source;
	final I symbol;
	
	final Word<I> accessSequence;
	
	
	// Tree transition
	TTTStateDFA<I> treeTarget;
	// Non-tree transition
	BDTNode<Word<I>> dtTarget;
	
	
	public TTTTransitionDFA(TTTStateDFA<I> source, I symbol, BDTNode<Word<I>> dtTarget) {
		this.source = source;
		this.symbol = symbol;
		this.dtTarget = dtTarget;
		this.accessSequence = source.getAccessSequence().append(symbol);
	}
	
	public Word<I> getAccessSequence() {
		return accessSequence;
	}
	
	public TTTStateDFA<I> getSource() {
		return source;
	}
	
	public boolean isTree() {
		return (treeTarget != null);
	}
	
	public TTTStateDFA<I> getTreeTarget() {
		assert isTree();
		return treeTarget;
	}
	
	public BDTNode<Word<I>> getNonTreeTarget() {
		assert !isTree();
		return dtTarget;
	}
	
	void makeTree(TTTStateDFA<I> target) {
		assert !isTree();
		this.treeTarget = target;
		this.dtTarget = null;
	}
	
	
	@Override
	public String toString() {
		return source.toString() + "[" + symbol + "]";
	}

}
