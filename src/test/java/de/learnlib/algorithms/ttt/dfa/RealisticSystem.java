package de.learnlib.algorithms.ttt.dfa;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.importers.aut.AUTImporter;

public class RealisticSystem implements DFALearningExample<Integer> {
	
	private final String name;
	private final CompactDFA<Integer> model;
	
	public RealisticSystem(String name) throws IOException {
		this.name = name;
		try(InputStream is = RealisticSystem.class.getResourceAsStream("/" + name + ".dfa.gz")) {
			this.model = AUTImporter.read(is);
		}
	}

	@Override
	public DFA<?, Integer> getReferenceAutomaton() {
		return model;
	}

	@Override
	public Alphabet<Integer> getAlphabet() {
		return model.getInputAlphabet();
	}
	
	@Override
	public String toString() {
		return name;
	}

}
