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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.dfa.cache.PCTreeCacheCreator;
import de.learnlib.algorithms.ttt.dfa.eq.EQCreatorPCTrace;

public class TTTDFATestRealistic {
	
	public static void main(String[] args) throws Exception {
		RealisticSystem sched4 = new RealisticSystem("sched4");
		
		DFA<?,Integer> model = sched4.getReferenceAutomaton();
		Alphabet<Integer> alphabet = sched4.getAlphabet();
		
		int n = model.size();
		int k = alphabet.size();
		
		
		for(int i = 500; i < 3000; i+=50) {
			System.err.println("i = " + i);
		FWTestRunner testRunner2
			= new FWTestRunner("sched4-growing/" + i, 1, new EQCreatorPCTrace(i, System.currentTimeMillis()), new PCTreeCacheCreator());


	testRunner2.runTests(Collections.singletonList(sched4),
		LearnerCreators.LEARNERS);
	
	testRunner2.shutdown();
		}
	}
	
}
