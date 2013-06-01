package de.learnlib.algorithms.ttt.stree;

import java.util.List;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class STNode<I> {
	
	private final I symbol;
	private final STNode<I> parent;
	private final int depth;
	private final int id;
	

	public STNode() {
		this(null, null, 0);
	}
	
	public STNode(I symbol, STNode<I> parent, int id) {
		this.symbol = symbol;
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public I getSymbol() {
		return symbol;
	}
	
	public STNode<I> getParent() {
		return parent;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public boolean isRoot() {
		return (parent == null);
	}
	
	public void appendSuffix(List<? super I> symList) {
		if(parent == null)
			return;
		symList.add(symbol);
		parent.appendSuffix(symList);
	}
	
	public Word<I> getSuffix() {
		if(parent == null)
			return Word.epsilon();
		WordBuilder<I> wb = new WordBuilder<>(depth);
		appendSuffix(wb);
		return wb.toWord();
	}

}
