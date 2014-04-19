package de.learnlib.algorithms.ttt.dfa;

import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.dfa.DFACaches;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.eqtests.basic.RandomWordsEQOracle.DFARandomWordsEQOracle;
import de.learnlib.oracles.CounterOracle.DFACounterOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

public class TTTDFATest {
	
	public static void main(String[] args) throws Exception {
		System.in.read();
		Alphabet<Integer> alphabet = Alphabets.integers(0, 4);
		CompactDFA<Integer> model = RandomAutomata.randomDFA(new Random(1), 1000, alphabet);
//		
//		try(Writer w = DOT.createDotWriter(true)) {
//			GraphDOT.write(model, alphabet, w);
//		}
		
		DFASimulatorOracle<Integer> simOracle = new DFASimulatorOracle<>(model);
		MembershipOracle<Integer,Boolean> llOracle = DFACaches.createDAGCache(alphabet, simOracle);
		
		
		
		SymbolCounterDFAOracle<Integer> scOracle = new SymbolCounterDFAOracle<>(llOracle);
		DFACounterOracle<Integer> mcOracle = new DFACounterOracle<>(scOracle, "#MQs");
		MembershipOracle<Integer, Boolean> oracle = mcOracle;
		LearningAlgorithm.DFALearner<Integer> tttLearner;
//		tttLearner = new TTTLearnerDFA<>(alphabet, oracle, LocalSuffixFinders.RIVEST_SCHAPIRE);
//		tttLearner = new RivestSchapireDFA<>(alphabet, oracle);
//		tttLearner = new ExtensibleLStarDFABuilder<Integer>()
//				.withAlphabet(alphabet)
//				.withOracle(oracle)
//				.create();
		tttLearner = new DTLearnerDFA<>(alphabet, oracle, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
//		
//		tttLearner = new KearnsVaziraniDFA<>(alphabet, oracle, true);
		tttLearner.startLearning();
		
		//DFASimulatorEQOracle<Integer> eqOracle = new DFASimulatorEQOracle<>(model);
		DFARandomWordsEQOracle<Integer> eqOracle = new DFARandomWordsEQOracle<>(oracle, 5, 100, 10000, new Random());
		
		DefaultQuery<Integer, Boolean> ce;
		
		while((ce = eqOracle.findCounterExample(tttLearner.getHypothesisModel(), alphabet)) != null) {
//			try(Writer w = DOT.createDotWriter(false)) {
//				GraphDOT.write(tttLearner.getHypothesisModel().graphView(), w);
//			}
//			try(Writer w = DOT.createDotWriter(true)) {
//				GraphDOT.write(tttLearner.getDiscriminationTree().graphView(), w);
//			}
			tttLearner.refineHypothesis(ce);
			
		}
		
		System.err.println("Final hypothesis correct? " + (tttLearner.getHypothesisModel().size() == model.size()));
		
		System.err.println("Required " + scOracle.getSymbolCount() + " symbols");
		System.err.println("Required " + mcOracle.getCount() + " queries");
		
	}

}
