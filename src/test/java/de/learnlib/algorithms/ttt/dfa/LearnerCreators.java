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

import java.util.HashMap;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFABuilder;
import de.learnlib.algorithms.kv.dfa.KearnsVaziraniDFABuilder;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.algorithms.nlstar.NLStarLearner;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinders;





public class LearnerCreators {
	
	public static final LearnerCreator LSTAR = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new ExtensibleLStarDFABuilder<I>()
					.withAlphabet(alphabet)
					.withOracle(oracle)
					.create();
		}
		@Override
		public String getName() {
			return "LStar";
		}
	};
	
	public static final LearnerCreator SUFFIX1BY1 = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new ExtensibleLStarDFABuilder<I>()
					.withAlphabet(alphabet)
					.withOracle(oracle)
					.withCexHandler(ObservationTableCEXHandlers.SUFFIX1BY1)
					.create();
		}
		@Override
		public String getName() {
			return "Suffix1by1";
		}
	};
	
	public static final LearnerCreator KV = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new KearnsVaziraniDFABuilder<I>()
					.withAlphabet(alphabet)
					.withOracle(oracle)
					.create();
		}
		@Override
		public String getName() {
			return "KV";
		}
	};
	
	public static final LearnerCreator RS = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new RivestSchapireDFA<>(alphabet, oracle);
		}
		@Override
		public String getName() {
			return "RS";
		}
	};
	
	public static final LearnerCreator DT = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new DTLearnerDFABuilder<I>()
					.withAlphabet(alphabet)
					.withOracle(oracle)
					.withSuffixFinder(LocalSuffixFinders.RIVEST_SCHAPIRE)
					.create();
		}
		@Override
		public String getName() {
			return "DT";
		}
	};
	
	public static final LearnerCreator TTT = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new TTTLearnerDFA<>(alphabet, oracle, LocalSuffixFinders.RIVEST_SCHAPIRE);
		}
		@Override
		public String getName() {
			return "TTT";
		}
	};
	
	public static final LearnerCreator NLSTAR = new LearnerCreator() {
		@Override
		public <I> LearningAlgorithm<DFA<?, I>, I, Boolean> createLearner(
				Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
			return new NLStarLearner<I>(alphabet, oracle).asDFALearner();
		}
		@Override
		public String getName() {
			return "NLStar";
		}
	};
	
	public static final LearnerCreator[] LEARNERS = {
		DT,
		TTT,
		LSTAR,
		SUFFIX1BY1,
		RS,
		KV,
		NLSTAR,
	};
	
	private static final Map<String,LearnerCreator> byNameMap;
	
	static {
		byNameMap = new HashMap<>();
		for(LearnerCreator lc : LEARNERS) {
			byNameMap.put(lc.getName(), lc);
		}
	}
	
	public static LearnerCreator byName(String name) {
		LearnerCreator lc = byNameMap.get(name);
		if(lc == null) {
			throw new IllegalArgumentException();
		}
		return lc;
	}
	
	public static LearnerCreator[] getLearners(String... names) {
		LearnerCreator[] lcs = new LearnerCreator[names.length];
		for(int i = 0; i < names.length; i++) {
			lcs[i] = byName(names[i]);
		}
		return lcs;
	}
	
	private LearnerCreators() {
		throw new AssertionError();
	}

}
