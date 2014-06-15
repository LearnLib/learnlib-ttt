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
package de.learnlib.algorithms.ttt.dfa;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.importers.aut.AUTImporter;

public class RealisticSystem implements DFALearningExample<Integer> {
	
	private final String name;
	private final CompactDFA<Integer> model;
	
	public RealisticSystem(String name) throws IOException {
		this.name = name;
		try(InputStream is = RealisticSystem.class.getResourceAsStream("/" + name + ".dfa.gz")) {
			this.model = AUTImporter.read(is);
		}
	}

	@Override
	public DFA<?, Integer> getReferenceAutomaton() {
		return model;
	}

	@Override
	public Alphabet<Integer> getAlphabet() {
		return model.getInputAlphabet();
	}
	
	@Override
	public String toString() {
		return name;
	}

}
