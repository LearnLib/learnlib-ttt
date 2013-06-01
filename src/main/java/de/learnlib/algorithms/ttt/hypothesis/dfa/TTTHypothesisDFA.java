package de.learnlib.algorithms.ttt.hypothesis.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
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

}
