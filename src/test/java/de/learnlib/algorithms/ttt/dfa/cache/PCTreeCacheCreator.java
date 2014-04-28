package de.learnlib.algorithms.ttt.dfa.cache;

import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.words.Alphabet;
import de.learnlib.api.MembershipOracle;
import de.learnlib.cache.dfa.DFACacheOracle;

public class PCTreeCacheCreator implements CacheCreator {

	@Override
	public <I> DFACacheOracle<I> createCache(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> delegate) {
		return new DFACacheOracle<>(new IncrementalPCDFATreeBuilder<>(alphabet), delegate);
	}

}
