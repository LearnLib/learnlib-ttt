package de.learnlib.algorithms.ttt.dfa;

import java.io.Writer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.cache.PCTreeCacheCreator;
import de.learnlib.ttt.dfa.resourceprot.Action;
import de.learnlib.ttt.dfa.resourceprot.EQCreatorRP;
import de.learnlib.ttt.dfa.resourceprot.ResourceProtocol;

public class TTTDFATestRP {
	
	public static void main(String[] args) throws Exception {
		ResourceProtocol rp = new ResourceProtocol(4);
		
		DFA<?,Action> model = rp.getReferenceAutomaton();
		Alphabet<Action> alphabet = rp.getAlphabet();
		
		int n = model.size();
		int k = alphabet.size();
		
		System.err.println("n = " + n + ", k = " + k);
		
		
		Map<Action.Type, Double> actDist
			= new EnumMap<>(Action.Type.class);
			
		actDist.put(Action.Type.OPEN, 0.25);
		actDist.put(Action.Type.CLOSE, 0.2);
		
		actDist.put(Action.Type.CHMOD_RW, 0.15);
		actDist.put(Action.Type.CHMOD_RO, 0.15);
		
		actDist.put(Action.Type.READ, 1.0);
		actDist.put(Action.Type.WRITE, 0.05);
		
		TestRunner testRunner
		//	= new TestRunner(1, new EQCreatorFixed<>(ces), new PCTreeCacheCreator());
			= new TestRunner(1, new EQCreatorRP(1500, actDist), new PCTreeCacheCreator());
	
	Map<String,Map<String,StatisticalResult>> results = testRunner.runTests(Collections.singletonList(rp),
			LearnerCreators.LEARNERS);
	
	System.err.println("n = " + n + ", k = " + k);
	TestRunner.printResults(results, System.out);
	}
	
}
