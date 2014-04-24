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
