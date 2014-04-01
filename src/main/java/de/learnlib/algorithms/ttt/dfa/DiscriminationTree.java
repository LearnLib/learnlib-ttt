package de.learnlib.algorithms.ttt.dfa;

import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

import net.automatalib.words.Word;

public class DiscriminationTree<I> {

	private final DTNode<I> root;
	
	private final MembershipOracle<I, Boolean> oracle;
	
	public DiscriminationTree(MembershipOracle<I, Boolean> oracle) {
		this.root = new DTNode<>();
		this.oracle = oracle;
	}
	
	public DTNode<I> getRoot() {
		return root;
	}
	
	
	public DTNode<I> sift(Word<I> word) {
		return sift(root, word);
	}
	
	public DTNode<I> sift(DTNode<I> start, Word<I> word) {
		DTNode<I> curr = start;
		
		while(!curr.isLeaf()) {
			boolean outcome = mqOut(word, curr.getDiscriminator());
			curr = curr.getChild(outcome);
		}
		
		return curr;
	}
	
	
	
	
	private boolean mqOut(Word<I> prefix, Word<I> suffix) {
		return MQUtil.output(oracle, prefix, suffix);
	}
	

}
