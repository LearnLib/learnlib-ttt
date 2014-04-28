package de.learnlib.algorithms.ttt.dfa.cache;

import java.util.Collection;

import net.automatalib.words.Alphabet;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.cache.dfa.DFACacheOracle;

public class NullCacheCreator implements CacheCreator {

	private static class NullCacheOracle<I> extends DFACacheOracle<I> {

		private final MembershipOracle<I, Boolean> delegate;
		public NullCacheOracle(Alphabet<I> alphabet,
				MembershipOracle<I, Boolean> delegate) {
			super(alphabet, delegate);
			this.delegate = delegate;
		}
		@Override
		public void processQueries(
				Collection<? extends Query<I, Boolean>> queries) {
			delegate.processQueries(queries);
		}		
	}
	
	
	@Override
	public <I> DFACacheOracle<I> createCache(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> delegate) {
		return new NullCacheOracle<>(alphabet, delegate);
	}

}
