package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;

public interface LearnerCreator {
	public <I> LearningAlgorithm<DFA<?,I>, I, Boolean>
		createLearner(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle);
	
	public String getName();
}
