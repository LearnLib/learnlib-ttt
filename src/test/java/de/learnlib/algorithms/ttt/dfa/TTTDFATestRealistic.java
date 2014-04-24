package de.learnlib.algorithms.ttt.dfa;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import de.learnlib.importers.aut.AUTImporter;

public class TTTDFATestRealistic {
	
	public static final String[] MODEL_NAMES = { "peterson2", "pots2", "peterson3" };
	public static void main(String[] args) throws Exception {
		
		Map<String,StatisticalResult[]> results = new HashMap<>();
		for(String modelName : MODEL_NAMES) {
			String resourceName = "/" + modelName + ".dfa.gz";
			
			CompactDFA<Integer> model = AUTImporter.read(TTTDFATestRealistic.class.getResourceAsStream(resourceName));
			Alphabet<Integer> alphabet = model.getInputAlphabet();
			
			StatisticalResult[] testResults
				= TestRunner.runTestsStatistical(alphabet, model, LearnerCreators.LEARNERS);
			printObjects(testResults);
			
			results.put(modelName, testResults);
		}
		
		
		for(Map.Entry<String,StatisticalResult[]> result : results.entrySet()) {
			System.out.println("Results for " + result.getKey());
			System.out.println("==================================");
			printObjects(result.getValue());
		}
	}
	
	private static final void printObjects(StatisticalResult[] results) {
		for(StatisticalResult res : results) {
			System.out.println(res.toString());
		}
		for(StatisticalResult res : results) {
			System.out.println(res.toLatexStringShort());
		}
	}

}
