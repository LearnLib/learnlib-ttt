package de.learnlib.algorithms.ttt.hypothesis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.dtree.TempDTNode;

public class HypothesisState<I,O,SP,TP> {

	private final HTransition<I,O,SP,TP> treeIncoming;
	private final int id;
	private final int depth;
	
	private DTNode<I,O,SP,TP> dtLeaf;
	private TempDTNode<I, O, SP, TP> tempDt;
	
	private SP property;
	
	private final HTransition<I,O,SP,TP>[] transitions;
	
	public HypothesisState(int alphabetSize) {	
		this(alphabetSize, 0, null);
	}
	
	@SuppressWarnings("unchecked")
	public HypothesisState(int alphabetSize, int id, HTransition<I,O,SP,TP> treeIncoming) {
		this.id = id;
		this.treeIncoming = treeIncoming;
		this.depth = (treeIncoming == null) ? 0 : treeIncoming.getSource().depth + 1;
		this.transitions = new HTransition[alphabetSize];
	}
	
	
	public DTNode<I,O,SP,TP> getDTLeaf() {
		return dtLeaf;
	}
	
	public void setTempDT(TempDTNode<I, O, SP, TP> tempDt) {
		this.tempDt = tempDt;
	}
	
	public TempDTNode<I, O, SP, TP> getTempDT() {
		return tempDt;
	}
	
	public void setDTLeaf(DTNode<I,O,SP,TP> dtLeaf) {
		this.dtLeaf = dtLeaf;
	}
	
	public HTransition<I,O,SP,TP> getTreeIncoming() {
		return treeIncoming;
	}
	
	public void appendAccessSequence(List<? super I> symList) {
		if(treeIncoming == null)
			return;
		treeIncoming.getSource().appendAccessSequence(symList);
		symList.add(treeIncoming.getSymbol());
	}
	
	public Word<I> getAccessSequence() {
		if(treeIncoming == null)
			return Word.epsilon();
		WordBuilder<I> wb = new WordBuilder<>(depth);
		appendAccessSequence(wb);
		return wb.toWord();
	}
	
	
	public SP getProperty() {
		return property;
	}
	
	public void setProperty(SP property) {
		this.property = property;
	}
	
	public int getId() {
		return id;
	}
	
	public HTransition<I,O,SP,TP> getTransition(int transIdx) {
		return transitions[transIdx];
	}
	
	public void setTransition(int transIdx, HTransition<I,O,SP,TP> transition) {
		transitions[transIdx] = transition;
	}
	
	public Collection<HTransition<I,O,SP,TP>> getOutgoingTransitions() {
		return Collections.unmodifiableList(Arrays.asList(transitions));
	}

	public int getDepth() {
		return depth;
	}
	
	@Override
	public String toString() {
		return "q" + id;
	}
}
