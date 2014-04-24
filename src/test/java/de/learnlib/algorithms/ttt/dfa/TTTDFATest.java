package de.learnlib.algorithms.ttt.dfa;

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.dfa.ExampleKeylock;

public class TTTDFATest {
	
	public static void main(String[] args) throws Exception {
		Alphabet<Integer> alphabet = Alphabets.integers(0, 9);
		DFA<?,Integer> model = RandomAutomata.randomDFA(new Random(), 200, alphabet);
		
		ExampleKeylock keylock = ExampleKeylock.createExample(20, true, 9);
		
		alphabet = keylock.getAlphabet();
		model = keylock.getReferenceAutomaton();
		
		Word<Integer>[] ceWords = findKeylockCEWords(alphabet, model, TestRunner.NUM_TESTS);
		
		//Result[] results = TestRunner.runTests(alphabet, model, LearnerCreators.LEARNERS);
		//StatisticalResult[] results = TestRunner.runTestsStatistical(alphabet, model, LearnerCreators.LEARNERS);
		StatisticalResult[] results = TestRunner.runTestsStatisticalSingle(alphabet, model, ceWords, LearnerCreators.LEARNERS);
		
		printObjects(results);
	}
	
	private static final void printObjects(StatisticalResult[] results) {
		for(StatisticalResult res : results) {
			System.out.println(res.toLatexStringShort());
		}
	}
	
	public static <S,I> Word<I> findKeylockCE(Alphabet<I> alphabet, DFA<S,I> model, Random r) {
		S curr = model.getInitialState();
		int alphabetSize = alphabet.size();
		
		WordBuilder<I> wb = new WordBuilder<>();
		
		while(!model.isAccepting(curr)) {
			I sym = alphabet.getSymbol(r.nextInt(alphabetSize));
			wb.append(sym);
			curr = model.getSuccessor(curr, sym);
		}
		
		return wb.toWord();
	}
	
	public static <I> Word<I>[] findKeylockCEWords(Alphabet<I> alphabet, DFA<?,I> model, int num) {
		Random r = new Random();
		Word<I>[] result = new Word[num];
		
		for(int i = 0; i < result.length; i++) {
			result[i] = findKeylockCE(alphabet, model, r);
		}
		
		return result;
	}

}
