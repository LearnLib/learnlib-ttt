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
package de.learnlib.algorithms.ttt.dfa.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class PCTraceEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final long seed;
	private final DFA<?,I> model;
	private final int length;

	public PCTraceEQOracle(DFA<?,I> model, int length, long seed) {
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
			Collection<? extends I> inputs, int length, Random r) {
		
		for(;;) {
			WordBuilder<I> trace = new WordBuilder<>();
			
			S1 currModel = model.getInitialState();
			S2 currHyp = hypothesis.getInitialState();
			for(int i = 0; i < length && model.isAccepting(currModel); i++) {
				List<I> candidates = new ArrayList<>();
				
				for(I sym : inputs) {
					S1 succ = model.getSuccessor(currModel, sym);
					if(model.isAccepting(succ)) {
						candidates.add(sym);
					}
				}
				
				if(candidates.isEmpty()) {
					candidates.addAll(inputs);
				}
				
				int symIdx = r.nextInt(candidates.size());
				
				I sym = candidates.get(symIdx);
				
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
