package de.learnlib.eq;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class DFASimulatorEQOracleDFS<I> implements DFAEquivalenceOracle<I> {
	
	
	
	private static class Record<I,S1,S2> {
		
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		// TODO Auto-generated method stub
		return null;
	}

}
