package de.learnlib.eq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class DFARandomSampleEQOracle<I> implements DFAEquivalenceOracle<I> {
	
	private final MembershipOracle<I, Boolean> mqOracle;
	private final DFA<?,I> target;
	
	
	private final Random random = new Random();
	
	public DFARandomSampleEQOracle(MembershipOracle<I, Boolean> mqOracle, DFA<?,I> target) {
		this.mqOracle = mqOracle;
		this.target = target;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		List<? extends I> inputsRA = CollectionsUtil.randomAccessList(inputs);
		
		if(Automata.findSeparatingWord(target, hypothesis, inputsRA) == null) {
			return null;
		}
				
		int numInputs = inputsRA.size();
		int minLength = hypothesis.size() / 2;
		int maxLength = hypothesis.size() * 2;
		
		WordBuilder<I> wb = new WordBuilder<>();
		List<DefaultQuery<I, Boolean>> singleQueryList = Arrays.asList((DefaultQuery<I,Boolean>)null);
		
		while(Automata.findSeparatingWord(target, hypothesis, inputs) != null) {
			int length = minLength + random.nextInt(maxLength - minLength + 1);
			
			for(int i = 0; i < length; i++) {
				wb.append(inputsRA.get(random.nextInt(numInputs)));
			}
			
			Word<I> input = wb.toWord();
			DefaultQuery<I, Boolean> qry = new DefaultQuery<>(input);
			singleQueryList.set(0, qry);
			
			mqOracle.processQueries(singleQueryList);
			
			Boolean out = qry.getOutput();
			if(out.booleanValue() != hypothesis.accepts(input)) {
				return qry;
			}
			
			wb.clear();
		}
		return null;
	}

}
