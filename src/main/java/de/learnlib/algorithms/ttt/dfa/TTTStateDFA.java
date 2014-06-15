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

import net.automatalib.words.Word;

/**
 * A state in a {@link TTTHypothesisDFA}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol
 */
public class TTTStateDFA<I> implements AccessSequenceProvider<I> {
	
	final int id;
	final boolean accepting;
	
	final TTTTransitionDFA<I>[] transitions;
	final TTTTransitionDFA<I> parentTransition;
	
	DTNode<I> dtLeaf;

	@SuppressWarnings("unchecked")
	public TTTStateDFA(int alphabetSize, TTTTransitionDFA<I> parentTransition, int id, boolean accepting) {
		this.id = id;
		this.accepting = accepting;
		this.parentTransition = parentTransition;
		this.transitions = new TTTTransitionDFA[alphabetSize];
	}
	
	/**
	 * Retrieves whether or not this state is accepting. 
	 * @return {@code true} if this state is accepting, {@code false} otherwise
	 */
	public boolean isAccepting() {
		return accepting;
	}
	
	/**
	 * Checks whether this state is the initial state (i.e., the root of the
	 * spanning tree).
	 * @return {@code true} if this state is the initial state, {@code false} otherwise
	 */
	public boolean isRoot() {
		return (parentTransition == null);
	}
	
	/**
	 * Retrieves the discrimination tree leaf associated with this state.
	 * @return the discrimination tree leaf associated with this state
	 */
	public DTNode<I> getDTLeaf() {
		return dtLeaf;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.ttt.dfa.AccessSequenceProvider#getAccessSequence()
	 */
	@Override
	public Word<I> getAccessSequence() {
		if(parentTransition != null) {
			return parentTransition.getAccessSequence();
		}
		return Word.epsilon(); // root
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "s" + id;
	}
}
