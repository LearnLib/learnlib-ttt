package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

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
	
	public DTNode<I> sift(AccessSequenceProvider<I> asp) {
		return sift(asp, true);
	}
	
	public DTNode<I> sift(AccessSequenceProvider<I> asp, boolean hard) {
		return sift(asp.getAccessSequence(), hard);
	}
	
	public DTNode<I> sift(Word<I> word) {
		return sift(word, true);
	}
	
	public DTNode<I> sift(Word<I> word, boolean hard) {
		return sift(root, word, hard);
	}
	
	public DTNode<I> sift(DTNode<I> start, AccessSequenceProvider<I> asp, boolean hard) {
		return sift(start, asp.getAccessSequence(), hard);
	}
	
	public DTNode<I> sift(DTNode<I> start, Word<I> word, boolean hard) {
		DTNode<I> curr = start;
		
		while(!curr.isLeaf() && (hard || !curr.isTemp())) {
			boolean outcome = mqOut(word, curr.getDiscriminator());
			curr = curr.getChild(outcome);
		}
		
		return curr;
	}
	
	
	public DTNode<I> leastCommonAncestor(DTNode<I> node1, DTNode<I> node2) {
		int ddiff = node1.getDepth() - node2.getDepth();
		
		DTNode<I> curr1, curr2;
		if(ddiff < 0) {
			curr1 = node2;
			curr2 = node1;
			ddiff *= -1;
		}
		else {
			curr1 = node1;
			curr2 = node2;
		}
		
		for(int i = 0; i < ddiff; i++) {
			curr1 = curr1.getParent();
		}
		
		while(curr1 != curr2) {
			curr1 = curr1.getParent();
			curr2 = curr2.getParent();
		}
		
		return curr1;
	}
	
	
	private boolean mqOut(Word<I> prefix, Word<I> suffix) {
		return MQUtil.output(oracle, prefix, suffix);
	}
	

}
