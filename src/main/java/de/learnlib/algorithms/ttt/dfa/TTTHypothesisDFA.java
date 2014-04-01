package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.words.Alphabet;

public class TTTHypothesisDFA<I> extends AbstractDFA<TTTStateDFA<I>,I> {
	
	private final List<TTTStateDFA<I>> states = new ArrayList<>();
	
	private final Alphabet<I> alphabet;
	private TTTStateDFA<I> initialState;

	public TTTHypothesisDFA(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}

	@Override
	public Collection<TTTStateDFA<I>> getStates() {
		return Collections.unmodifiableList(states);
	}

	@Override
	public TTTStateDFA<I> getInitialState() {
		return initialState;
	}

	@Override
	public TTTStateDFA<I> getTransition(TTTStateDFA<I> state, I input) {
		TTTTransitionDFA<I> trans = getInternalTransition(state, input);
		return trans.getTarget();
	}

	@Override
	public boolean isAccepting(TTTStateDFA<I> state) {
		return state.accepting;
	}
	
	public boolean isInitialized() {
		return (initialState != null);
	}
	
	public TTTStateDFA<I> initialize(boolean initialAccepting) {
		assert !isInitialized();
		
		initialState = createState(null, initialAccepting);
		return initialState;
	}
	
	public TTTTransitionDFA<I> getInternalTransition(TTTStateDFA<I> state, I input) {
		int inputIdx = alphabet.getSymbolIndex(input);
		TTTTransitionDFA<I> trans = state.transitions[inputIdx];
		return trans;
	}
	
	
	public TTTStateDFA<I> createState(TTTTransitionDFA<I> parent, boolean accepting) {
		TTTStateDFA<I> state = new TTTStateDFA<I>(alphabet.size(), parent, states.size(), accepting);
		states.add(state);
		return state;
	}

	
	
}
