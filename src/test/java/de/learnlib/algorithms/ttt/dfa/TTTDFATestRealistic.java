package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.algorithms.ttt.dfa.cache.PCTreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorPCTrace;
import de.learnlib.oracles.DefaultQuery;

public class TTTDFATestRealistic {
	
	
	private static <S,I> List<Word<I>> generateTraces(DFA<S,I> model, Collection<? extends I> inputs,
			int minLength, int maxLength, int numTraces, Random random) {
		List<Word<I>> traces = new ArrayList<>(numTraces);
		
		for(int i = 0; i < numTraces; i++) {
			
			int length = minLength + random.nextInt(maxLength - minLength + 1);
			
			
			S curr = model.getInitialState();
			
			WordBuilder<I> trace = new WordBuilder<>();
			for(int j = 0; j < length && model.isAccepting(curr); j++) {
				List<I> candidates = new ArrayList<>();
				
				for(I sym : inputs) {
					S succ = model.getSuccessor(curr, sym);
					if(model.isAccepting(succ)) {
						candidates.add(sym);
					}
				}
				
				if(candidates.isEmpty()) {
					candidates.addAll(inputs);
				}
				
				I sym = candidates.get(random.nextInt(candidates.size()));
				trace.add(sym);
				S succ = model.getSuccessor(curr, sym);
				curr = succ;
			}
			
			traces.add(trace.toWord());
		}
		
		return traces;
	}
	
	public static <I> List<DefaultQuery<I,Boolean>> toQueries(List<Word<I>> traces) {
		List<DefaultQuery<I,Boolean>> queries = new ArrayList<>(traces.size());
		
		for(Word<I> trace : traces) {
			DefaultQuery<I, Boolean> query = new DefaultQuery<>(trace, true);
			queries.add(query);
		}
		
		return queries;
	}
	
	public static void main(String[] args) throws Exception {
		RealisticSystem sched4 = new RealisticSystem("sched4");
		
		DFA<?,Integer> model = sched4.getReferenceAutomaton();
		Alphabet<Integer> alphabet = sched4.getAlphabet();
		
		int n = model.size();
		int k = alphabet.size();
		
		
		
		FWTestRunner testRunner
		//	= new TestRunner(1, new EQCreatorFixed<>(ces), new PCTreeCacheCreator());
			= new FWTestRunner(10, new EQCreatorPCTrace(100, 0L), new PCTreeCacheCreator());
	
		testRunner.runTests(Collections.singletonList(sched4),
			LearnerCreators.LEARNERS);
	}
	
}
