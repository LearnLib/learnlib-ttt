package de.learnlib.algorithms.ttt.dfa;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.features.observationtable.OTUtils;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.PCDFAHashCache;
import de.learnlib.cache.dfa.DFACacheConsistencyTest;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.cache.dfa.DFACaches;
import de.learnlib.eq.PCRandomWalkEQOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle.DFASimulatorEQOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

public class TestRunner {
	
	public static final int NUM_TESTS = 100;
	
	public static <I> Result[] runTests(Alphabet<I> alphabet, DFA<?,I> model,
			LearnerCreator[] learnerCreators) {
		Result[] results = new Result[learnerCreators.length];
		int i = 0;
		for(LearnerCreator lc : learnerCreators) {
			Result r = runTest(alphabet, model, lc);
			results[i++] = r;
		}
		return results;
	}
	
	public static <I> StatisticalResult runTestStatistical(Alphabet<I> alphabet,
			DFA<?,I> model,
			LearnerCreator learner) {
		Result[] res = new Result[NUM_TESTS];
		for(int i = 0; i < NUM_TESTS; i++) {
			do {
				try {
					System.err.println("Test #" + i);
					res[i] = runTest(alphabet, model, learner);
				}
				catch(Exception | AssertionError ex) {
					ex.printStackTrace();
				}
			} while(res[i] == null);
		}
		return new StatisticalResult(res);
	}
	
	public static <I> StatisticalResult runTestStatisticalSingle(Alphabet<I> alphabet,
			DFA<?,I> model,
			Word<I>[] ceWords,
			LearnerCreator learner) {
		Result[] res = new Result[ceWords.length];
		for(int i = 0; i < ceWords.length; i++) {
			do {
				try {
					System.err.println("Run test #" + i);
					res[i] = runTestSingle(alphabet, model, ceWords[i], learner);
				}
				catch(Exception | AssertionError ex) {
					ex.printStackTrace();
				}
			} while(res[i] == null);
		}
		return new StatisticalResult(res);
	}
	
	public static <I> StatisticalResult[] runTestsStatistical(Alphabet<I> alphabet,
			DFA<?,I> model,
			LearnerCreator... learnerCreators) {
		StatisticalResult[] results = new StatisticalResult[learnerCreators.length];
		
		for(int i = 0; i < learnerCreators.length; i++) {
			results[i] = runTestStatistical(alphabet, model, learnerCreators[i]);
		}
		return results;
	}
	
	public static <I> StatisticalResult[] runTestsStatisticalSingle(Alphabet<I> alphabet,
			DFA<?,I> model,
			Word<I>[] ceWords,
			LearnerCreator... learnerCreators) {
		StatisticalResult[] results = new StatisticalResult[learnerCreators.length];
		
		for(int i = 0; i < learnerCreators.length; i++) {
			results[i] = runTestStatisticalSingle(alphabet, model, ceWords, learnerCreators[i]);
		}
		return results;
	}
	
	public static <I> Result runTest(Alphabet<I> alphabet, DFA<?,I> model,
			LearnerCreator learner) {
		System.err.println("Testing learner " + learner.getName());
		DFASimulatorOracle<I> simOracle = new DFASimulatorOracle<>(model);
		DFAStatisticsOracle<I> simOracleStats = new DFAStatisticsOracle<>(simOracle);
		
		//DFACacheOracle<I> cacheOracle = DFACaches.createTreeCache(alphabet, simOracleStats);
		DFACacheOracle<I> cacheOracle = new DFACacheOracle<>(new IncrementalPCDFATreeBuilder<>(alphabet), simOracleStats);
		//PCDFAHashCache<I> cacheOracle = new PCDFAHashCache<>(simOracleStats);
		DFAStatisticsOracle<I> cacheOracleStats = new DFAStatisticsOracle<>(cacheOracle);
		
		MembershipOracle<I,Boolean> effOracle = cacheOracleStats;
		
		EquivalenceOracle<DFA<?,I>,I,Boolean> ccTest = cacheOracle.createCacheConsistencyTest();
		
		EquivalenceOracle<DFA<?,I>,I,Boolean> eqOracle;
		eqOracle = new DFASimulatorEQOracle<>(model);
		//eqOracle = new RandomWordsEQOracle<>(cacheOracleStats, 1, 2*model.size(), 10 * model.size() * alphabet.size(), new Random());
		eqOracle = new PCRandomWalkEQOracle<>(effOracle, 1, 2*model.size(), model.size() * alphabet.size());
		

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
	
	public static <I> Result runTestSingle(Alphabet<I> alphabet, DFA<?,I> model,
			Word<I> ceWord,
			LearnerCreator learner) {
		System.err.println("Testing learner " + learner.getName());
		DFASimulatorOracle<I> simOracle = new DFASimulatorOracle<>(model);
		DFAStatisticsOracle<I> simOracleStats = new DFAStatisticsOracle<>(simOracle);
		
		DFACacheOracle<I> cacheOracle = DFACaches.createTreeCache(alphabet, simOracleStats);
		DFAStatisticsOracle<I> cacheOracleStats = new DFAStatisticsOracle<>(cacheOracle);
		
		
		
		DFACacheConsistencyTest<I> ccTest = cacheOracle.createCacheConsistencyTest();
		
		//EquivalenceOracle<DFA<?,I>,I,Boolean> eqOracle;
		//eqOracle = new DFASimulatorEQOracle<>(model);
		//eqOracle = new RandomWordsEQOracle<>(cacheOracleStats, 1, 2*model.size(), model.size() * alphabet.size(), new Random());
		

		LearningAlgorithm<DFA<?,I>, I, Boolean> dfaLearner
			= learner.createLearner(alphabet, cacheOracleStats);
		
		dfaLearner.startLearning();
		ensureCacheConsistency(alphabet, ccTest, dfaLearner);
		
		DefaultQuery<I,Boolean> ce = new DefaultQuery<>(ceWord, model.computeOutput(ceWord));
		
		System.err.println(dfaLearner.refineHypothesis(ce));
		ensureCacheConsistency(alphabet, ccTest, dfaLearner);
		
		long lastTotalQueries = cacheOracleStats.getQueryCount();
		long lastTotalQueriesSymbols = cacheOracleStats.getSymbolCount();
		
		long lastUniqueQueries = simOracleStats.getQueryCount();
		long lastUniqueQueriesSymbols = simOracleStats.getSymbolCount();
		
		
		System.err.println("CEword was " + ceWord);
		System.err.println("Hypothesis has " + dfaLearner.getHypothesisModel().size() + " states");
		Word<I> sep;
		if((sep = Automata.findSeparatingWord(model, dfaLearner.getHypothesisModel(), alphabet)) != null) {
			System.err.println(sep);
			ExtensibleLStarDFA<I> lstar = (ExtensibleLStarDFA<I>)dfaLearner;
			try(Writer w = DOT.createDotWriter(true)) {
				GraphDOT.write(dfaLearner.getHypothesisModel(), alphabet, w);
				OTUtils.displayHTMLInBrowser(lstar.getObservationTable());
			}
			catch(IOException ex) {}
			
			throw new AssertionError();
		}
		
		Result res = new Result(learner.getName());
		
		res.totalQueries = lastTotalQueries;
		res.totalQueriesSymbols = lastTotalQueriesSymbols;
		
		res.uniqueQueries = lastUniqueQueries;
		res.uniqueQueriesSymbols = lastUniqueQueriesSymbols;
		
		res.totalRounds = 1L;
		
		return res;
	}
	
	private static <I> void ensureCacheConsistency(Alphabet<I> alphabet,
			EquivalenceOracle<DFA<?,I>,I,Boolean> ccTest,
			LearningAlgorithm<DFA<?,I>, I, Boolean> learner) {
		DefaultQuery<I, Boolean> incons;
		
		while((incons = ccTest.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
			learner.refineHypothesis(incons);
		}
	}

}
