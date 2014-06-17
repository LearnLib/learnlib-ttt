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

import java.util.Collections;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.cache.NullCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorPCTrace;
import de.learnlib.ttt.dfa.resourceprot.Action;
import de.learnlib.ttt.dfa.resourceprot.EQCreatorRP;
import de.learnlib.ttt.dfa.resourceprot.ResourceProtocol;

public class TTTDFATestRP {
	
	public static void main(String[] args) throws Exception {
		ResourceProtocol rp = new ResourceProtocol(3);
		
		DFA<?,Action> model = rp.getReferenceAutomaton();
		Alphabet<Action> alphabet = rp.getAlphabet();
		
		int n = model.size();
		int k = alphabet.size();
		
		System.err.println("n = " + n + ", k = " + k);
		
		
		/*
		Map<Action.Type, Double> actDist
			= new EnumMap<>(Action.Type.class);
			
		actDist.put(Action.Type.OPEN, 0.5);
		actDist.put(Action.Type.CLOSE, 0.5);
		
		actDist.put(Action.Type.CHMOD_RW, 0.05);
		actDist.put(Action.Type.CHMOD_RO, 0.05);
		
		actDist.put(Action.Type.READ, 1.0);
		actDist.put(Action.Type.WRITE, 0.05);
		*/
		TestRunner testRunner
		//	= new TestRunner(1, new EQCreatorFixed<>(ces), new PCTreeCacheCreator());
			= new TestRunner(1, new EQCreatorPCTrace(1000, 1L), new NullCacheCreator());
	
	Map<String,Map<String,StatisticalResult>> results = testRunner.runTests(Collections.singletonList(rp),
			LearnerCreators.getLearners("TTT", "KV", "DT"));//, "KV", "DT"));
	
	System.err.println("n = " + n + ", k = " + k);
	TestRunner.printResults(results, System.out);
	}
	
}
