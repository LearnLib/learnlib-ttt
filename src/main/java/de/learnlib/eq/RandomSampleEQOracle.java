package de.learnlib.eq;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class RandomSampleEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final MembershipOracle<I, Boolean> mqOracle;
	
	private final Random random;
	
	private final float minLengthFactor;
	private final float maxLengthFactor;
	
	public RandomSampleEQOracle(MembershipOracle<I, Boolean> mqOracle,
			float minLengthFactor, float maxLengthFactor) {
		this(new Random(), mqOracle, minLengthFactor, maxLengthFactor);
	}
	
	public RandomSampleEQOracle(Random random, MembershipOracle<I, Boolean> mqOracle,
			float minLengthFactor, float maxLengthFactor) {
		this.random = random;
		this.mqOracle = mqOracle;
		this.minLengthFactor = minLengthFactor;
		this.maxLengthFactor = maxLengthFactor;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		
		List<? extends I> symbols = CollectionsUtil.randomAccessList(inputs);
		int numSymbols = symbols.size();
		
		int minLength = (int)((float)hypothesis.size() * minLengthFactor);
		int maxLength = (int)((float)hypothesis.size() * maxLengthFactor);
		
		for (;;) {
			int length = minLength + random.nextInt((maxLength - minLength) + 1);

			WordBuilder<I> testtrace = new WordBuilder<>(length);
			
			for(int i = 0; i < length; i++) {
				testtrace.add(symbols.get(random.nextInt(numSymbols)));
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
