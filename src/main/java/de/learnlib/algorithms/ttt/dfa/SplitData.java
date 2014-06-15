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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data associated with a {@link DTNode} while an enclosing subtree is being split.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class SplitData<I> {
	
	// TODO: HashSets/Maps are quite an overkill for booleans
	private final Set<Boolean> marks = new HashSet<>();
	private final Map<Boolean, IncomingList<I>> incomingTransitions
		= new HashMap<>();
	
	private Boolean stateLabel;
	
	/**
	 * Mark this node with the given label. The result indicates whether
	 * it has been newly marked.
	 * 
	 * @param label the label to mark this node with
	 * @return {@code true} if the node was previously unmarked (wrt. to the given label),
	 * {@code false} otherwise
	 */
	public boolean mark(boolean label) {
		return marks.add(label);
	}
	
	/**
	 * Checks whether there is a state label associated with this node,
	 * regardless of its value.
	 * 
	 * @return {@code true} if there is a state label ({@code true} or {@code false})
	 * associated with this node, {@code false} otherwise
	 */
	public boolean hasStateLabel() {
		return (stateLabel != null);
	}
	
	/**
	 * Sets the state label associated with this split data.
	 * <p>
	 * <b>Note:</b> invoking this operation is illegal if a state label has already
	 * been set.
	 * 
	 * @param label the state label
	 */
	public void setStateLabel(boolean label) {
		assert !hasStateLabel();
		
		this.stateLabel = label;
	}
	
	/**
	 * Retrieves the state label associated with this split data.
	 * <p>
	 * <b>Note:</b> invoking this operation is illegal if no state label
	 * has previously been set.
	 * @return the state label
	 */
	public boolean getStateLabel() {
		assert hasStateLabel();
		
		return stateLabel;
	}
	
	/**
	 * Retrieves the list of incoming transitions for the respective label.
	 * <p>
	 * This method will always return a non-{@code null} value.
	 * 
	 * @param label the label
	 * @return the (possibly empty) list associated with the given state label
	 */
	public IncomingList<I> getIncoming(boolean label) {
		IncomingList<I> list = incomingTransitions.get(label);
		if(list == null) {
			list = new IncomingList<>();
			incomingTransitions.put(label, list);
		}
		
		return list;
	}
	
	/**
	 * Checks whether the corresponding node is marked with the given label.
	 * 
	 * @param label the label
	 * @return {@code true} if the corresponding node is marked with the given
	 * label, {@code false} otherwise
	 */
	public boolean isMarked(boolean label) {
		return marks.contains(label);
	}
	
	
}
