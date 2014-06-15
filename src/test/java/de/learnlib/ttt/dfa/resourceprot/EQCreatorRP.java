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

import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreator;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;

public class EQCreatorRP extends EQCreator {

	private final int length;
	private final Map<Action.Type,Double> actionDistribution;
	
	public EQCreatorRP(int length, Map<Action.Type,Double> actionDistribution) {
		this.length = length;
		this.actionDistribution = actionDistribution;
	}
	
	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model,
			MembershipOracle<I, Boolean> mqOracle) {
		return (EquivalenceOracle)new RPEQOracle(length, (DFA)model, actionDistribution);
	}

}
