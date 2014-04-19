package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;

public interface AccessSequenceProvider<I> {
	
	public Word<I> getAccessSequence();

}
