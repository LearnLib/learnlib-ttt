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
package de.learnlib.ttt.algorithms.dfa.examples;

import java.util.Arrays;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.LearningExample.DFALearningExample;

public class RandomDFAExample implements DFALearningExample<Integer> {
	
	private final CompactDFA<Integer> model;
	
	public RandomDFAExample(int alphabetSize, int modelSize, Random random) {
		Alphabet<Integer> alphabet = Alphabets.integers(0, alphabetSize - 1);
		this.model = RandomAutomata.randomDeterministic(random, modelSize, alphabet, Arrays.asList(true, true, true, true, true, false), Arrays.asList((Void)null), new CompactDFA<Integer>(alphabet));
	}

	@Override
	public DFA<?, Integer> getReferenceAutomaton() {
		return model;
	}

	@Override
	public Alphabet<Integer> getAlphabet() {
		return model.getInputAlphabet();
	}
	
	public String toString() {
		return "Random-" + model.getInputAlphabet().size() + "-" + model.size();
	}

}
