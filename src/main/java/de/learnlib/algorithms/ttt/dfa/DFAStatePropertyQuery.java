package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.StatePropertyQuery;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;

public class DFAStatePropertyQuery<I> extends
		StatePropertyQuery<I, Boolean, Boolean> {

	public DFAStatePropertyQuery(HypothesisState<I, Boolean, Boolean, ?> state) {
		super(state, state.getAccessSequence(), Word.<I>epsilon());
	}

	@Override
	protected Boolean extractProperty(Boolean output) {
		return output;
	}
}
