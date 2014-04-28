package de.learnlib.ttt.algorithms.dfa.examples;

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.LearningExample.DFALearningExample;

public class RandomDFAExample implements DFALearningExample<Integer> {
	
	private final CompactDFA<Integer> model;
	
	public RandomDFAExample(int alphabetSize, int modelSize, Random random) {
		Alphabet<Integer> alphabet = Alphabets.integers(0, alphabetSize - 1);
		this.model = RandomAutomata.randomDFA(random, modelSize, alphabet);
	}

	@Override
	public DFA<?, Integer> getReferenceAutomaton() {
		return model;
	}

	@Override
	public Alphabet<Integer> getAlphabet() {
		return model.getInputAlphabet();
	}
	
	public String toString() {
		return "Random-" + model.getInputAlphabet().size() + "-" + model.size();
	}

}
