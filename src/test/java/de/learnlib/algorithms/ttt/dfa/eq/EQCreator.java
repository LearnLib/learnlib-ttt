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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eq.ExhaustiveEQOracle;

public abstract class EQCreator {
	
	public <I> EquivalenceOracle<DFA<?,I>, I, Boolean> createEQOracle(Alphabet<I> alphabet,
			DFA<?,I> model, MembershipOracle<I, Boolean> mqOracle) {
		EquivalenceOracle<DFA<?,I>, I, Boolean> eqOracle = doCreateEQOracle(alphabet, model, mqOracle);
		
		return new ExhaustiveEQOracle<>(model, eqOracle);
	}
	
	protected abstract <I> EquivalenceOracle<DFA<?,I>, I, Boolean> doCreateEQOracle(Alphabet<I> alphabet,
			DFA<?,I> model, MembershipOracle<I, Boolean> mqOracle);

}
