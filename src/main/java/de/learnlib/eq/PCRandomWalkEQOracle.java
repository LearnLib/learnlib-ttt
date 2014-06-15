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

/**
 * A modified random sampling oracle for prefix-closed systems, only taking into
 * account words that form valid traces in the hypothesis. This addresses the fact
 * normal random sampling almost always leads words which run into a sink in the hypothesis.
 * Note that, apart from exploiting the knowledge
 * that the target model is prefix-closed, this is still entirely black-box.
 * <p>
 * There is no upper bound on the number of tests performed by this oracle. It keeps on
 * sampling until a counterexample is found. It is therefore <b>highly</b> recommended
 * to use it in conjunction with an {@link ExhaustiveEQOracle} only.
 * <p>
 * <b>Caveat:</b> there might be cases when this equivalence oracle is incapable of finding
 * a counterexample, even if there exists one, leading to an infinite loop even in the
 * aforementioned setup. This is the case if the language accepted
 * by the hypothesis is a proper subset of the target language. Since the difference between
 * the two languages only contains words rejected by the hypothesis, while this oracle
 * only examines words accepted by the hypothesis, no solution might be found.
 * 
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
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
