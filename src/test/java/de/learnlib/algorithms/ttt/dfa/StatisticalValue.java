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

public class StatisticalValue {
	public long min;
	public long max;
	public long avg;
	public long sd;

	
	public StatisticalValue(long... singleValues) {
		long total = 0L;
		min = Long.MAX_VALUE;
		max = Long.MIN_VALUE;
		
		for(long val : singleValues) {
			total += val;
			if(val < min) {
				min = val;
			}
			if(val > max) {
				max = val;
			}
		}
		
		avg = total / singleValues.length;
		
		long variance = 0L;
		
		for(long val : singleValues) {
			variance += sqr(val - avg);
		}
		
		variance /= singleValues.length;
		
		sd = sqrt(variance);
	}
	
	private static final long sqr(long value) {
		return value * value;
	}
	
	private static final long sqrt(long value) {
		return (long)Math.sqrt((double)value);
	}
}
