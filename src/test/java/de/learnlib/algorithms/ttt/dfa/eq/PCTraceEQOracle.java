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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class PCTraceEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final Random random;
	private final DFA<?,I> model;
	private final Alphabet<I> alphabet;
	private final int length;

	public PCTraceEQOracle(DFA<?,I> model, Alphabet<I> alphabet, int length, long seed) {
		this.model = model;
		this.alphabet = alphabet;
		this.random = new Random(seed);
		this.length = length;
	}
	
	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(model, hypothesis, alphabet, length, random);
	}
	
	private static <S1,S2,I>
	DefaultQuery<I, Boolean> doFindCounterExample(DFA<S1,I> model, DFA<S2,I> hypothesis,
			Alphabet<I> alphabet, int length, Random r) {
		
		int c = 0;
		
		float rejectProb = 2.0f/length;
		
		CompactDFA<I> negHyp = DFAs.complement(hypothesis, alphabet);
		CompactDFA<I> ceDFA = DFAs.and(model, negHyp, alphabet);
		ceDFA = Automata.invasiveMinimize(ceDFA, alphabet);
		
		if(ceDFA.size() == 1 && !ceDFA.isAccepting(ceDFA.getInitialState())) {
			ceDFA = DFAs.xor(model, hypothesis, alphabet);
			ceDFA = Automata.invasiveMinimize(ceDFA, alphabet);
		}
		
		int size = ceDFA.size();
		int k = alphabet.size();
		
		int[] accDists = new int[size];
		Arrays.fill(accDists, Integer.MAX_VALUE);
		
		for(int i = 0; i < size; i++) {
			if(ceDFA.isAccepting(i)) {
				accDists[i] = 0;
			}
		}
		
		boolean stable;
		
		do {
			stable = true;
			for(int state = 0; state < size; state++) {
				int succMinDist = Integer.MAX_VALUE;
				for(I sym : alphabet) {
					int succ = ceDFA.getIntSuccessor(state, sym);
					succMinDist = Math.min(succMinDist, accDists[succ]);
				}
				if(succMinDist == Integer.MAX_VALUE) {
					continue;
				}
				succMinDist++;
				if(succMinDist < accDists[state]) {
					accDists[state] = succMinDist;
					stable = false;
				}
			}
		} while(!stable);
		
		
		int remaining = length;
		if(accDists[ceDFA.getIntInitialState()] > remaining) {
			remaining = accDists[ceDFA.getIntInitialState()];
		}
		
		int currState = ceDFA.getIntInitialState();
		WordBuilder<I> traceBuilder = new WordBuilder<>(remaining);
		while(remaining > 0) {
			remaining--;
			
			List<I> candidates = new ArrayList<>();
			for(I sym : alphabet) {
				int succ = ceDFA.getIntSuccessor(currState, sym);
				if(accDists[succ] <= remaining) {
					candidates.add(sym);
				}
			}
			
			if(candidates.isEmpty()) {
				throw new AssertionError();
			}
			
			int symIdx = r.nextInt(candidates.size());
			
			I sym = candidates.get(symIdx);
			
			traceBuilder.add(sym);
			currState = ceDFA.getIntSuccessor(currState, sym);
		}

		if(!ceDFA.isAccepting(currState)) {
			throw new AssertionError();
		}
		
		Word<I> trace = traceBuilder.toWord();
		DefaultQuery<I, Boolean> ce = new DefaultQuery<>(trace, model.accepts(trace));
		return ce;
	}

}
