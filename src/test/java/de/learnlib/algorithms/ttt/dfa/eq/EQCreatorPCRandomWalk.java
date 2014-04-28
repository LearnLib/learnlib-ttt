package de.learnlib.algorithms.ttt.dfa.eq;

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eq.PCRandomWalkEQOracle;

public class EQCreatorPCRandomWalk extends EQCreator {
	
	private final Random r;
	private final float minLengthFactor;
	private final float maxLengthFactor;
	
	public EQCreatorPCRandomWalk(Random r, float minLengthFactor, float maxLengthFactor) {
		this.r = r;
		this.minLengthFactor = minLengthFactor;
		this.maxLengthFactor = maxLengthFactor;
	}

	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model,
			MembershipOracle<I, Boolean> mqOracle) {
		return new PCRandomWalkEQOracle<>(r, mqOracle, minLengthFactor, maxLengthFactor);
	}

}
