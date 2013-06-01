package de.learnlib.algorithms.ttt.mealy;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.TransitionPropertyQuery;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;

public class MealyTransitionPropertyQuery<I, O> extends
		TransitionPropertyQuery<I, O, O> {

	public MealyTransitionPropertyQuery(HTransition<I, O, ?, O> transition) {
		super(transition, transition.getSource().getAccessSequence(), Word.fromLetter(transition.getSymbol()));
	}

	@Override
	protected O extractProperty(O output) {
		return output;
	}

}
