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
