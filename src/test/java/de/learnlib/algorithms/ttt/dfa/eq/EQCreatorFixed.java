package de.learnlib.algorithms.ttt.dfa.eq;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eqtests.basic.SampleSetEQOracle;
import de.learnlib.oracles.DefaultQuery;

public class EQCreatorFixed<I> extends EQCreator {
	
	private final SampleSetEQOracle<I, Boolean> sampleSetEQOracle; 
	
	public EQCreatorFixed(Collection<? extends DefaultQuery<I, Boolean>> ceList) {
		this.sampleSetEQOracle = new SampleSetEQOracle<>(false);
		sampleSetEQOracle.addAll(ceList);
	}


	@Override
	@SuppressWarnings("unchecked") // evil hack
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model,
			MembershipOracle<I, Boolean> mqOracle) {
		return new DFAEquivalenceOracle<I>() {
			@Override
			public DefaultQuery<I, Boolean> findCounterExample(
					DFA<?, I> hypothesis, Collection<? extends I> inputs) {
				DefaultQuery ce = sampleSetEQOracle.findCounterExample((DFA)hypothesis, (Collection)inputs);
				
				if(ce == null) {
					throw new AssertionError();
				}
				
				return ce;
			}
			
		};
	}
	
	
	
	

}
