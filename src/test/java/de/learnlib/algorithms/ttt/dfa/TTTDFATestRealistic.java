package de.learnlib.algorithms.ttt.dfa;

import java.util.Collections;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.cache.PCTreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorPCRandomWalk;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorPCTrace;

public class TTTDFATestRealistic {
	
	public static void main(String[] args) throws Exception {
		RealisticSystem sched4 = new RealisticSystem("sched4");
		
		DFA<?,Integer> model = sched4.getReferenceAutomaton();
		Alphabet<Integer> alphabet = sched4.getAlphabet();
		
		int n = model.size();
		int k = alphabet.size();
		
		
		
		FWTestRunner testRunner
			= new FWTestRunner("sched4-sampling", 100, new EQCreatorPCRandomWalk(new Random(), 0.5f, 2f), new PCTreeCacheCreator());
	
		testRunner.runTests(Collections.singletonList(sched4),
			LearnerCreators.LEARNERS);
		
		testRunner.shutdown();
		
		FWTestRunner testRunner2
			= new FWTestRunner("sched4-fixed", 100, new EQCreatorPCTrace(500, 1L), new PCTreeCacheCreator());

	testRunner2.runTests(Collections.singletonList(sched4),
		LearnerCreators.LEARNERS);
	
	testRunner2.shutdown();
	}
	
}
