package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;

public class TTTStateDFA<I> {
	
	final int id;
	final boolean accepting;
	
	final TTTTransitionDFA<I>[] transitions;
	final TTTTransitionDFA<I> parentTransition;
	
	DTNode<I> dtLeaf;

	@SuppressWarnings("unchecked")
	public TTTStateDFA(int alphabetSize, TTTTransitionDFA<I> parentTransition, int id, boolean accepting) {
		this.id = id;
		this.accepting = accepting;
		this.parentTransition = parentTransition;
		this.transitions = new TTTTransitionDFA[alphabetSize];
	}
	
	
	public boolean isAccepting() {
		return accepting;
	}
	
	public boolean isRoot() {
		return (parentTransition == null);
	}
	
	public Word<I> getAccessSequence() {
		if(parentTransition != null) {
			return parentTransition.getAccessSequence();
		}
		return Word.epsilon(); // root
	}

}
