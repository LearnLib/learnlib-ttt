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

import java.util.Iterator;

/**
 * The head of the intrusive linked list for storing incoming transitions of a DT node.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class IncomingList<I> extends IncomingListElem<I> implements Iterable<TTTTransitionDFA<I>> {
	
	private static final class ListIterator<I> implements Iterator<TTTTransitionDFA<I>> {
		private TTTTransitionDFA<I> cursor;
		
		public ListIterator(TTTTransitionDFA<I> start) {
			this.cursor = start;
		}

		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public TTTTransitionDFA<I> next() {
			TTTTransitionDFA<I> curr = cursor;
			cursor = cursor.nextIncoming;
			return curr;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		
	}

	public void insertIncoming(TTTTransitionDFA<I> transition) {
		transition.removeFromList();
		
		transition.nextIncoming = nextIncoming;
		transition.prevIncoming = this;
		if(nextIncoming != null) {
			nextIncoming.prevIncoming = transition;
		}
		this.nextIncoming = transition;
	}
	
	public void insertAllIncoming(IncomingList<I> list) {
		insertAllIncoming(list.nextIncoming);
	}
	
	public void insertAllIncoming(TTTTransitionDFA<I> firstTransition) {
		if(firstTransition == null) {
			return;
		}
		
		if(nextIncoming == null) {
			nextIncoming = firstTransition;
			firstTransition.prevIncoming = this;
		}
		else {
			TTTTransitionDFA<I> oldNext = nextIncoming;
			nextIncoming = firstTransition;
			firstTransition.prevIncoming = this;
			TTTTransitionDFA<I> last = firstTransition;
			
			while(last.nextIncoming != null) {
				last = last.nextIncoming;
			}
			
			last.nextIncoming = oldNext;
			oldNext.prevIncoming = last;
		}
	}
	
	public TTTTransitionDFA<I> choose() {
		return nextIncoming;
	}

	@Override
	public Iterator<TTTTransitionDFA<I>> iterator() {
		return new ListIterator<>(nextIncoming);
	}
}
