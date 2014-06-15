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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

/**
 * A white-box equivalence oracle which queries a delegate equivalence oracle
 * only if the hypothesis is not yet correct. This allows, e.g., random sampling without
 * an external termination criterion to be used safely.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class ExhaustiveEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final DFA<?,I> model;
	private final EquivalenceOracle<DFA<?,I>, I, Boolean> delegate;
	
	public ExhaustiveEQOracle(DFA<?,I> model, EquivalenceOracle<DFA<?,I>, I, Boolean> delegate) {
		this.model = model;
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		if(Automata.findSeparatingWord(model, hypothesis, inputs) == null) {
			return null;
		}
		
		return delegate.findCounterExample(hypothesis, inputs);
	}
}
