/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib-TTT, https://github.com/LearnLib/learnlib-ttt/
 * 
 * LearnLib-TTT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LearnLib-TTT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LearnLib-TTT.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Random sampling oracle that, instead of being provided absolute values for the
 * minimum and maximum length, calculates these as a function of the hypothesis size, using
 * provided scaling factors.
 * <p>
 * Furthermore, this oracle does not take an upper bound on the number of tests. Instead,
 * it keeps on sampling until a counterexample is found. It is therefore highly recommended
 * to use it in conjunction with an {@link ExhaustiveEQOracle} only.
 *  
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
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
