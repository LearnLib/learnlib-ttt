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
import java.util.Random;

import de.learnlib.algorithms.ttt.dfa.cache.TreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorRandomSample;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.ttt.algorithms.dfa.examples.RandomDFAExample;

public class TTTDFATestRandom {
	
	
	private static final int NUM_TESTS = 30;
	
	private static final int NUM_DFA = 1;
	
	private static final int[][] sizes = {
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
			= new FWTestRunner("random-sample", NUM_TESTS, new EQCreatorRandomSample(0.5f, 2.0f), new TreeCacheCreator());
		
		testRunner.runTests(examples, LearnerCreators.LEARNERS);
		testRunner.shutdown();
	
	/*	
		FWTestRunner testRunner2
			= new FWTestRunner("random-fixed-500", NUM_TESTS, new EQCreatorTrace(1000, 2L), new TreeCacheCreator());
		
		testRunner2.runTests(examples, LearnerCreators.LEARNERS);
		
		testRunner2.shutdown();
		*/
		System.exit(0);
	}
	
}
