/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib-TTT, https://github.com/LearnLib/learnlib-ttt/
 * 
 * LearnLib-TTT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LearnLib-TTT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LearnLib-TTT.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;


/**
 * Hypothesis DFA for the {@link TTTLearnerDFA TTT algorithm}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class TTTHypothesisDFA<I> extends AbstractDFA<TTTStateDFA<I>,I>
		implements DOTPlottableAutomaton<TTTStateDFA<I>, I, TTTStateDFA<I>> {

	private final List<TTTStateDFA<I>> states = new ArrayList<>();
	
	private final Alphabet<I> alphabet;
	private TTTStateDFA<I> initialState;

	/**
	 * Constructor.
	 * 
	 * @param alphabet the input alphabet
	 */
	public TTTHypothesisDFA(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStates()
	 */
	@Override
	public Collection<TTTStateDFA<I>> getStates() {
		return Collections.unmodifiableList(states);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleDTS#getInitialState()
	 */
	@Override
	public TTTStateDFA<I> getInitialState() {
		return initialState;
	}

	@Override
	public TTTStateDFA<I> getTransition(TTTStateDFA<I> state, I input) {
		TTTTransitionDFA<I> trans = getInternalTransition(state, input);
		return trans.getTarget();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.acceptors.AcceptorTS#isAccepting(java.lang.Object)
	 */
	@Override
	public boolean isAccepting(TTTStateDFA<I> state) {
		return state.accepting;
	}
	
	/**
	 * Checks whether this automaton was initialized (i.e.,
	 * {@link #initialize(boolean)} has been called).
	 * 
	 * @return {@code true} if this automaton was initialized, {@code false}
	 * otherwise.
	 */
	public boolean isInitialized() {
		return (initialState != null);
	}
	
	/**
	 * Initializes the automaton, adding an initial state. Whether or not the
	 * initial state is accepting needs to be known at this point.
	 * 
	 * @param initialAccepting whether or not the initial state is accepting
	 * @return the initial state of this newly initialized automaton
	 */
	public TTTStateDFA<I> initialize(boolean initialAccepting) {
		assert !isInitialized();
		
		initialState = createState(null, initialAccepting);
		return initialState;
	}
	
	/**
	 * Retrieves the <i>internal</i> transition (i.e., the {@link TTTTransitionDFA} object)
	 * for a given state and input. This method is required since the {@link DFA} interface
	 * requires the return value of {@link #getTransition(TTTStateDFA, Object)} to
	 * refer to the successor state directly.
	 * 
	 * @param state the source state
	 * @param input the input symbol triggering the transition
	 * @return the transition object
	 */
	public TTTTransitionDFA<I> getInternalTransition(TTTStateDFA<I> state, I input) {
		int inputIdx = alphabet.getSymbolIndex(input);
		TTTTransitionDFA<I> trans = state.transitions[inputIdx];
		return trans;
	}
	
	
	public TTTStateDFA<I> createState(TTTTransitionDFA<I> parent, boolean accepting) {
		TTTStateDFA<I> state = new TTTStateDFA<I>(alphabet.size(), parent, states.size(), accepting);
		states.add(state);
		if(parent != null) {
			parent.makeTree(state);
		}
		return state;
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		return alphabet;
	}

	@Override
	public GraphDOTHelper<TTTStateDFA<I>, TransitionEdge<I, TTTStateDFA<I>>> getDOTHelper() {
		return new DOTHelperFSA<>(this);
	}


	
	public class GraphView extends AbstractGraph<TTTStateDFA<I>,TTTTransitionDFA<I>>
			implements DOTPlottableGraph<TTTStateDFA<I>,TTTTransitionDFA<I>> {

		@Override
		public Collection<? extends TTTStateDFA<I>> getNodes() {
			return states;
		}

		@Override
		public Collection<? extends TTTTransitionDFA<I>> getOutgoingEdges(
				TTTStateDFA<I> node) {
			return Arrays.asList(node.transitions);
		}

		@Override
		public TTTStateDFA<I> getTarget(TTTTransitionDFA<I> edge) {
			return edge.getTarget();
		}

		@Override
		public GraphDOTHelper<TTTStateDFA<I>, TTTTransitionDFA<I>> getGraphDOTHelper() {
			return new DefaultDOTHelper<TTTStateDFA<I>,TTTTransitionDFA<I>>() {

				@Override
				public boolean getNodeProperties(TTTStateDFA<I> node,
						Map<String, String> properties) {
					if(node.isAccepting()) {
						properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
					}
					else {
						properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
					}
					return true;
				}

				@Override
				public boolean getEdgeProperties(TTTStateDFA<I> src,
						TTTTransitionDFA<I> edge, TTTStateDFA<I> tgt,
						Map<String, String> properties) {
					properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getInput()));
					if(edge.isTree()) {
						properties.put(EdgeAttrs.STYLE, "bold");
					}
					return true;
				}
				
			};
		}
		
		
	}
	
	public GraphView graphView() {
		return new GraphView();
	}
	
}
