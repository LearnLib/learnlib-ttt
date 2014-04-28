package de.learnlib.algorithms.ttt.dfa.eq;

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eq.RandomSampleEQOracle;

public class EQCreatorRandomSample extends EQCreator {
	
	private final Random random;
	private final float minLengthFactor;
	private final float maxLengthFactor;
	
	public EQCreatorRandomSample(float minLengthFactor, float maxLengthFactor) {
		this(new Random(), minLengthFactor, maxLengthFactor);
	}
	
	public EQCreatorRandomSample(Random r, float minLengthFactor, float maxLengthFactor)  {
		this.random = r;
		this.minLengthFactor = minLengthFactor;
		this.maxLengthFactor = maxLengthFactor;
	}

	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model, MembershipOracle<I, Boolean> mqOracle) {
		return new RandomSampleEQOracle<>(random, mqOracle, minLengthFactor, maxLengthFactor);
	}

}
