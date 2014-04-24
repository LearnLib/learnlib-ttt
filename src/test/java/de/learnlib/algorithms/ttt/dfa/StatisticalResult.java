package de.learnlib.algorithms.ttt.dfa;

public class StatisticalResult {
	
	public final String name;
	
	public StatisticalValue uniqueQueries;
	public StatisticalValue uniqueQueriesSymbols;
	
	public StatisticalValue totalQueries;
	public StatisticalValue totalQueriesSymbols;
	
	public StatisticalValue totalRounds;

	public StatisticalResult(Result... results) {
		this.name = results[0].name;
		
		
		int num = results.length;
		
		long[] values = new long[num];
		
		for(int i = 0; i < num; i++) {
			values[i] = results[i].uniqueQueries;
		}
		this.uniqueQueries = new StatisticalValue(values);
		
		for(int i = 0; i < num; i++) {
			values[i] = results[i].uniqueQueriesSymbols;
		}
		this.uniqueQueriesSymbols = new StatisticalValue(values);
		
		for(int i = 0; i < num; i++) {
			values[i] = results[i].totalQueries;
		}
		this.totalQueries = new StatisticalValue(values);
		
		for(int i = 0; i < num; i++) {
			values[i] = results[i].totalQueriesSymbols;
		}
		this.totalQueriesSymbols = new StatisticalValue(values);
		
		for(int i = 0; i < num; i++) {
			values[i] = results[i].totalRounds;
		}
		this.totalRounds = new StatisticalValue(values);
	}
	
	@Override
	public String toString() {
		return String.format("%3s | %8d/%8d/%8d (%7d) | %10d/%10d/%10d (%9d) | %3d/%3d/%3d (%2d)",
				name,
				uniqueQueries.min, uniqueQueries.max, uniqueQueries.avg, uniqueQueries.sd,
				uniqueQueriesSymbols.min, uniqueQueriesSymbols.max, uniqueQueriesSymbols.avg, uniqueQueriesSymbols.sd,
				totalRounds.min, totalRounds.max, totalRounds.avg, totalRounds.sd);
	}
	
	public String toLatexString() {
		return String.format("%s & %d & %d & %d & %d & %d & %d & %d & %d & %d & %d & %d & %d",
				name,
				uniqueQueries.avg, uniqueQueries.sd, uniqueQueries.min, uniqueQueries.max,
				uniqueQueriesSymbols.avg, uniqueQueriesSymbols.sd, uniqueQueriesSymbols.min, uniqueQueriesSymbols.max,
				totalRounds.avg, totalRounds.sd, totalRounds.min, totalRounds.max);
	}
	
	public String toLatexStringShort() {
		return String.format("%s & %d & %d & %d & %d & %d & %d\\\\",
				name,
				uniqueQueries.avg, (uniqueQueries.sd*100L)/uniqueQueries.avg, 
				uniqueQueriesSymbols.avg, (uniqueQueriesSymbols.sd*100L)/uniqueQueriesSymbols.avg, 
				totalRounds.avg, totalRounds.sd);
	}
	
}
