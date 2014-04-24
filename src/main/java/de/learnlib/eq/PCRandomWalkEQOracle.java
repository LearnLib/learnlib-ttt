package de.learnlib.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class PCRandomWalkEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final MembershipOracle<I, Boolean> mqOracle;
	
	private final int maxTests;
	private final int minLength;
	private final int maxLength;
	
	private final Random random = new Random();
	
	public PCRandomWalkEQOracle(MembershipOracle<I, Boolean> mqOracle, int minLength, int maxLength, int maxTests) {
		this.mqOracle = mqOracle;
		this.maxTests = maxTests;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(hypothesis, inputs);
	}
	
	private <S> DefaultQuery<I, Boolean> doFindCounterExample(DFA<S, I> hypothesis,
			Collection<? extends I> inputs) {

		List<I> candidates = new ArrayList<>();
		
		for (int i = 0; i < maxTests; ++i) {
			S curr = hypothesis.getInitialState();
			
			int length = minLength + random.nextInt((maxLength - minLength) + 1);

			WordBuilder<I> testtrace = new WordBuilder<>(length);
			
			for(int j = 0; j < length; j++) {
				for(I sym : inputs) {
					S succ = hypothesis.getSuccessor(curr, sym);
					if(hypothesis.isAccepting(succ)) {
						candidates.add(sym);
					}
				}
				
				if(candidates.isEmpty()) {
					candidates.addAll(inputs);
				}
				int symIdx = random.nextInt(candidates.size());
				I sym = candidates.get(symIdx);
				candidates.clear();
				
				testtrace.append(sym);
				
				curr = hypothesis.getSuccessor(curr, sym);
			}
			
			final DefaultQuery<I, Boolean> query = new DefaultQuery<>(testtrace.toWord());
			
			// query oracle
			mqOracle.processQueries(Collections.singleton(query));
			
			Boolean oracleoutput = query.getOutput();

			// trace hypothesis
			Boolean hypOutput = hypothesis.computeOutput(testtrace);

			// compare output of hypothesis and oracle
			if (!Objects.equals(oracleoutput, hypOutput)) {
				return query;
			}
			else if(!oracleoutput) {
			//	System.err.println("WTF?");
			}
		}
		
		return null;
	}

}
