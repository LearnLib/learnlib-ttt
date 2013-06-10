package de.learnlib.algorithms.ttt.hypothesis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.abstractimpl.AbstractAutomaton;
import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dtree.DTNode;

public abstract class TTTHypothesis<I,O,SP,TP,T> extends AbstractGraph<HypothesisState<I,O,SP,TP>,HTransition<I, O, SP, TP>>
		implements DOTPlottableGraph<HypothesisState<I,O,SP,TP>, HTransition<I,O,SP,TP>>, UniversalDeterministicAutomaton<HypothesisState<I,O,SP,TP>,I,T,SP,TP>,
		NodeIDs<HypothesisState<I,O,SP,TP>>, StateIDs<HypothesisState<I,O,SP,TP>> {
	
	private final Alphabet<I> alphabet;
	private final HypothesisState<I,O,SP,TP> root;
	private final List<HypothesisState<I,O,SP,TP>> nodes = new ArrayList<>();

	public TTTHypothesis(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.root = new HypothesisState<>(alphabet.size());
		this.nodes.add(root);
	}
	
	public HypothesisState<I,O,SP,TP> createState(HTransition<I, O, SP, TP> treeIncoming) {
		HypothesisState<I,O,SP,TP> state = new HypothesisState<>(alphabet.size(), nodes.size(), treeIncoming);
		nodes.add(state);
		return state;
	}
	
	public HTransition<I,O,SP,TP> getInternalTransition(HypothesisState<I, O, SP, TP> state, I symbol) {
		int symIdx = alphabet.getSymbolIndex(symbol);
		return state.getTransition(symIdx);
	}

	@Override
	public Collection<HypothesisState<I,O,SP,TP>> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	@Override
	public NodeIDs<HypothesisState<I,O,SP,TP>> nodeIDs() {
		return this;
	}

	@Override
	public Collection<HTransition<I, O, SP, TP>> getOutgoingEdges(HypothesisState<I,O,SP,TP> node) {
		return node.getOutgoingTransitions();
	}

	@Override
	public HypothesisState<I,O,SP,TP> getTarget(HTransition<I, O, SP, TP> edge) {
		HypothesisState<I,O,SP,TP> tt = edge.getTreeTarget();
		if(tt != null)
			return tt;
		
		DTNode<I,O,SP,TP> dt = edge.getDT();
		if(!dt.isLeaf())
			throw new IllegalStateException("Open transition [" + edge.getAccessSequence() + "]");
		return dt.getTempRoot().getState();
	}

	@Override
	public Collection<T> getTransitions(HypothesisState<I, O, SP, TP> state,
			I input) {
		return AbstractDeterministicAutomaton.getTransitions(this, state, input);
	}

	@Override
	public DeterministicTransitionSystem<? extends Set<HypothesisState<I, O, SP, TP>>, I, ? extends Collection<T>> powersetView() {
		return AbstractDeterministicAutomaton.powersetView(this);
	}

	@Override
	public <V> MutableMapping<HypothesisState<I, O, SP, TP>, V> createDynamicStateMapping() {
		return AbstractAutomaton.createDynamicStateMapping(this);
	}

	@Override
	public <V> MutableMapping<HypothesisState<I, O, SP, TP>, V> createStaticStateMapping() {
		return AbstractDeterministicAutomaton.createStaticStateMapping(this);
	}

	@Override
	public Set<HypothesisState<I, O, SP, TP>> getInitialStates() {
		return AbstractDeterministicAutomaton.getInitialStates(this);
	}

	@Override
	public Set<HypothesisState<I, O, SP, TP>> getStates(Iterable<I> input) {
		return AbstractDeterministicAutomaton.getStates(this, input);
	}

	@Override
	public Set<HypothesisState<I, O, SP, TP>> getSuccessors(
			HypothesisState<I, O, SP, TP> state, I input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, state, input);
	}

	@Override
	public Set<HypothesisState<I, O, SP, TP>> getSuccessors(
			HypothesisState<I, O, SP, TP> state, Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, state, input);
	}

	@Override
	public Set<HypothesisState<I, O, SP, TP>> getSuccessors(
			Collection<HypothesisState<I, O, SP, TP>> states, Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, states, input);
	}

	@Override
	public Collection<HypothesisState<I, O, SP, TP>> getStates() {
		return Collections.unmodifiableCollection(nodes);
	}

	@Override
	public StateIDs<HypothesisState<I, O, SP, TP>> stateIDs() {
		return this;
	}

	@Override
	public HypothesisState<I, O, SP, TP> getInitialState() {
		return root;
	}

	@Override
	public HypothesisState<I, O, SP, TP> getState(Iterable<I> input) {
		return AbstractDeterministicAutomaton.getState(this, input);
	}

	@Override
	public HypothesisState<I, O, SP, TP> getSuccessor(
			HypothesisState<I, O, SP, TP> state, I input) {
		return AbstractDeterministicAutomaton.getSuccessor(this, state, input);
	}

	@Override
	public HypothesisState<I, O, SP, TP> getSuccessor(
			HypothesisState<I, O, SP, TP> state, Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessor(this, state, input);
	}

	@Override
	public T getTransition(HypothesisState<I, O, SP, TP> state, I sym) {
		HTransition<I,O,SP,TP> itrans = getInternalTransition(state, sym);
		
		return getAutomatonTransition(itrans);
	}

	@Override
	public SP getStateProperty(HypothesisState<I, O, SP, TP> state) {
		return state.getProperty();
	}

	@Override
	public HypothesisState<I, O, SP, TP> getState(int id) {
		return nodes.get(id);
	}

	@Override
	public int getStateId(HypothesisState<I, O, SP, TP> state) {
		return state.getId();
	}

	@Override
	public HypothesisState<I, O, SP, TP> getNode(int id) {
		return nodes.get(id);
	}

	@Override
	public int getNodeId(HypothesisState<I, O, SP, TP> node) {
		return node.getId();
	}

	@Override
	public GraphDOTHelper<HypothesisState<I, O, SP, TP>, HTransition<I, O, SP, TP>> getGraphDOTHelper() {
		return new DefaultDOTHelper<HypothesisState<I,O,SP,TP>,HTransition<I,O,SP,TP>>() {

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
			 */
			@Override
			protected Collection<? extends HypothesisState<I, O, SP, TP>> initialNodes() {
				return Collections.singleton(root);
			}

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getNodeProperties(
					HypothesisState<I, O, SP, TP> node,
					Map<String, String> properties) {
				if(!super.getNodeProperties(node, properties))
					return false;
				properties.put(LABEL, node.toString());
				return true;
			}

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.lang.Object, java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getEdgeProperties(HypothesisState<I, O, SP, TP> src,
					HTransition<I, O, SP, TP> edge,
					HypothesisState<I, O, SP, TP> tgt,
					Map<String, String> properties) {
				if(!super.getEdgeProperties(src, edge, tgt, properties))
					return false;
				properties.put(LABEL, String.valueOf(edge.getSymbol()));
				if(edge.isTree())
					properties.put("style", "bold");
				return true;
			}
		};
	}
	
	protected abstract T getAutomatonTransition(HTransition<I,O,SP,TP> itrans);


}
