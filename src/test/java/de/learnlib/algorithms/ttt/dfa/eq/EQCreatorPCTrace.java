package de.learnlib.algorithms.ttt.dfa.eq;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;

public class EQCreatorPCTrace extends EQCreator {

	private final int length;
	private final long seed;
	
	
	public EQCreatorPCTrace(int length, long seed) {
		this.length = length;
		this.seed = seed;
	}
	
	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model,
			MembershipOracle<I, Boolean> mqOracle) {
		return new PCTraceEQOracle<>(model, length, seed);
	}

}
