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
package de.learnlib.statistics;

import java.util.Collection;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.Query;

public class DFAStatisticsOracle<I> implements DFAMembershipOracle<I> {
	
	private long symbolCount = 0L;
	private long queryCount = 0L;
	private final MembershipOracle<I, Boolean> delegate;

	public DFAStatisticsOracle(MembershipOracle<I, Boolean> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
		for(Query<I,Boolean> q : queries) {
			symbolCount += q.getPrefix().length();
			symbolCount += q.getSuffix().length();
		}
		queryCount += queries.size();
		delegate.processQueries(queries);
	}

	public long getSymbolCount() {
		return symbolCount;
	}
	
	public long getQueryCount() {
		return queryCount;
	}
}
