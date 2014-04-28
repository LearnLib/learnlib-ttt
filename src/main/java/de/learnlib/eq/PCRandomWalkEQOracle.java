package de.learnlib.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class PCRandomWalkEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final MembershipOracle<I, Boolean> mqOracle;
	
	private final Random random;
	
	private final float minLengthFactor;
	private final float maxLengthFactor;
	
	public PCRandomWalkEQOracle(MembershipOracle<I, Boolean> mqOracle,
			float minLengthFactor, float maxLengthFactor) {
		this(new Random(), mqOracle, minLengthFactor, maxLengthFactor);
	}
	
	public PCRandomWalkEQOracle(Random random, MembershipOracle<I, Boolean> mqOracle,
			float minLengthFactor, float maxLengthFactor) {
		this.random = random;
		this.mqOracle = mqOracle;
		this.minLengthFactor = minLengthFactor;
		this.maxLengthFactor = maxLengthFactor;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(hypothesis, inputs);
	}
	
	private <S> DefaultQuery<I, Boolean> doFindCounterExample(DFA<S, I> hypothesis,
			Collection<? extends I> inputs) {

		List<I> candidates = new ArrayList<>();
		
		int minLength = (int)((float)hypothesis.size() * minLengthFactor);
		int maxLength = (int)((float)hypothesis.size() * maxLengthFactor);
		
		for (;;) {
			S curr = hypothesis.getInitialState();
			
			int length = minLength + random.nextInt((maxLength - minLength) + 1);

			WordBuilder<I> testtrace = new WordBuilder<>(length);
			
			for(int j = 0; j < length; j++) {
				for(I sym : inputs) {
					S succ = hypothesis.getSuccessor(curr, sym);
					if(hypothesis.isAccepting(succ)) {
						candidates.add(sym);
					}
				}
				
				if(candidates.isEmpty()) {
					candidates.addAll(inputs);
				}
				int symIdx = random.nextInt(candidates.size());
				I sym = candidates.get(symIdx);
				candidates.clear();
				
				testtrace.append(sym);
				
				curr = hypothesis.getSuccessor(curr, sym);
			}
			
			final DefaultQuery<I, Boolean> query = new DefaultQuery<>(testtrace.toWord());
			
			// query oracle
			mqOracle.processQueries(Collections.singleton(query));
			
			Boolean oracleoutput = query.getOutput();

			// trace hypothesis
			Boolean hypOutput = hypothesis.computeOutput(testtrace);

			// compare output of hypothesis and oracle
			if (!Objects.equals(oracleoutput, hypOutput)) {
				return query;
			}
		}
		
	}

}
