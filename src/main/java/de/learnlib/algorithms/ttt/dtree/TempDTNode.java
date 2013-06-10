package de.learnlib.algorithms.ttt.dtree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

public class TempDTNode<I,O,SP,TP> {
	
	public static <I,O,SP,TP> TempDTNode<I, O, SP, TP> commonAncestor(TempDTNode<I, O, SP, TP> a, TempDTNode<I, O, SP, TP> b) {
		int ad = a.depth;
		int bd = b.depth;
		
		if(ad < bd) {
			TempDTNode<I, O, SP, TP> tmp = a;
			a = b;
			b = tmp;
			int tmpd = ad;
			ad = bd;
			bd = tmpd;
		}
		
		while(ad-- > bd)
			a = a.parent;
		
		while(a != b) {
			a = a.parent;
			b = b.parent;
		}
		
		return a;
	}

	private TempDTNode<I, O, SP, TP> parent;
	private int depth;
	
	// LEAF FIELDS
	private HypothesisState<I, O, SP, TP> state;

	// INTERNAL NODE FIELDS
	private Word<I> suffix;
	private Map<O,TempDTNode<I,O,SP,TP>> children;

	public TempDTNode(Word<I> suffix, TempDTNode<I, O, SP, TP> parent) {
		this.suffix = suffix;
		this.children = new HashMap<>();
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	public TempDTNode(HypothesisState<I, O, SP, TP> state, TempDTNode<I, O, SP, TP> parent) {
		this.state = state;
		if(state != null)
			state.setTempDT(this);
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	/**
	 * Cleans up a temporary tree. Cleaning up means that:
	 * <ul>
	 * <li>all nodes with an outdegree of exactly 1 are removed from the tree</li>
	 * <li>all parent pointers are corrected</li>
	 * </ul>
	 * Note that depth values are not modified by this method. For this purpose, use
	 * {@link #fixDepth(int)}.
	 */
	public TempDTNode<I, O, SP, TP> cleanUp() {
		parent = null;
		
		if(children == null)
			return this;
		
		for(Map.Entry<O,TempDTNode<I,O,SP,TP>> e : children.entrySet()) {
			TempDTNode<I, O, SP, TP> child = e.getValue();
			TempDTNode<I, O, SP, TP> cleanChild = child.cleanUp();
			e.setValue(cleanChild);
			cleanChild.parent = this;
		}
		
		if(children.size() == 1) {
			TempDTNode<I, O, SP, TP> onlyChild = children.values().iterator().next();
			return onlyChild;
		}
		
		return this;
	}
	
	public void updateDT(DTNode<I, O, SP, TP> dt) {
		System.err.println("updateDT(" + dt.getTempRoot() + ")");
		if(children == null) {
			state.setDTLeaf(dt);
			return;
		}
		
		for(Map.Entry<O,TempDTNode<I, O, SP, TP>> e : children.entrySet()) {
			TempDTNode<I, O, SP, TP> child = e.getValue();
			child.updateDT(dt);
		}
	}
	
	public void fixDepth(int depth) {
		this.depth = depth;
		if(children != null) {
			for(Map.Entry<O,TempDTNode<I,O,SP,TP>> e : children.entrySet())
				e.getValue().fixDepth(depth + 1);
		}
	}
	
	public void setState(HypothesisState<I, O, SP, TP> state) {
		this.state = state;
		state.setTempDT(this);
	}
	
	public TempDTNode<I, O, SP, TP> getParent() {
		return parent;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public boolean isLeaf() {
		return (state != null);
	}
	
	public Word<I> getSuffix() {
		return suffix;
	}
	
	public HypothesisState<I, O, SP, TP> getState() {
		return state;
	}
	
	
	public TempDTNode<I,O,SP,TP> sift(MembershipOracle<I, O> oracle, Word<I> word) {
		TempDTNode<I, O, SP, TP> curr = this;
		
		while(curr.suffix != null) {
			O out = MQUtil.query(oracle, word, curr.suffix);
			TempDTNode<I, O, SP, TP> child = curr.children.get(out);
			if(child == null) {
				child = new TempDTNode<>((HypothesisState<I, O, SP, TP>)null, curr);
				curr.children.put(out, child);
			}
			curr = child;
		}
		
		return curr;
	}
	
	public void split(Word<I> suffix, O oldOutcome, HypothesisState<I, O, SP, TP> newState, O newOutcome) {
		if(state == null)
			throw new IllegalArgumentException("Can only split leaves!");
		this.children = new HashMap<>();
		TempDTNode<I, O, SP, TP> oldNode = new TempDTNode<>(state, this);
		TempDTNode<I, O, SP, TP> newNode = new TempDTNode<>(newState, this);
		children.put(oldOutcome, oldNode);
		children.put(newOutcome, newNode);
		this.state = null;
		this.suffix = suffix;
	}
	
	public Map<O,TempDTNode<I, O, SP, TP>> treeSplit(MembershipOracle<I, O> oracle, Word<I> suffix) {
		if(state != null) {
			O out = MQUtil.query(oracle, state.getAccessSequence(), suffix);
			return Collections.singletonMap(out, this);
		}
		
		if(this.suffix.equals(suffix))
			return children;
		
		Map<O,TempDTNode<I,O,SP,TP>> result = new HashMap<>();
		
		for(Map.Entry<O,TempDTNode<I,O,SP,TP>> childEntry : children.entrySet()) {
			O out = childEntry.getKey();
			TempDTNode<I, O, SP, TP> child = childEntry.getValue();
			Map<O,TempDTNode<I, O, SP, TP>> childRes = child.treeSplit(oracle, suffix);
			
			for(Map.Entry<O,TempDTNode<I,O,SP,TP>> crEntry : childRes.entrySet()) {
				O splitKey = crEntry.getKey();
				TempDTNode<I, O, SP, TP> n = crEntry.getValue();
				
				TempDTNode<I, O, SP, TP> p = result.get(splitKey);
				if(p == null) {
					p = new TempDTNode<>(suffix, null);
					result.put(splitKey, p);
				}
				p.getChildren().put(out, n);
			}
		}
			
		return result;
	}
	

	
	public Map<O,TempDTNode<I,O,SP,TP>> getChildren() {
		return children;
	}
	
	
	@Override
	public String toString() {
		if(suffix == null)
			return String.valueOf(state);
		StringBuilder sb = new StringBuilder();
		sb.append(suffix.toString());
		sb.append('{');
		for(Map.Entry<O,TempDTNode<I, O, SP, TP>> e : children.entrySet()) {
			sb.append(String.valueOf(e.getKey()));
			sb.append(" -> ");
			sb.append(e.getValue().toString());
			sb.append(",");
		}
		sb.append('}');
		return sb.toString();
	}
	
	public void replace(Word<I> newSuffix, Map<O, TempDTNode<I, O, SP, TP>> children) {
		this.suffix = newSuffix;
		this.children = children;
		cleanUp();
		fixDepth(depth);
	}


}
