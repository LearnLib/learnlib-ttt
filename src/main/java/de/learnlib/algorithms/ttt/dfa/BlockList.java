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
 * A list for storing blocks (identified by their root {@link DTNode}s). The
 * list is implemented as a singly-linked list, and allows O(1) insertion
 * and removal of elements.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class BlockList<I> extends BlockListElem<I> implements Iterable<DTNode<I>> {
	
	/**
	 * Iterator for a {@link BlockList}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
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
	
	/**
	 * Inserts a block into the list. Currently, the block is inserted
	 * at the head of the list. However, callers should not rely on this.
	 * @param blockRoot the root node of the block to be inserted
	 */
	public void insertBlock(DTNode<I> blockRoot) {
		blockRoot.removeFromBlockList();
		
		blockRoot.nextBlock = nextBlock;
		if(nextBlock != null) {
			nextBlock.prevBlock = blockRoot;
		}
		blockRoot.prevBlock = this;
		nextBlock = blockRoot;
	}
	
	/**
	 * Retrieves any block from the list. If the list is empty,
	 * {@code null} is returned.
	 * @return any block from the list, or {@code null} if the list is empty.
	 */
	public DTNode<I> chooseBlock() {
		return nextBlock;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<DTNode<I>> iterator() {
		return new ListIterator<>(nextBlock);
	}

	/**
	 * Checks whether this list is empty.
	 * @return {@code true} if the list is empty, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return (nextBlock == null);
	}
}
