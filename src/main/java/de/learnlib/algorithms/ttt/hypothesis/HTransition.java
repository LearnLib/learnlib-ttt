package de.learnlib.algorithms.ttt.hypothesis;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.algorithms.ttt.dtree.DTNode;

public class HTransition<I, O, SP, TP> {
	
	// GENERAL PURPOSE FIELDS
	private final HypothesisState<I,O,SP,TP> source;
	private final I symbol;
	private TP property;
	
	// TREE EDGE FIELDS
	private HypothesisState<I,O,SP,TP> treeTgt;
	
	// NON-TREE EDGE FIELDS
	private DTNode<I,O,SP,TP> dt;
	

	public HTransition(HypothesisState<I,O,SP,TP> source, I symbol, DTNode<I,O,SP,TP> dtTgt) {
		this.source = source;
		this.symbol = symbol;
		this.treeTgt = null;
		this.dt = dtTgt;
	}
	
	public boolean isTree() {
		return (treeTgt != null);
	}
	
	public HypothesisState<I,O,SP,TP> getSource() {
		return source;
	}
	
	public I getSymbol() {
		return symbol;
	}
	
	public TP getProperty() {
		return property;
	}
	
	public void setProperty(TP property) {
		this.property = property;
	}
	
	public HypothesisState<I,O,SP,TP> getTreeTarget() {
		return treeTgt;
	}
	
	public DTNode<I,O,SP,TP> getDT() {
		return dt;
	}
	
	public DTNode<I,O,SP,TP> currentDTTarget() {
		if(treeTgt != null)
			return treeTgt.getDTLeaf();
		return dt;
	}
	
	public void updateDTTarget(DTNode<I,O,SP,TP> dtTgt) {
		assert (dtTgt != null);
		this.dt = dtTgt;
	}
	
	public void makeTree(HypothesisState<I,O,SP,TP> treeTgt) {
		if(this.treeTgt != null)
			throw new IllegalStateException("Cannot make transition [" + getAccessSequence() + "] a tree transition: already is");
		
		this.treeTgt = treeTgt;
		this.dt = null;
	}

	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<I>(source.getDepth() + 1);
		source.appendAccessSequence(wb);
		wb.append(symbol);
		return wb.toWord();
	}

	public HypothesisState<I, O, SP, TP> nonTreeTarget() {
		if(treeTgt != null)
			throw new IllegalStateException("Cannot invoke nonTreeTarget() on a tree transition");
		return dt.getTempRoot().getState();
	}

}
