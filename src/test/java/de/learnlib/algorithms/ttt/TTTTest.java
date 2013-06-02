package de.learnlib.algorithms.ttt;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;

public class TTTTest {

	public static void main(String[] args) throws IOException {
		Alphabet<Symbol> alphabet = ExampleAngluin.getAlphabet();
		DFA<?,Symbol> dfa = ExampleAngluin.getInstance();
		
		SimulatorOracle<Symbol, Boolean> mqOracle = new SimulatorOracle<>(dfa);
		
		
		TTTLearnerDFA<Symbol> learner = new TTTLearnerDFA<>(alphabet, mqOracle);
		
		learner.startLearning();
		
		Writer w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getHypothesisTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getDiscriminationTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getSuffixTree(), w);
		w.close();
		
		learner.refineHypothesis(new DefaultQuery<Symbol,Boolean>(Word.fromLetter(ExampleAngluin.IN_0), Boolean.FALSE));
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getHypothesisTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getDiscriminationTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getSuffixTree(), w);
		w.close();
		
		learner.refineHypothesis(new DefaultQuery<Symbol,Boolean>(Word.fromSymbols(ExampleAngluin.IN_1, ExampleAngluin.IN_1), Boolean.FALSE));
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getHypothesisTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getDiscriminationTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getSuffixTree(), w);
		w.close();
		
		learner.refineHypothesis(new DefaultQuery<Symbol,Boolean>(Word.fromSymbols(ExampleAngluin.IN_1, ExampleAngluin.IN_0, ExampleAngluin.IN_1, ExampleAngluin.IN_0), Boolean.FALSE));
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getHypothesisTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getDiscriminationTree(), w);
		w.close();
		
		w = DOT.createDotWriter(true);
		GraphDOT.write(learner.getSuffixTree(), w);
		w.close();
	}

}
