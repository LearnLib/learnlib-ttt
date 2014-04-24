package de.learnlib.algorithms.ttt.dfa;

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
