package de.learnlib.ttt.dfa.resourceprot;

import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreator;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;

public class EQCreatorRP extends EQCreator {

	private final int length;
	private final Map<Action.Type,Double> actionDistribution;
	
	public EQCreatorRP(int length, Map<Action.Type,Double> actionDistribution) {
		this.length = length;
		this.actionDistribution = actionDistribution;
	}
	
	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model,
			MembershipOracle<I, Boolean> mqOracle) {
		return (EquivalenceOracle)new RPEQOracle(length, (DFA)model, actionDistribution);
	}

}
