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
import net.automatalib.words.WordBuilder;

/**
 * A transition in a {@link TTTHypothesisDFA}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class TTTTransitionDFA<I> extends IncomingListElem<I> implements AccessSequenceProvider<I> {
	
	
	
	private final TTTStateDFA<I> source;
	private final I input;

	// TREE TRANSITION
	private TTTStateDFA<I> treeTarget;
	
	// NON-TREE TRANSITION
	DTNode<I> nonTreeTarget;
	
	protected IncomingListElem<I> prevIncoming;
	

	public TTTTransitionDFA(TTTStateDFA<I> source, I input) {
		this.source = source;
		this.input = input;
	}
	
	
	public boolean isTree() {
		return (treeTarget != null);
	}
	
	public TTTStateDFA<I> getTreeTarget() {
		assert isTree();
		
		return treeTarget;
	}
	
	public DTNode<I> getNonTreeTarget() {
		assert !isTree();
		
		return nonTreeTarget;
	}
	
	public DTNode<I> getDTTarget() {
		if(treeTarget != null) {
			return treeTarget.dtLeaf;
		}
		return nonTreeTarget;
	}
	
	
	public TTTStateDFA<I> getTarget() {
		if(treeTarget != null) {
			return treeTarget;
		}
		
		return nonTreeTarget.state;
	}
	
	public TTTStateDFA<I> getSource() {
		return source;
	}
	
	public I getInput() {
		return input;
	}
	
	@Override
	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<>(); // FIXME capacity hint
		
		TTTTransitionDFA<I> curr = this;
		
		while(curr != null) {
			wb.add(curr.input);
			curr = curr.source.parentTransition;
		}
		
		return wb.reverse().toWord();
	}
	
	void makeTree(TTTStateDFA<I> treeTarget) {
		removeFromList();
		this.treeTarget = treeTarget;
		this.nonTreeTarget = null;
	}
	
	void setNonTreeTarget(DTNode<I> nonTreeTarget) {
		this.nonTreeTarget = nonTreeTarget;
		nonTreeTarget.getIncoming().insertIncoming(this);
	}
	
	
	void removeFromList() {
		if(prevIncoming != null) {
			prevIncoming.nextIncoming = nextIncoming;
		}
		if(nextIncoming != null) {
			nextIncoming.prevIncoming = prevIncoming;
		}
	}
}
