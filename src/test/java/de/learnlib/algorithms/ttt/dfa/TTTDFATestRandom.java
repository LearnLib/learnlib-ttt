package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.learnlib.algorithms.ttt.dfa.cache.TreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorRandomSample;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.ttt.algorithms.dfa.examples.RandomDFAExample;

public class TTTDFATestRandom {
	
	
	private static final int NUM_TESTS = 100;
	
	public static void main(String[] args) throws Exception {
		
		List<DFALearningExample<Integer>> examples = new ArrayList<>();
		
		Random r = new Random();
		
		examples.add(new RandomDFAExample(5, 100, r));
		examples.add(new RandomDFAExample(10, 100, r));
		examples.add(new RandomDFAExample(10, 200, r));
		examples.add(new RandomDFAExample(20, 200, r));
		
		TestRunner testRunner
			= new TestRunner(NUM_TESTS, new EQCreatorRandomSample(0.5f, 2.0f), new TreeCacheCreator());
		
		Map<String,Map<String,StatisticalResult>> results = testRunner.runTests(examples, LearnerCreators.LEARNERS);
		
		TestRunner.printResults(results, System.out);
	}
	
}
