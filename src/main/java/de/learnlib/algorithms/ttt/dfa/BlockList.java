package de.learnlib.algorithms.ttt.dfa;

import java.util.Iterator;

public class BlockList<I> extends BlockListElem<I> implements Iterable<DTNode<I>> {
	
	private static final class ListIterator<I> implements Iterator<DTNode<I>> {
		private DTNode<I> cursor;
		public ListIterator(DTNode<I> start) {
			this.cursor = start;
		}
		@Override
		public boolean hasNext() {
			return cursor != null;
		}
		@Override
		public DTNode<I> next() {
			DTNode<I> current = cursor;
			cursor = cursor.nextBlock;
			return current;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public void insertBlock(DTNode<I> blockRoot) {
		blockRoot.removeFromBlockList();
		
		blockRoot.nextBlock = nextBlock;
		if(nextBlock != null) {
			nextBlock.prevBlock = blockRoot;
		}
		blockRoot.prevBlock = this;
		nextBlock = blockRoot;
	}
	
	
	public DTNode<I> chooseBlock() {
		return nextBlock;
	}

	@Override
	public Iterator<DTNode<I>> iterator() {
		return new ListIterator<>(nextBlock);
	}

	
	public boolean isEmpty() {
		return (nextBlock == null);
	}
}
