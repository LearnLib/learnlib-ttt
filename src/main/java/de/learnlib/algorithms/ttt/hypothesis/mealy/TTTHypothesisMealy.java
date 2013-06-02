package de.learnlib.algorithms.ttt.hypothesis.mealy;

import java.util.List;
import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.graphs.dot.DelegateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis;

public class TTTHypothesisMealy<I, O> extends TTTHypothesis<I, O, Void, O, HTransition<I, O, Void, O>>
		implements MealyMachine<HypothesisState<I,O,Void,O>, I, HTransition<I,O,Void,O>, O>{

	public TTTHypothesisMealy(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public HypothesisState<I, O, Void, O> getSuccessor(
			HTransition<I, O, Void, O> transition) {
		return getTarget(transition);
	}

	@Override
	public O getTransitionProperty(HTransition<I, O, Void, O> transition) {
		return transition.getProperty();
	}

	@Override
	protected HTransition<I, O, Void, O> getAutomatonTransition(
			HTransition<I, O, Void, O> itrans) {
		return itrans;
	}

	@Override
	public O getOutput(HypothesisState<I, O, Void, O> state, I input) {
		return AbstractTransOutAutomaton.getOutput(this, state, input);
	}

	@Override
	public void trace(Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, input, output);
	}

	@Override
	public void trace(HypothesisState<I, O, Void, O> state, Iterable<I> input,
			List<O> output) {
		AbstractTransOutAutomaton.trace(this, state, input, output);
	}

	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}

	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}

	@Override
	public O getTransitionOutput(HTransition<I, O, Void, O> trans) {
		return trans.getProperty();
	}

	/* (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<HypothesisState<I, O, Void, O>, HTransition<I, O, Void, O>> getGraphDOTHelper() {
		return new DelegateDOTHelper<HypothesisState<I, O, Void, O>, HTransition<I, O, Void, O>>(super.getGraphDOTHelper()) {

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DelegateDOTHelper#getEdgeProperties(java.lang.Object, java.lang.Object, java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getEdgeProperties(
					HypothesisState<I, O, Void, O> src,
					HTransition<I, O, Void, O> edge,
					HypothesisState<I, O, Void, O> tgt,
					Map<String, String> properties) {
				if(!super.getEdgeProperties(src, edge, tgt, properties))
					return false;
				String lbl = String.valueOf(edge.getSymbol()) + " / " + String.valueOf(edge.getProperty());
				properties.put(LABEL, lbl);
				return true;
			}
		};
	}

	
}
