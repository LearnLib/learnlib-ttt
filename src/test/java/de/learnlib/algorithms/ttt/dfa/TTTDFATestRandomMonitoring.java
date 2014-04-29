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
			= new FWTestRunner(NUM_TESTS, new EQCreatorRandomSample(r2, 0.5f, 2.0f), new TreeCacheCreator());
		
		testRunner.runTests(examples, LearnerCreators.LEARNERS);
		
		System.exit(0);
	}
	
}
