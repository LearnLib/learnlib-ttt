package de.learnlib.algorithms.ttt.dfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class FWTestRunner {
	
	
	private final class RunTest<I> implements Callable<Void> {
		
		private final int i;
		private final PrintStream ps;
		private final DFALearningExample<I> example;
		private final LearnerCreator learner;
		
		public RunTest(int testId, PrintStream ps, DFALearningExample<I> example, LearnerCreator learner) {
			this.i = testId;
			this.ps = ps;
			this.example = example;
			this.learner = learner;
		}

		@Override
		public Void call() throws Exception {
			for(;;) {
				try {
					System.err.println("Running " + learner.getName() + " test " + i + " on " + example.toString());
					Result res = runTest(example.getAlphabet(), example.getReferenceAutomaton(), learner);
					res.printRaw(ps);
					System.err.println(learner.getName() + " test " + i + " on " + example.toString() + " finished");
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
	
	public FWTestRunner(int numTests, EQCreator eqCreator, CacheCreator cacheCreator) {
		this(numTests, eqCreator, cacheCreator, -1);
	}
	
	public FWTestRunner(int numTests, EQCreator eqCreator, CacheCreator cacheCreator, int numThreads) {
		if(numThreads < 0) {
			numThreads = Runtime.getRuntime().availableProcessors();
		}
		this.numTests = numTests;
		this.eqCreator = eqCreator;
		this.cacheCreator = cacheCreator;
		this.exec = Executors.newFixedThreadPool(numThreads);
	}
	
	
	public <I> void runTests(
			List<? extends DFALearningExample<?>> examples,
			LearnerCreator... learnerCreators) throws Exception {
		
		Date d = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMdd-HHmmss");
		String dateStr = fmt.format(d);
		
		File dir = new File(new File("results"), dateStr);
		if(dir.exists()) {
			throw new IllegalStateException();
		}
		dir.mkdirs();
		
		// List<Callable<Void>> jobs = new ArrayList<>();
		List<Future<?>> futures = new ArrayList<>();
		
		List<FileOutputStream> outputStreams = new ArrayList<>();
		
		for(DFALearningExample<?> ex : examples) {
			for(LearnerCreator lc : learnerCreators) {
				int c = 0;
				String basename = ex.toString() + "-" + lc.getName();
				File f = new File(dir, basename + ".dat");
				while(f.exists()) {
					f = new File(dir, basename + "-" + ++c + ".dat");
				}
				FileOutputStream fos = new FileOutputStream(f);
				outputStreams.add(fos);
				
				PrintStream ps = new PrintStream(fos);
				
				for(int i = 0; i < numTests; i++) {
					Callable<Void> job = new RunTest<>(i, ps, ex, lc);
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
		
		System.err.println("Closing streams");
		for(FileOutputStream fos : outputStreams) {
			try {
				fos.close();
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
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
