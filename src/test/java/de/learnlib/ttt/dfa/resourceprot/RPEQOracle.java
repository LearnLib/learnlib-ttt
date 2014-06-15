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
package de.learnlib.ttt.dfa.resourceprot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class RPEQOracle implements DFAEquivalenceOracle<Action> {

	private final int length;
	private final DFA<?,Action> model;
	private final Map<Action.Type,Double> actionDistribution;
	
	private final Random random = new Random();
	
	public RPEQOracle(int length, DFA<?,Action> model, Map<Action.Type,Double> actionDistribution) {
		this.length = length;
		this.model = model;
		this.actionDistribution = actionDistribution;
	}

	@Override
	public DefaultQuery<Action, Boolean> findCounterExample(
			DFA<?, Action> hypothesis, Collection<? extends Action> inputs) {
		return doFindCounterExample(model, hypothesis, inputs);
	}
	
	
	private <S1,S2>
	DefaultQuery<Action, Boolean> doFindCounterExample(DFA<S1,Action> model, DFA<S2,Action> hypothesis,
			Collection<? extends Action> inputs) {
		
		
		System.err.println("Search CE");
		for(;;) {
			WordBuilder<Action> trace = new WordBuilder<>();
			
			
			
			S1 currModel = model.getInitialState();
			S2 currHyp = hypothesis.getInitialState();
			for(int i = 0; i < length && model.isAccepting(currModel) && hypothesis.isAccepting(currHyp); i++) {
				List<Action> candidates = new ArrayList<>();
				
				for(Action sym : inputs) {
					S1 succ = model.getSuccessor(currModel, sym);
					if(model.isAccepting(succ) || random.nextInt(1000) == 0) {
						candidates.add(sym);
					}
				}
				
				if(candidates.isEmpty()) {
					candidates.addAll(inputs);
				}
				
				double total = 0.0;
				
				for(Action act : candidates) {
					total += actionDistribution.get(act.type);
				}
				
				double val = random.nextDouble() * total;
				
				Action sym = null;
				
				Iterator<Action> candIt = candidates.iterator();
				
				while(candIt.hasNext() && sym == null) {
					Action act = candIt.next();
					double weight = actionDistribution.get(act.type);
					
					if(!candIt.hasNext() || val < weight) {
						sym = act;
					}
					else {
						val -= weight;
					}
				}
				
				assert sym != null;
				
				trace.add(sym);
				
				currModel = model.getSuccessor(currModel, sym);
				currHyp = hypothesis.getSuccessor(currHyp, sym);
			}
			
			
			if(model.isAccepting(currModel) != hypothesis.isAccepting(currHyp)) {
				DefaultQuery<Action, Boolean> ce = new DefaultQuery<>(trace.toWord(), model.isAccepting(currModel));
				System.err.println("Found CE");
				return ce;
			}
		}
		
	}
	
}
