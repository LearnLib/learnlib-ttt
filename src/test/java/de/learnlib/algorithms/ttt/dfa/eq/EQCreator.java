package de.learnlib.algorithms.ttt.dfa.eq;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eq.ExhaustiveEQOracle;

public abstract class EQCreator {
	
	public <I> EquivalenceOracle<DFA<?,I>, I, Boolean> createEQOracle(Alphabet<I> alphabet,
			DFA<?,I> model, MembershipOracle<I, Boolean> mqOracle) {
		EquivalenceOracle<DFA<?,I>, I, Boolean> eqOracle = doCreateEQOracle(alphabet, model, mqOracle);
		
		return new ExhaustiveEQOracle<>(model, eqOracle);
	}
	
	protected abstract <I> EquivalenceOracle<DFA<?,I>, I, Boolean> doCreateEQOracle(Alphabet<I> alphabet,
			DFA<?,I> model, MembershipOracle<I, Boolean> mqOracle);

}
