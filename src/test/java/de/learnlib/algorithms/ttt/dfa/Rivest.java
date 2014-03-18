package de.learnlib.algorithms.ttt.dfa;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class Rivest {

	public static void main(String[] args) throws IOException{
		Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
		char a = 'a';
		char b = 'b';
		DFA<?,Character> dfa = AutomatonBuilders.newDFA(alphabet)
				.from("").on(a).to("a")
					.on(b).to("b")
				.from("a").on(a).to("aa")
				.from("aa").on(b).loop()
					.on(a).to("aaa")
				.from("aaa").on(a, b).loop()
				.from("b").on(a).to("ba")
				.from("ba").on(a).to("baa")
				.withAccepting("aaa", "baa")
				.withInitial("")
				.create();
		
		
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(dfa, alphabet, w);
		}
		
		
		dfa = AutomatonBuilders.newDFA(alphabet)
				.from("").on(a).loop()
					.on(b).to("b")
				.from("b")
					.on(a,b).to("")
				.withInitial("")
				.create();
		
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(dfa, alphabet, w);
		}
		
		dfa = AutomatonBuilders.newDFA(alphabet)
				.from("").on(a).to("a")
					.on(b).loop()
				.from("a").on(a).loop()
					.on(b).to("")
				.withInitial("")
				.create();
					
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(dfa, alphabet, w);
		}
	}

}
