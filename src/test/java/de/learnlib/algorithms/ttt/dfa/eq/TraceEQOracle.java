package de.learnlib.algorithms.ttt.dfa.eq;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class TraceEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final long seed;
	private final DFA<?,I> model;
	private final int length;

	public TraceEQOracle(DFA<?,I> model, int length, long seed) {
		this.model = model;
		this.seed = seed;
		this.length = length;
	}
	
	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(model, hypothesis, inputs, length, new Random(seed));
	}
	
	private static <S1,S2,I>
	DefaultQuery<I, Boolean> doFindCounterExample(DFA<S1,I> model, DFA<S2,I> hypothesis,
			Collection<? extends I> inputsColl, int length, Random r) {
		
		List<? extends I> inputs = CollectionsUtil.randomAccessList(inputsColl);
		
		for(;;) {
			WordBuilder<I> trace = new WordBuilder<>();
			
			S1 currModel = model.getInitialState();
			S2 currHyp = hypothesis.getInitialState();
			for(int i = 0; i < length; i++) {
				int symIdx = r.nextInt(inputs.size());
				
				I sym = inputs.get(symIdx);
				
				trace.add(sym);
				
				currModel = model.getSuccessor(currModel, sym);
				currHyp = hypothesis.getSuccessor(currHyp, sym);
			}
			
			
			if(model.isAccepting(currModel) != hypothesis.isAccepting(currHyp)) {
				DefaultQuery<I, Boolean> ce = new DefaultQuery<>(trace.toWord(), model.isAccepting(currModel));
				return ce;
			}
		}
		
	}

}
