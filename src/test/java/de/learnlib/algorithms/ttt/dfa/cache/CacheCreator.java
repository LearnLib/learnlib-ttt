package de.learnlib.algorithms.ttt.dfa.cache;

import net.automatalib.words.Alphabet;
import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.dfa.DFACacheOracle;

public interface CacheCreator {
	public <I> DFACacheOracle<I> createCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> delegate);
}
