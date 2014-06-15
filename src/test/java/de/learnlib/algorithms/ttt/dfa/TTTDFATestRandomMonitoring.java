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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.learnlib.algorithms.ttt.dfa.cache.TreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorRandomSample;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.ttt.algorithms.dfa.examples.RandomDFAExample;

public class TTTDFATestRandomMonitoring {
	
	
	private static final int NUM_TESTS = 1000;
	
	private static final int NUM_DFA = 10;
	
	private static final int[][] sizes = {
		{ 5, 100 },
		{ 10, 100 },
		{ 10, 200 },
		{ 20, 200 }
	};
	
	public static void main(String[] args) throws Exception {
		
		List<DFALearningExample<Integer>> examples = new ArrayList<>();
		
		Random r = new Random(1337L);
		
		for(int i = 0; i < NUM_DFA; i++) {
			for(int j = 0; j < sizes.length; j++) {
				RandomDFAExample ex = new RandomDFAExample(sizes[j][0], sizes[j][1], r);
				while(ex.getReferenceAutomaton().size() != sizes[j][1]) {
					ex = new RandomDFAExample(sizes[j][0], sizes[j][1], r);
				}
				examples.add(ex);
			}
		}
		
		Random r2 = new Random(42L);
				
		FWTestRunner testRunner
			= new FWTestRunner("monitoring", NUM_TESTS, new EQCreatorRandomSample(r2, 0.5f, 2.0f), new TreeCacheCreator());
		
		testRunner.runTests(examples, LearnerCreators.LEARNERS);
		
		System.exit(0);
	}
	
}
