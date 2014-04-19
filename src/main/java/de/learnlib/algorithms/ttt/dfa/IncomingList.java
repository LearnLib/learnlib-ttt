package de.learnlib.algorithms.ttt.dfa;

import java.util.Iterator;

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
