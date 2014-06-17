package de.learnlib.algorithms.ttt.dfa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.TTTExperiment.Config;
import de.learnlib.algorithms.ttt.dfa.eq.PCTraceEQOracle;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.statistics.DFAStatisticsOracle;

public class ExperimentRunner {
	
	private static final class RunTest<I> implements Callable<Void> {	
		private final int testId;
		private final int ceLength;
		private final PrintStream ps;
		private final DFALearningExample<I> example;
		private final LearnerCreator learner;
		private final long seed;
		
		public RunTest(int testId, int ceLength, long seed, PrintStream ps, DFALearningExample<I> example, LearnerCreator learner) {
			this.testId = testId;
			this.ceLength = ceLength;
			this.ps = ps;
			this.example = example;
			this.learner = learner;
			this.seed = seed;
		}

		@Override
		public Void call() throws Exception {
			for(;;) {
				try {
					System.err.println("Running " + learner.getName() + " test " + testId + " on " + example.toString() + ", ce length = " + ceLength);
					Result res = runTest(example.getAlphabet(), example.getReferenceAutomaton(), learner, ceLength, seed);
					synchronized(ps) {
						ps.println(String.format("%d %d %d", ceLength, res.totalQueries, res.totalQueriesSymbols));
						ps.flush();
					}
					System.err.println(learner.getName() + " test " + testId + " on " + example.toString() + " finished");
					return null;
				}
				catch(Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private final Config config;
	
	public ExperimentRunner(Config config) {
		this.config = config;
	}
	
	
	public void run() throws FileNotFoundException, IOException {
		ExecutorService exec = Executors.newFixedThreadPool(config.numThreads);
		
		File resultDir = new File("results");
		File outputDir = new File(resultDir, config.outputName);
		
		outputDir.mkdirs();
		
		File configFile = new File(outputDir, "config");
		try(PrintStream ps = new PrintStream(configFile)) {
			config.print(ps);
		}
		
		RealisticSystem target = new RealisticSystem(config.targetSystem);
		LearnerCreator[] learners = LearnerCreators.getLearners(config.learners);
		
		PrintStream[] outputStreams = new PrintStream[learners.length];
		for(int i = 0; i < learners.length; i++) {
			String name = learners[i].getName();
			File outputFile = new File(outputDir, name + ".dat");
			outputStreams[i] = new PrintStream(outputFile);
		}
		
		Random random = new Random(config.seed);
		
		List<Future<?>> futures = new ArrayList<>();
		for(int i = config.ceLengthMin; i <= config.ceLengthMax; i += config.ceLengthStep) {
			for(int j = 0; j < config.repeatCount; j++) {
				long localSeed = random.nextLong();
				for(int k = 0; k < learners.length; k++) {
					LearnerCreator learner = learners[k];
					PrintStream stream = outputStreams[k];
					RunTest<Integer> rt = new RunTest<>(j, i, localSeed, stream, target, learner);
					Future<?> fut = exec.submit(rt);
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
		
		for(PrintStream ps : outputStreams) {
			ps.close();
		}
		
		exec.shutdown();
	}
	
	
	public static <I> Result runTest(Alphabet<I> alphabet, DFA<?,I> model,
			LearnerCreator learner, int ceLength, long seed) {
		
		DFASimulatorOracle<I> simOracle = new DFASimulatorOracle<>(model);
		DFAStatisticsOracle<I> simOracleStats = new DFAStatisticsOracle<>(simOracle);
		
		MembershipOracle<I,Boolean> effOracle = simOracleStats;
		
		EquivalenceOracle<DFA<?,I>,I,Boolean> eqOracle
			= new PCTraceEQOracle<I>(model, alphabet, ceLength, seed);

		LearningAlgorithm<DFA<?,I>, I, Boolean> dfaLearner
			= learner.createLearner(alphabet, effOracle);
		
		dfaLearner.startLearning();
		
		
		DFA<?,I> hyp = dfaLearner.getHypothesisModel();
		long rounds = 0L;
		while(Automata.findSeparatingWord(model, hyp, alphabet) != null) {
			rounds++;
			DefaultQuery<I, Boolean> ce = eqOracle.findCounterExample(hyp, alphabet);
			
			while(dfaLearner.refineHypothesis(ce));
		}
		
		
		Result res = new Result(learner.getName());
		
		res.totalQueries = simOracleStats.getQueryCount();
		res.totalQueriesSymbols = simOracleStats.getSymbolCount();
		
		res.totalRounds = rounds;
		
		return res;
	}

}
