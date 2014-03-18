package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.AccessSequenceTransformer;

import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

@ParametersAreNonnullByDefault
public class TTTHypothesisDFA<I> extends AbstractDFA<TTTStateDFA<I>, I>
		implements AccessSequenceTransformer<I> {
	
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
			return getTransitionTarget(edge);
		}

		@Override
		public GraphDOTHelper<TTTStateDFA<I>, TTTTransitionDFA<I>> getGraphDOTHelper() {
			return new DefaultDOTHelper<TTTStateDFA<I>,TTTTransitionDFA<I>>() {
				@Override
				protected Collection<? extends TTTStateDFA<I>> initialNodes() {
					return Collections.singleton(initialState);
				}
				@Override
				public boolean getNodeProperties(TTTStateDFA<I> node,
						Map<String, String> properties) {
					properties.put(NodeAttrs.LABEL, node.toString());
					if(node.isAccepting()) {
						properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
					}
					return true;
				}
				@Override
				public boolean getEdgeProperties(TTTStateDFA<I> src,
						TTTTransitionDFA<I> edge, TTTStateDFA<I> tgt,
						Map<String, String> properties) {
					if(edge.isTree()) {
						properties.put(EdgeAttrs.STYLE, "bold");
					}
					properties.put(EdgeAttrs.LABEL, String.valueOf(edge.symbol));
					return true;
				}
				
			};
		}
		
		
	}
	
	@Nonnull
	private final Alphabet<I> alphabet;
	@Nonnull
	private final List<TTTStateDFA<I>> states = new ArrayList<>();
	@Nullable
	private TTTStateDFA<I> initialState;

	public TTTHypothesisDFA(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	public TTTStateDFA<I> initialize(boolean initialAccepting) {
		initialState = new TTTStateDFA<>(0, initialAccepting, null, alphabet.size());
		states.add(initialState);
		states.add(null);
		return initialState;
	}
	
	public TTTStateDFA<I> createState(TTTTransitionDFA<I> parentTransition, boolean accepting) {
		TTTStateDFA<I> newState = new TTTStateDFA<>(states.size(), accepting, parentTransition, alphabet.size());
		states.add(newState);
		return newState;
	}
	
	public TTTStateDFA<I> createStateSpecial(TTTTransitionDFA<I> parentTransition) {
		assert states.get(1) == null;
		TTTStateDFA<I> newState = new TTTStateDFA<>(1, !initialState.accepting, parentTransition, alphabet.size());
		states.set(1, newState);
		return newState;
	}

	@Override
	public Collection<TTTStateDFA<I>> getStates() {
		return Collections.unmodifiableCollection(states);
	}

	@Override
	public TTTStateDFA<I> getInitialState() {
		return initialState;
	}

	@Override
	public TTTStateDFA<I> getTransition(TTTStateDFA<I> state, I input) {
		int inputIdx = alphabet.getSymbolIndex(input);
		TTTTransitionDFA<I> trans = state.getTransition(inputIdx);
		return getTransitionTarget(trans);
	}

	@Override
	public boolean isAccepting(TTTStateDFA<I> state) {
		return state.isAccepting();
	}

	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		TTTStateDFA<I> state = getState(word);
		return state.getAccessSequence();
	}

	public TTTTransitionDFA<I> getInternalTransition(TTTStateDFA<I> state, I symbol) {
		int symIdx = alphabet.getSymbolIndex(symbol);
		return state.transitions[symIdx];
	}
	
	@Override
	public boolean isAccessSequence(Word<I> word) {
		TTTStateDFA<I> current = initialState;
		
		for(I sym : word) {
			int symIdx = alphabet.getSymbolIndex(sym);
			TTTTransitionDFA<I> trans = current.getTransition(symIdx);
			if(!trans.isTree()) {
				return false;
			}
		}
		return true;
	}
	
	public TTTStateDFA<I> getTransitionTarget(TTTTransitionDFA<I> trans) {
		if(trans.isTree()) {
			return trans.getTreeTarget();
		}
		if(trans.dtTarget.isInner()) {
			return null;
		}
		return states.get(trans.dtTarget.getLeafId());
	}
	
	public boolean isInitialized() {
		return (initialState != null);
	}
	
	public TTTStateDFA<I> getState(int index) {
		return states.get(index);
	}
	
	public GraphView graphView() {
		return new GraphView();
	}

}
