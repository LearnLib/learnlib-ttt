package de.learnlib.algorithms.ttt.stree;

import java.util.List;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class STNode<I> {
	
	private final I symbol;
	private final STNode<I> parent;
	private final int depth;
	private final int id;
	private Word<I> tmpWord;
	private STNode<I> finalReplacement;
	

	public STNode() {
		this(null, null, 0);
	}
	
	
	public STNode(Word<I> tmpWord) {
		this.symbol = null;
		this.parent = null;
		this.depth = -1;
		this.id = -1;
		this.tmpWord = tmpWord;
		this.finalReplacement = null;
	}
	
	
	public STNode(I symbol, STNode<I> parent, int id) {
		this.symbol = symbol;
		this.parent = parent;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
		this.id = id;
		this.tmpWord = null;
		this.finalReplacement = null;
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
		return (id == 0);
	}
	
	public boolean isTemp() {
		return (id == -1); 
	}
	
	public void appendSuffix(List<? super I> symList) {
		if(id == -1) {
			CollectionsUtil.addAll(symList, tmpWord);
			return;
		}
		if(parent == null)
			return;
		symList.add(symbol);
		parent.appendSuffix(symList);
	}
	
	public Word<I> getSuffix() {
		if(id == -1) {
			return tmpWord;
		}
		
		if(parent == null)
			return Word.epsilon();
		WordBuilder<I> wb = new WordBuilder<>(depth);
		appendSuffix(wb);
		return wb.toWord();
	}
	
	public void setTempWord(Word<I> tmpWord) {
		assert (id == -1);
		this.tmpWord = tmpWord;
	}
	
	public void setFinalReplacement(STNode<I> finalReplacement) {
		this.finalReplacement = finalReplacement;
	}
	
	public STNode<I> getFinalReplacement() {
		return finalReplacement;
	}

}
