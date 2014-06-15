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

/**
 * Abstract base class for objects that may occur in a {@link BlockList},
 * either as a value element or the head of the list (which represents the list itself,
 * but does not carry any value).
 * <p>
 * The purpose of this class is to enable managing block lists <i>intrusively</i>.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public abstract class BlockListElem<I> {
	
	protected DTNode<I> nextBlock;

}
