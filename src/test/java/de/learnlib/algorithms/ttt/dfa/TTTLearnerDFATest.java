package de.learnlib.algorithms.ttt.dfa;

import java.io.InputStream;
import java.util.Random;
import java.util.logging.LogManager;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle.DFASimulatorEQOracle;
import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class TTTLearnerDFATest {

	public static void main(String[] args) throws Exception {
		System.in.read();
		try(InputStream is = TTTLearnerDFATest.class.getResourceAsStream("log.properties")) {
			LogManager.getLogManager().readConfiguration(is);
		}
		Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
		DFA<?,Character> dfa = RandomAutomata.randomDFA(new Random(), 1000, alphabet);
		
		
		int keylockLength = 5;
		CompactDFA<Character> keylockDfa = new CompactDFA<>(alphabet);
		int init = keylockDfa.addInitialState(false);
		int curr = init;
		
		for(int i = 0; i < keylockLength; i++) {
			int next = keylockDfa.addIntState(false);
			for(Character c : alphabet) {
				keylockDfa.setTransition(curr, c, next);
			}
			curr = next;
		}
		
		int lock = keylockDfa.addState(true);
		for(Character c : alphabet) {
			keylockDfa.setTransition(curr, c, lock);
			keylockDfa.setTransition(lock, c, init);
		}
		
		//dfa = keylockDfa;
		
		// try(Writer w = DOT.createDotWriter(false)) {
		// 	GraphDOT.write(dfa, alphabet, w);
		// }
		System.err.println("DFA has " + dfa.size() + " states");
		DFAMembershipOracle<Character> oracle = new DFASimulatorOracle<>(dfa);
		CounterOracle<Character, Boolean> tttCounterOracle = new CounterOracle<>(oracle, "#MQs (TTT)");
		
		MembershipOracle<Character,Boolean> effectiveTTTOracle = tttCounterOracle;
			// DFACaches.createTreeCache(alphabet, tttCounterOracle);
		TTTLearnerDFA<Character> learner = new TTTLearnerDFA<>(alphabet, effectiveTTTOracle);
		
		
		//showHypothesis(learner.getHypothesisModel(), alphabet);
		
		long seed = System.nanoTime();
		
		DFAEquivalenceOracle<Character> eqOracle
			= new DFASimulatorEQOracle<>(dfa);
			//= new DFARandomWordsEQOracle<>(effectiveTTTOracle, 1, 30000, 10000, new Random(seed));
		
		long start = System.currentTimeMillis();
		long end = 0L;

		learner.startLearning();
		
		int numCes = 0;
		for(;;) {
			DefaultQuery<Character, Boolean> ce = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet);
			
			if(ce == null) {
				end = System.currentTimeMillis();
				Word<Character> sepWord = Automata.findSeparatingWord(learner.getHypothesisModel(), dfa, alphabet);
				System.err.println("Sepword: " + sepWord);
				break;
			}
			learner.refineHypothesis(ce);
			
			numCes++;
			
			assert numCes < dfa.size();
		}
		long duration = end - start;
		System.err.println("Learning using TTT took " + duration + "ms");
		System.err.println(tttCounterOracle.getCounter().getDetails());
		System.err.println("Required " + numCes + " counterexamples");
		
		CounterOracle<Character,Boolean> lstarCounterOracle = new CounterOracle<>(oracle, "#MQs (L*)");
		MembershipOracle<Character,Boolean> effectiveLstarOracle = lstarCounterOracle;
			// DFACaches.createTreeCache(alphabet, lstarCounterOracle);
		
		DFALearner<Character> lstarLearner = new ExtensibleLStarDFABuilder<Character>()
			.withAlphabet(alphabet)
			.withOracle(effectiveLstarOracle)
			.withCexHandler(ObservationTableCEXHandlers.RIVEST_SCHAPIRE)
			.create();
		
		//eqOracle
		//= new DFARandomWordsEQOracle<>(effectiveLstarOracle, 1, 10000, 10000, new Random(seed));
		
		start = System.currentTimeMillis();
		end = 0L;

		lstarLearner.startLearning();
		
		numCes = 0;
		for(;;) {
			DefaultQuery<Character, Boolean> ce = eqOracle.findCounterExample(lstarLearner.getHypothesisModel(), alphabet);
			
			if(ce == null) {
				end = System.currentTimeMillis();
				Word<Character> sepWord = Automata.findSeparatingWord(lstarLearner.getHypothesisModel(), dfa, alphabet);
				System.err.println("Sepword: " + sepWord);
				break;
			}
			while(lstarLearner.refineHypothesis(ce));
			numCes++;
			
			assert numCes < dfa.size();
		}
		duration = end - start;
		System.err.println("Learning using L* took " + duration + "ms");
		System.err.println(lstarCounterOracle.getCounter().getDetails());
		System.err.println("Required " + numCes + " counterexamples");
	}
	
	
	

}
