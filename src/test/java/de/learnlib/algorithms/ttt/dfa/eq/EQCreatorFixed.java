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
	@SuppressWarnings({"unchecked","rawtypes"}) // evil hack
	protected <I2> EquivalenceOracle<DFA<?, I2>, I2, Boolean> doCreateEQOracle(
			Alphabet<I2> alphabet, DFA<?, I2> model,
			MembershipOracle<I2, Boolean> mqOracle) {
		return new DFAEquivalenceOracle<I2>() {
			@Override
			public DefaultQuery<I2, Boolean> findCounterExample(
					DFA<?, I2> hypothesis, Collection<? extends I2> inputs) {
				DefaultQuery ce = sampleSetEQOracle.findCounterExample((DFA)hypothesis, (Collection)inputs);
				
				if(ce == null) {
					throw new AssertionError();
				}
				
				return ce;
			}
			
		};
	}
	
	
	
	

}
