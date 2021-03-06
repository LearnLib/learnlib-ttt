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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.words.Word;

import com.google.common.collect.AbstractIterator;

public class DTNode<I> extends BlockListElem<I> {
	
	
	private static final class StatesIterator<I> extends AbstractIterator<TTTStateDFA<I>> {
		private final Deque<DTNode<I>> stack = new ArrayDeque<>();
		public StatesIterator(DTNode<I> root) {
			stack.push(root);
		}
		@Override
		protected TTTStateDFA<I> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I> curr = stack.pop();
				
				if(curr.isLeaf()) {
					if(curr.state != null) {
						return curr.state;
					}
				}
				else {
					stack.push(curr.getTrueChild());
					stack.push(curr.getFalseChild());
				}
			}
			
			return endOfData();
		}
	}
	
	private static final class LeavesIterator<I> extends AbstractIterator<DTNode<I>> {
		private final Deque<DTNode<I>> stack = new ArrayDeque<>();
		public LeavesIterator(DTNode<I> root) {
			stack.push(root);
		}
		@Override
		protected DTNode<I> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I> curr = stack.pop();
				
				if(curr.isLeaf()) {
					return curr;
				}
				else {
					stack.push(curr.getTrueChild());
					stack.push(curr.getFalseChild());
				}
			}
			
			return endOfData();
		}
	}
	
	private static final class NodesIterator<I> extends AbstractIterator<DTNode<I>> {
		private final Deque<DTNode<I>> stack = new ArrayDeque<>();
		public NodesIterator(DTNode<I> root) {
			stack.push(root);
		}
		
		@Override
		protected DTNode<I> computeNext() {
			while(!stack.isEmpty()) {
				DTNode<I> curr = stack.pop();
				
				if(curr.isInner()) {
					stack.push(curr.getFalseChild());
					stack.push(curr.getTrueChild());
				}
				
				return curr;
			}
			return endOfData();
		}
	}

	private final DTNode<I> parent;
	private final boolean parentEdgeLabel;
	private final int depth;
	
	SplitData<I> splitData = null;
	
	private final IncomingList<I> incoming = new IncomingList<>();
	
	// INNER NODE DATA
	private Word<I> discriminator;
	private DTNode<I> falseChild;
	private DTNode<I> trueChild;
	BlockListElem<I> prevBlock;
	
	// LEAF NODE DATA
	TTTStateDFA<I> state;
	
	boolean temp = false;
	
	
	public DTNode() {
		this(null, false);
	}
	
	public DTNode(DTNode<I> parent, boolean parentEdgeLabel) {
		this.parent = parent;
		this.parentEdgeLabel = parentEdgeLabel;
		this.depth = (parent != null) ? parent.depth + 1 : 0; 
	}
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	
	public boolean isInner() {
		return (discriminator != null);
	}
	
	public boolean isLeaf() {
		return (discriminator == null);
	}
	
	public DTNode<I> getFalseChild() {
		return falseChild;
	}
	
	public DTNode<I> getTrueChild() {
		return trueChild;
	}
	
	public DTNode<I> getExtremalChild(boolean label) {
		DTNode<I> curr = this;
		
		while(!curr.isLeaf()) {
			curr = curr.getChild(label);
		}
		
		return curr;
	}
	
	public void setFalseChild(DTNode<I> newFalseChild) {
		assert newFalseChild.parent == this;
		assert newFalseChild.parentEdgeLabel == false;
		this.falseChild = newFalseChild;
	}
	
	public void setTrueChild(DTNode<I> newTrueChild) {
		assert newTrueChild.parent == this;
		assert newTrueChild.parentEdgeLabel == true;
		this.trueChild = newTrueChild;
	}
	
	public void setChild(boolean value, DTNode<I> newChild) {
		if(value) {
			setTrueChild(newChild);
		}
		else {
			setFalseChild(newChild);
		}
	}
	
	public DTNode<I> getChild(boolean value) {
		assert isInner();
		
		return value ? trueChild : falseChild;
	}
	
	public DTNode<I> getParent() {
		return parent;
	}
	
	public boolean getParentEdgeLabel() {
		return parentEdgeLabel;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public boolean isTemp() {
		return temp;
	}
	
	public Iterator<TTTStateDFA<I>> subtreeStatesIterator() {
		return new StatesIterator<>(this);
	}
	
	
	void split(Word<I> discriminator) {
		assert state == null;
		
		split(discriminator, false, null);
	}
	
	void split(Word<I> discriminator, boolean newOut, TTTStateDFA<I> newState) {
		boolean oldOut = !newOut;
		DTNode<I> oldChild = new DTNode<>(this, oldOut);
		oldChild.state = this.state;
		this.state = null;
		this.discriminator = discriminator;
		setChild(oldOut, oldChild);
		
		DTNode<I> newChild = new DTNode<>(this, newOut);
		newChild.state = newState;
		setChild(newOut, newChild);
	}

	public IncomingList<I> getIncoming() {
		return incoming;
	}
	
	public Iterator<DTNode<I>> subtreeLeavesIterator() {
		return new LeavesIterator<>(this);
	}
	
	public Iterable<DTNode<I>> subtreeLeaves() {
		return new Iterable<DTNode<I>>() {
			@Override
			public Iterator<DTNode<I>> iterator() {
				return subtreeLeavesIterator();
			}
		};
	}
	
	public Iterator<DTNode<I>> subtreeNodesIterator() {
		return new NodesIterator<>(this);
	}
	
	public Boolean subtreeLabel(DTNode<I> descendant) {
		DTNode<I> curr = descendant;
		
		while(curr.depth > this.depth + 1) {
			curr = curr.parent;
		}
		
		if(curr.parent != this) {
			return null;
		}
		
		return curr.parentEdgeLabel;
	}
	
	/**
	 * Updates the {@link TTTTransitionDFA#nonTreeTarget} attribute to
	 * point to this node for all transitions in the incoming
	 * list.
	 */
	void updateIncoming() {
		for(TTTTransitionDFA<I> trans : incoming) {
			trans.nonTreeTarget = this;
		}
	}
	
	
	boolean isBlockRoot() {
		return (prevBlock != null);
	}
	
	DTNode<I> getBlockRoot() {
		DTNode<I> curr = this;
		
		while(curr != null && !curr.isBlockRoot()) {
			curr = curr.parent;
		}
		
		return curr;
	}
	
	
	void removeFromBlockList() {
		if(prevBlock != null) {
			prevBlock.nextBlock = nextBlock;
			if(nextBlock != null) {
				nextBlock.prevBlock = prevBlock;
			}
			prevBlock = nextBlock = null;
		}
	}
	
	void setDiscriminator(Word<I> newDiscriminator) {
		assert isInner();
		
		this.discriminator = newDiscriminator;
	}
	
	
}
