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

public class TTTHypothesisDFA<I> extends AbstractDFA<TTTStateDFA<I>,I>
		implements DOTPlottableAutomaton<TTTStateDFA<I>, I, TTTStateDFA<I>> {

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
