package de.learnlib.algorithms.ttt.dfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import de.learnlib.importers.aut.AUTImporter;

public class TTTDFATestRealistic {
	
	public static final String[] MODEL_NAMES = { //"sched4", "peterson2",
		"sched5", "pots2" };
	
	public static void main(String[] args) throws Exception {
		System.in.read();
		
		Map<String,StatisticalResult[]> results = new HashMap<>();
		
		TestRunner testRunner = new TestRunner();
		
		PrintStream ps = new PrintStream(new FileOutputStream(new File("results.txt")));
		for(String modelName : MODEL_NAMES) {
			String resourceName = "/" + modelName + ".dfa.gz";
			
			CompactDFA<Integer> model = AUTImporter.read(TTTDFATestRealistic.class.getResourceAsStream(resourceName));
			Alphabet<Integer> alphabet = model.getInputAlphabet();
			
			System.err.println("Learning model " + modelName);
			System.err.println("Model size: " + model.size() + " / " + alphabet.size());
			
			StatisticalResult[] testResults
				= testRunner.runTestsStatistical(alphabet, model, LearnerCreators.LEARNERS);
			printObjects(testResults, System.out);
			printObjects(testResults, ps);
			ps.flush();
			
			results.put(modelName, testResults);
		}
		ps.close();
		
		testRunner.shutdown();
		
		for(Map.Entry<String,StatisticalResult[]> result : results.entrySet()) {
			System.out.println("Results for " + result.getKey());
			System.out.println("==================================");
			printObjects(result.getValue(), System.out);
		}
	}
	
	private static final void printObjects(StatisticalResult[] results, PrintStream ps) {
		for(StatisticalResult res : results) {
			ps.println(res.toString());
		}
		for(StatisticalResult res : results) {
			ps.println(res.toLatexStringShort());
		}
	}

}
