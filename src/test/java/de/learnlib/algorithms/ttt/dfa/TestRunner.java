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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.cache.CacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreator;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.statistics.DFAStatisticsOracle;

public class TestRunner {
	
	
	private final class RunTest<I> implements Callable<Void> {
		
		private final int i;
		private final Result[] storage;
		private final DFALearningExample<I> example;
		private final LearnerCreator learner;
		
		public RunTest(int testId, Result[] storage, DFALearningExample<I> example, LearnerCreator learner) {
			this.i = testId;
			this.storage = storage;
			this.example = example;
			this.learner = learner;
		}

		@Override
		public Void call() throws Exception {
			for(;;) {
				try {
					System.err.println("Running " + learner.getName() + " test " + i + "/" + storage.length + " on " + example.toString());
					Result res = runTest(example.getAlphabet(), example.getReferenceAutomaton(), learner);
					storage[i] = res;
					System.err.println(learner.getName() + " test " + i + "/" + storage.length + " on " + example.toString() + " finished");
					return null;
				}
				catch(Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	
	private final int numTests;
	private final ExecutorService exec;
	
	private final EQCreator eqCreator;
	private final CacheCreator cacheCreator;
	
	public TestRunner(int numTests, EQCreator eqCreator, CacheCreator cacheCreator) {
		this(numTests, eqCreator, cacheCreator, -1);
	}
	
	public TestRunner(int numTests, EQCreator eqCreator, CacheCreator cacheCreator, int numThreads) {
		if(numThreads < 0) {
			numThreads = Runtime.getRuntime().availableProcessors();
		}
		this.numTests = numTests;
		this.eqCreator = eqCreator;
		this.cacheCreator = cacheCreator;
		this.exec = Executors.newFixedThreadPool(numThreads);
	}
	
	
	public <I> Map<String,Map<String,StatisticalResult>> runTests(
			List<? extends DFALearningExample<?>> examples,
			LearnerCreator... learnerCreators) throws InterruptedException {
		
		Map<String,Map<String,Result[]>> singleResults = new HashMap<>();
		
		// List<Callable<Void>> jobs = new ArrayList<>();
		List<Future<?>> futures = new ArrayList<>();
		
		for(DFALearningExample<?> ex : examples) {
			Map<String,Result[]> perExample = new HashMap<>();
			singleResults.put(ex.toString(), perExample);
			for(LearnerCreator lc : learnerCreators) {
				Result[] results = new Result[numTests];
				perExample.put(lc.getName(), results);
				for(int i = 0; i < results.length; i++) {
					Callable<Void> job = new RunTest<>(i, results, ex, lc);
					// jobs.add(job);
					Future<?> fut = exec.submit(job);
					futures.add(fut);
				}
			}
		}
		
		for(Future<?> f : futures) {
			try {
				f.get();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return aggregateResults(singleResults);
	}
	
	
	public <I> Result runTest(Alphabet<I> alphabet, DFA<?,I> model,
			LearnerCreator learner) {
		
		DFASimulatorOracle<I> simOracle = new DFASimulatorOracle<>(model);
		DFAStatisticsOracle<I> simOracleStats = new DFAStatisticsOracle<>(simOracle);
		
		DFACacheOracle<I> cacheOracle = cacheCreator.createCache(alphabet, simOracleStats);
		DFAStatisticsOracle<I> cacheOracleStats = new DFAStatisticsOracle<>(cacheOracle);
		
		MembershipOracle<I,Boolean> effOracle = cacheOracleStats;
		
		EquivalenceOracle<DFA<?,I>,I,Boolean> ccTest = cacheOracle.createCacheConsistencyTest();
		
		EquivalenceOracle<DFA<?,I>,I,Boolean> eqOracle = eqCreator.createEQOracle(alphabet, model, cacheOracleStats);

		LearningAlgorithm<DFA<?,I>, I, Boolean> dfaLearner
			= learner.createLearner(alphabet, effOracle);
		
		dfaLearner.startLearning();
		ensureCacheConsistency(alphabet, ccTest, dfaLearner);
		
		DefaultQuery<I,Boolean> ce;
		
		long lastTotalQueries = cacheOracleStats.getQueryCount();
		long lastTotalQueriesSymbols = cacheOracleStats.getSymbolCount();
		
		long lastUniqueQueries = simOracleStats.getQueryCount();
		long lastUniqueQueriesSymbols = simOracleStats.getSymbolCount();
		
		long rounds = 0L;
		while((ce = eqOracle.findCounterExample(dfaLearner.getHypothesisModel(), alphabet)) != null) {
			while(dfaLearner.refineHypothesis(ce));
			//System.err.println("Finished refining in " + learner.getName());
			ensureCacheConsistency(alphabet, ccTest, dfaLearner);
			
			rounds++;
			
			if(rounds > model.size()) {
				throw new AssertionError();
			}
			lastTotalQueries = cacheOracleStats.getQueryCount();
			lastTotalQueriesSymbols = cacheOracleStats.getSymbolCount();
			
			lastUniqueQueries = simOracleStats.getQueryCount();
			lastUniqueQueriesSymbols = simOracleStats.getSymbolCount();
		}
		
		if(Automata.findSeparatingWord(model, dfaLearner.getHypothesisModel(), alphabet) != null) {
			System.err.println("Hyp has " + dfaLearner.getHypothesisModel().size() + " states");
			throw new AssertionError();
		}
		
		Result res = new Result(learner.getName());
		
		res.totalQueries = lastTotalQueries;
		res.totalQueriesSymbols = lastTotalQueriesSymbols;
		
		res.uniqueQueries = lastUniqueQueries;
		res.uniqueQueriesSymbols = lastUniqueQueriesSymbols;
		
		res.totalRounds = rounds;
		
		return res;
	}
	
	
	private static Map<String,Map<String,StatisticalResult>> aggregateResults(Map<String,Map<String,Result[]>> singleResults) {
		Map<String,Map<String,StatisticalResult>> aggregated = new HashMap<>();
		
		for(Map.Entry<String,Map<String,Result[]>> sre : singleResults.entrySet()) {
			String modelName = sre.getKey();
			Map<String,Result[]> learnerResults = sre.getValue();
			
			Map<String,StatisticalResult> aggSingle = aggregateResultsSingle(learnerResults);
			aggregated.put(modelName, aggSingle);
		}
		
		return aggregated;
	}
	
	private static Map<String,StatisticalResult> aggregateResultsSingle(Map<String,Result[]> singleResults) {
		Map<String,StatisticalResult> aggregated = new HashMap<>();
		
		for(Map.Entry<String,Result[]> sre : singleResults.entrySet()) {
			String learnerName = sre.getKey();
			StatisticalResult sr = new StatisticalResult(sre.getValue());
			aggregated.put(learnerName, sr);
		}
		
		return aggregated;
	}
	
	
	
	public void shutdown() {
		exec.shutdown();
	}
	
	private static <I> void ensureCacheConsistency(Alphabet<I> alphabet,
			EquivalenceOracle<DFA<?,I>,I,Boolean> ccTest,
			LearningAlgorithm<DFA<?,I>, I, Boolean> learner) {
		
		DefaultQuery<I, Boolean> incons;
		
		while((incons = ccTest.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
			learner.refineHypothesis(incons);
		}
	}

	public static void printResults(
			Map<String, Map<String, StatisticalResult>> results, PrintStream ps) {
		for(Map.Entry<String,Map<String,StatisticalResult>> e1 : results.entrySet()) {
			ps.println("Results for example '" + e1.getKey() + "'");
			ps.println("=============================================");
			for(StatisticalResult res : e1.getValue().values()) {
				ps.println(res.toString());
			}
			ps.println();
		}
		
		for(Map.Entry<String,Map<String,StatisticalResult>> e1 : results.entrySet()) {
			ps.println("TeX Results for example '" + e1.getKey() + "'");
			ps.println("=============================================");
			for(StatisticalResult res : e1.getValue().values()) {
				ps.println(res.toLatexStringShort());
			}
			ps.println();
		}
	}

}
