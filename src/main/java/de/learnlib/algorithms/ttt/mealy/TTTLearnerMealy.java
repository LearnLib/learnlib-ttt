package de.learnlib.algorithms.ttt.mealy;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.AbstractTTTLearner;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.mealy.TTTHypothesisMealy;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public class TTTLearnerMealy<I, O> extends
		AbstractTTTLearner<I, O, Void, O, MealyMachine<?, I, ?, O>, TTTHypothesisMealy<I, O>> {

	public TTTLearnerMealy(Alphabet<I> alphabet, MembershipOracle<I,O> oracle) {
		super(alphabet, oracle, new TTTHypothesisMealy<I,O>(alphabet));
	}

	@Override
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return hypothesis;
	}

	/* (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.AbstractTTTLearner#transitionProperty(de.learnlib.algorithms.ttt.hypothesis.HTransition)
	 */
	@Override
	protected Query<I, O> transitionProperty(HTransition<I, O, Void, O> trans) {
		return new MealyTransitionPropertyQuery<>(trans);
	}

}
