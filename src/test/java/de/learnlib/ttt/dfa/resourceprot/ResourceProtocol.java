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
package de.learnlib.ttt.dfa.resourceprot;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import de.learnlib.examples.LearningExample.DFALearningExample;

public class ResourceProtocol implements DFALearningExample<Action> {
	
	private static final int OPEN_MASK = 0x1;
	private static final int RW_MASK = 0x2;
	
	private static int getResourceState(int state, int rid) {
		return (state >> (rid * 2)) & 0x3;
	}
	
	private static boolean isOpen(int state, int rid) {
		return (getResourceState(state, rid) & OPEN_MASK) != 0;
	}
	
	private static boolean isRW(int state, int rid) {
		return (getResourceState(state, rid) & RW_MASK) != 0;
	}
	
	private static int setOpen(int state, int rid, boolean open) {
		int chg = OPEN_MASK << (rid * 2);
		if(open) {
			state |= chg;
		}
		else {
			state &= ~chg;
		}
		return state;
	}
	
	private static int setRW(int state, int rid, boolean rw) {
		int chg = RW_MASK << (rid * 2);
		if(rw) {
			state |= chg;
		}
		else {
			state &= ~chg;
		}
		return state;
	}
	
	public static final CompactDFA<Action> generate(int numResources) {
		if(numResources <= 0 || numResources > 10) {
			throw new IllegalArgumentException();
		}
		
		FastAlphabet<Action> alphabet = new FastAlphabet<>();
		
		for(int i = 0; i < numResources; i++) {
			for(Action.Type at : Action.Type.values()) {
				alphabet.addSymbol(new Action(at, i));
			}
		}
		
		int size = (1 << (2*numResources));
		
		CompactDFA<Action> result = new CompactDFA<>(alphabet, size + 1);
		
		result.addInitialState(true);
		for(int i = 1; i < size; i++) {
			result.addState(true);
		}
		
		int sink = result.addState(false);
		
		for(int state = 0; state < size; state++) {
			for(Action act : alphabet) {
				int idx = alphabet.getSymbolIndex(act);
				
				int rid = act.resourceId;
				
				int succ;
				
				switch(act.type) {
				case OPEN:
					if(isOpen(state, rid)) {
						succ = sink;
					}
					else {
						succ = setOpen(state, rid, true);
					}
					break;
				case CLOSE:
					if(isOpen(state, rid)) {
						succ = setOpen(state, rid, false);
					}
					else {
						succ = sink;
					}
					break;
				case CHMOD_RO:
					if(isOpen(state, rid)) {
						succ = sink;
					}
					else {
						succ = setRW(state, rid, false);
					}
					break;
				case CHMOD_RW:
					if(isOpen(state, rid)) {
						succ = sink;
					}
					else {
						succ = setRW(state, rid, true);
					}
					break;
				case READ:
					if(isOpen(state, rid)) {
						succ = state;
					}
					else {
						succ = sink;
					}
					break;
				case WRITE:
					if(isOpen(state, rid) && isRW(state, rid)) {
						succ = state;
					}
					else {
						succ = sink;
					}
					break;
				default:
					throw new AssertionError();
				}
				result.setTransition(state, act, succ);
			}
		}
		
		for(Action act : alphabet) {
			result.setTransition(sink, act, sink);
		}
		
		return result;
	}
	
	private final int numResources;
	private final CompactDFA<Action> model;
	
	public ResourceProtocol(int numResources) {
		this.numResources = numResources;
		this.model = generate(numResources);
	}

	@Override
	public DFA<?, Action> getReferenceAutomaton() {
		return model;
	}

	@Override
	public Alphabet<Action> getAlphabet() {
		return model.getInputAlphabet();
	}
	
	@Override
	public String toString() {
		return "ResourceProtocol(" + numResources + ")";
	}
}
