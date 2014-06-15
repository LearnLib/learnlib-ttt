/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib-TTT, https://github.com/LearnLib/learnlib-ttt/
 * 
 * LearnLib-TTT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LearnLib-TTT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LearnLib-TTT.  If not, see <http://www.gnu.org/licenses/>.
 */
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
