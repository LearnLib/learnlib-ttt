package de.learnlib.algorithms.ttt.dfa;

import java.util.Objects;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.oracles.MQUtil;

public class IsbernerSuffixFinder implements
		LocalSuffixFinder<Object, Object> {

	@Override
	public <I extends Object, O extends Object> int findSuffixIndex(
			Query<I, O> ceQuery, AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I, O> hypOutput, MembershipOracle<I, O> oracle) {

		Word<I> queryWord = ceQuery.getInput();
		int queryLen = queryWord.length();
		
		Word<I> prefix = ceQuery.getPrefix();
		int prefixLen = prefix.length();
		
		
		int low = asTransformer.isAccessSequence(prefix) ? prefixLen : prefixLen-1;
		
		int high = queryLen;
		
		int ofs = 1;
		
		while(high - ofs > low) {
			int next = high - ofs;
			ofs *= 2;
			
			Word<I> nextPrefix = queryWord.prefix(next);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			
			Word<I> nextSuffix = queryWord.subWord(next);
			
			O hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			O ceOut = MQUtil.output(oracle, as, nextSuffix);
			
			if(!Objects.equals(hypOut, ceOut)) {
				low = next;
				break;
			}
			else
				high = next;
		}
		
		while((high - low) > 1) {
			int mid = low + (high - low + 1)/2;
			
			
			Word<I> nextPrefix = queryWord.prefix(mid);
			Word<I> as = asTransformer.transformAccessSequence(nextPrefix);
			
			Word<I> nextSuffix = queryWord.subWord(mid);
			
			O hypOut = hypOutput.computeSuffixOutput(as, nextSuffix);
			O ceOut = MQUtil.output(oracle, as, nextSuffix);
			
			if(!Objects.equals(hypOut, ceOut))
				low = mid;
			else
				high = mid;
		}
		
		// FIXME: No check if actually found CE
		return low+1;
	}

}
