package de.learnlib.eq;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class ExhaustiveEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final DFA<?,I> model;
	private final EquivalenceOracle<DFA<?,I>, I, Boolean> delegate;
	
	public ExhaustiveEQOracle(DFA<?,I> model, EquivalenceOracle<DFA<?,I>, I, Boolean> delegate) {
		this.model = model;
		this.delegate = delegate;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		if(Automata.findSeparatingWord(model, hypothesis, inputs) == null) {
			return null;
		}
		
		return delegate.findCounterExample(hypothesis, inputs);
	}
}
