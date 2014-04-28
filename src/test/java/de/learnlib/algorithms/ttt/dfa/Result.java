package de.learnlib.algorithms.ttt.dfa;

import java.io.PrintStream;


public class Result {
	
	public final String name;

	public long uniqueQueries;
	public long uniqueQueriesSymbols;
	
	public long totalQueries;
	public long totalQueriesSymbols;
	
	public long totalRounds;
	
	
	public Result(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("%-20s | %7d (%9d) | %9d (%11d) | %4d",
				name, uniqueQueries, totalQueries, uniqueQueriesSymbols, totalQueriesSymbols, totalRounds);
	}

	public void printRaw(PrintStream ps) {
		synchronized(ps) {
			ps.printf("%7d %9d %9d %11d %4d\n", uniqueQueries, totalQueries, uniqueQueriesSymbols, totalQueriesSymbols, totalRounds);
			ps.flush();
		}
		
	}
	
}
