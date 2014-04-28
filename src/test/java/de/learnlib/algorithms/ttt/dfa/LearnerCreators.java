package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFABuilder;
import de.learnlib.algorithms.kv.dfa.KearnsVaziraniDFABuilder;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
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
			return "L*";
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
	
	public static final LearnerCreator[] LEARNERS = {
		DT,
		TTT,
		// LSTAR,
		// SUFFIX1BY1,
		// RS,
		KV,
	};
	
	private LearnerCreators() {
		throw new AssertionError();
	}

}
