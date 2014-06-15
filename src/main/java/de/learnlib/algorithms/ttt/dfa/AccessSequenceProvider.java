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
 * Common interface for objects that have an access sequence associated with
 * them (e.g., states and transitions of a {@link TTTHypothesisDFA}).
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public interface AccessSequenceProvider<I> {
	
	/**
	 * Retrieves the access sequence of this object.
	 * @return the access sequence
	 */
	public Word<I> getAccessSequence();

}
