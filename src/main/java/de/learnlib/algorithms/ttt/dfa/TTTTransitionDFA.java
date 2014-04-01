package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class TTTTransitionDFA<I> {
	
	private final TTTStateDFA<I> source;
	private final I input;

	// TREE TRANSITION
	private TTTStateDFA<I> treeTarget;
	
	// NON-TREE TRANSITION
	DTNode<I> nonTreeTarget;
	

	public TTTTransitionDFA(TTTStateDFA<I> source, I input) {
		this.source = source;
		this.input = input;
	}
	
	
	public boolean isTree() {
		return (treeTarget != null);
	}
	
	public TTTStateDFA<I> getTreeTarget() {
		return treeTarget;
	}
	
	public DTNode<I> getNonTreeTarget() {
		return nonTreeTarget;
	}
	
	public DTNode<I> getDTTarget() {
		if(treeTarget != null) {
			return treeTarget.dtLeaf;
		}
		return nonTreeTarget;
	}
	
	
	public TTTStateDFA<I> getTarget() {
		if(treeTarget != null) {
			return treeTarget;
		}
		
		return nonTreeTarget.state;
	}
	
	public TTTStateDFA<I> getSource() {
		return source;
	}
	
	public I getInput() {
		return input;
	}
	
	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<>(); // FIXME capacity hint
		
		TTTTransitionDFA<I> curr = this;
		
		while(curr != null) {
			wb.add(curr.input);
			curr = curr.source.parentTransition;
		}
		
		return wb.reverse().toWord();
	}
	
	
	
	
}
