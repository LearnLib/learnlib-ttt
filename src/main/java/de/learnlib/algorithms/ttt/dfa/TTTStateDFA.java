package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.misberner.jdtree.binary.BDTNode;

import net.automatalib.words.Word;

public class TTTStateDFA<I> {
	
	final int id;
	final boolean accepting;
	final TTTTransitionDFA<I> parentTransition;
	final TTTTransitionDFA<I>[] transitions;
	
	private List<TTTTransitionDFA<I>> incoming = new ArrayList<>();
	
	boolean replaceMark;
	
	@SuppressWarnings("unchecked")
	public TTTStateDFA(int id, boolean accepting, TTTTransitionDFA<I> parentTransition, int alphabetSize) {
		this.id = id;
		this.accepting = accepting;
		this.parentTransition = parentTransition;
		this.transitions = new TTTTransitionDFA[alphabetSize];
		
		if(parentTransition != null) {
			parentTransition.makeTree(this);
		}
	}
	
	public TTTTransitionDFA<I> getParentTransition() {
		return parentTransition;
	}
	
	public TTTStateDFA<I> getParentState() {
		return (parentTransition != null) ? parentTransition.getSource() : null;
	}
	
	public boolean isAccepting() {
		return accepting;
	}
	
	public int getId() {
		return id;
	}
	
	public TTTTransitionDFA<I> getTransition(int index) {
		return transitions[index];
	}
	
	public Word<I> getAccessSequence() {
		if(parentTransition != null) {
			return parentTransition.getAccessSequence();
		}
		return Word.epsilon();
	}
	
	public boolean isRoot() {
		return (parentTransition == null);
	}
	
	
	
	public List<TTTTransitionDFA<I>> fetchIncoming() {
		if(incoming == null || incoming.isEmpty()) {
			return Collections.emptyList();
		}
		List<TTTTransitionDFA<I>> result = incoming;
		incoming = null;
		return result;
	}
	
	public void addIncoming(TTTTransitionDFA<I> transition) {
		if(incoming == null) {
			incoming = new ArrayList<>();
		}
		incoming.add(transition);
	}
	
	
	public void updateIncomingDT(BDTNode<Word<I>> dtTarget) {
		if(incoming == null) {
			return;
		}
		for(TTTTransitionDFA<I> trans : incoming) {
			trans.dtTarget = dtTarget;
		}
	}
	
	@Override
	public String toString() {
		return "q" + id;
	}
}
