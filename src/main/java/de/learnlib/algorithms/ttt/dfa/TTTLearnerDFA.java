package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.AbstractTTTLearner;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.dfa.TTTHypothesisDFA;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public class TTTLearnerDFA<I>
		extends
		AbstractTTTLearner<I, Boolean, Boolean, Void, DFA<?, I>, TTTHypothesisDFA<I>> {

	public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle) {
		super(alphabet, oracle, new TTTHypothesisDFA<I>(alphabet));
	}

	@Override
	public DFA<?, I> getHypothesisModel() {
		return hypothesis;
	}

	/* (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.AbstractTTTLearner#stateProperty(de.learnlib.algorithms.ttt.hypothesis.HypothesisState)
	 */
	@Override
	protected Query<I, Boolean> stateProperty(
			HypothesisState<I, Boolean, Boolean, Void> state) {
		return new DFAStatePropertyQuery<>(state);
	}
	
}
