package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class TTTTransitionDFA<I> extends IncomingListElem<I> implements AccessSequenceProvider<I> {
	
	
	public static final class Iterator<I> implements java.util.Iterator<TTTTransitionDFA<I>> {
		private TTTTransitionDFA<I> cursor;
		public Iterator(TTTTransitionDFA<I> start) {
			this.cursor = start;
		}
		
		@Override
		public boolean hasNext() {
			return cursor != null;
		}
		@Override
		public TTTTransitionDFA<I> next() {
			TTTTransitionDFA<I> curr = cursor;
			cursor = cursor.nextIncoming;
			return curr;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	
	
	private final TTTStateDFA<I> source;
	private final I input;

	// TREE TRANSITION
	private TTTStateDFA<I> treeTarget;
	
	// NON-TREE TRANSITION
	DTNode<I> nonTreeTarget;
	
	protected IncomingListElem<I> prevIncoming;
	

	public TTTTransitionDFA(TTTStateDFA<I> source, I input) {
		this.source = source;
		this.input = input;
	}
	
	
	public boolean isTree() {
		return (treeTarget != null);
	}
	
	public TTTStateDFA<I> getTreeTarget() {
		assert isTree();
		
		return treeTarget;
	}
	
	public DTNode<I> getNonTreeTarget() {
		assert !isTree();
		
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
	
	@Override
	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<>(); // FIXME capacity hint
		
		TTTTransitionDFA<I> curr = this;
		
		while(curr != null) {
			wb.add(curr.input);
			curr = curr.source.parentTransition;
		}
		
		return wb.reverse().toWord();
	}
	
	void makeTree(TTTStateDFA<I> treeTarget) {
		removeFromList();
		this.treeTarget = treeTarget;
		this.nonTreeTarget = null;
	}
	
	void setNonTreeTarget(DTNode<I> nonTreeTarget) {
		this.nonTreeTarget = nonTreeTarget;
		nonTreeTarget.getIncoming().insertIncoming(this);
	}
	
	
	void removeFromList() {
		if(prevIncoming != null) {
			prevIncoming.nextIncoming = nextIncoming;
		}
		if(nextIncoming != null) {
			nextIncoming.prevIncoming = prevIncoming;
		}
	}
}
