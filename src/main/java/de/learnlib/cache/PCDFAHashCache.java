package de.learnlib.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;


public class PCDFAHashCache<I> implements MembershipOracle.DFAMembershipOracle<I> {
	
	private final MembershipOracle<I, Boolean> delegate;
	
	private final Map<Word<I>,Boolean> cache = new HashMap<>();

	
	public PCDFAHashCache(MembershipOracle<I, Boolean> delegate) {
		this.delegate = delegate;
	}
	
	public DFAEquivalenceOracle<I> createCacheConsistencyTest() {
		return new DFAEquivalenceOracle<I>() {
			@Override
			public DefaultQuery<I, Boolean> findCounterExample(
					DFA<?, I> hypothesis, Collection<? extends I> inputs) {
				for(Map.Entry<Word<I>,Boolean> e : cache.entrySet()) {
					Word<I> word = e.getKey();
					Boolean expect = e.getValue();
					if(hypothesis.accepts(word) != expect) {
						return new DefaultQuery<>(word, expect);
					}
				}
				
				return null;
			}
			
		};
	}


	@Override
	public void processQueries(Collection<? extends Query<I,Boolean>> queries) {
		List<DefaultQuery<I,Boolean>> delegateQueryList = Arrays.asList((DefaultQuery<I,Boolean>)null);
		
outer:	for(Query<I,Boolean> q : queries) {
			Word<I> word = q.getInput().flatten();
			
			Boolean res = cache.get(word);
			if(res != null) {
				q.answer(res);
				continue;
			}
			
			for(int i = word.length() - 1; i >= 0; i--) {
				Word<I> pref = word.prefix(i);
				Boolean result = cache.get(pref);
				if(result != null) {
					if(!result) {
						q.answer(result);
						continue outer;
					}
					else {
						break;
					}
				}
			}
			
			DefaultQuery<I, Boolean> delegateQuery = new DefaultQuery<>(word);
			delegateQueryList.set(0, delegateQuery);
			delegate.processQueries(delegateQueryList);;
			
			Boolean result = delegateQuery.getOutput();
			q.answer(result);
			
			cache.put(word, result);
			if(result) {
				for(int i = word.length() - 1; i >= 0; i--) {
					Word<I> pref = word.prefix(i);
					if(cache.put(pref, result) != null) {
						break;
					}
				}
			}
		}
		
	}
	
	

}
