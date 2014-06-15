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

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eq.RandomSampleEQOracle;

public class EQCreatorRandomSample extends EQCreator {
	
	private final Random random;
	private final float minLengthFactor;
	private final float maxLengthFactor;
	
	public EQCreatorRandomSample(float minLengthFactor, float maxLengthFactor) {
		this(new Random(), minLengthFactor, maxLengthFactor);
	}
	
	public EQCreatorRandomSample(Random r, float minLengthFactor, float maxLengthFactor)  {
		this.random = r;
		this.minLengthFactor = minLengthFactor;
		this.maxLengthFactor = maxLengthFactor;
	}

	@Override
	protected <I> EquivalenceOracle<DFA<?, I>, I, Boolean> doCreateEQOracle(
			Alphabet<I> alphabet, DFA<?, I> model, MembershipOracle<I, Boolean> mqOracle) {
		return new RandomSampleEQOracle<>(random, mqOracle, minLengthFactor, maxLengthFactor);
	}

}
