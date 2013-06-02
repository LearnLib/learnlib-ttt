package de.learnlib.algorithms.ttt.hypothesis.dfa;

import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.graphs.dot.DelegateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis;

public class TTTHypothesisDFA<I> extends
		TTTHypothesis<I, Boolean, Boolean, Void, HypothesisState<I, Boolean, Boolean, Void>>
		implements DFA<HypothesisState<I,Boolean,Boolean,Void>,I> {

	public TTTHypothesisDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public HypothesisState<I, Boolean, Boolean, Void> getSuccessor(
			HypothesisState<I, Boolean, Boolean, Void> trans) {
		return AbstractFSA.getSuccessor(this, trans);
	}

	@Override
	public Void getTransitionProperty(
			HypothesisState<I, Boolean, Boolean, Void> trans) {
		return AbstractFSA.getTransitionProperty(this, trans);
	}

	@Override
	protected HypothesisState<I, Boolean, Boolean, Void> getAutomatonTransition(
			HTransition<I, Boolean, Boolean, Void> itrans) {
		return getTarget(itrans);
	}

	@Override
	public boolean accepts(Iterable<I> input) {
		return AbstractDFA.accepts(this, input);
	}

	@Override
	public boolean isAccepting(HypothesisState<I, Boolean, Boolean, Void> state) {
		return state.getProperty().booleanValue();
	}

	@Override
	public Boolean computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractFSA.computeSuffixOutput(this, prefix, suffix);
	}

	@Override
	public Boolean computeOutput(Iterable<I> input) {
		return AbstractFSA.computeOutput(this, input);
	}

	/* (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<HypothesisState<I, Boolean, Boolean, Void>, HTransition<I, Boolean, Boolean, Void>> getGraphDOTHelper() {
		return new DelegateDOTHelper<HypothesisState<I,Boolean,Boolean,Void>,HTransition<I,Boolean,Boolean,Void>>(super.getGraphDOTHelper()) {
			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DelegateDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getNodeProperties(
					HypothesisState<I, Boolean, Boolean, Void> node,
					Map<String, String> properties) {
				if(!super.getNodeProperties(node, properties))
					return false;
				if(node.getProperty().booleanValue())
					properties.put(SHAPE, "doublecircle");
				return true;
			}
		};
	}
	
	

}
