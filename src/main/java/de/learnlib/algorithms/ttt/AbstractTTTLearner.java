package de.learnlib.algorithms.ttt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.dtree.DiscriminationTree;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis;
import de.learnlib.algorithms.ttt.stree.SuffixTree;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;

public class AbstractTTTLearner<I, O, SP, TP, M, H extends TTTHypothesis<I, O, SP, TP, ?>> implements
		LearningAlgorithm<M, I, O> {

	private final Alphabet<I> alphabet;
	private final DiscriminationTree<I, O, SP, TP> dtree;
	private final SuffixTree<I> stree = new SuffixTree<>();
	protected final H hypothesis;
	
	private final Queue<HTransition<I,O,SP,TP>> openTransitions = new ArrayDeque<>();
	private final List<HypothesisState<I,O,SP,TP>> newStates = new ArrayList<>();
	private final List<HTransition<I,O,SP,TP>> newTransitions = new ArrayList<>();
	
	private final MembershipOracle<I,O> oracle;
	
	public AbstractTTTLearner(Alphabet<I> alphabet, H hypothesis) {
		this.alphabet = alphabet;
		this.hypothesis = hypothesis;
		this.dtree = new DiscriminationTree<>(hypothesis.getInitialState());
	}

	@Override
	public void startLearning() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected HypothesisState<I, O, SP, TP> createHypothesisState(HTransition<I,O,SP,TP> treeIncoming) {
		HypothesisState<I,O,SP,TP> state = hypothesis.createState(treeIncoming);
		newStates.add(state);
		
		int numSyms = alphabet.size();
		for(int i = 0; i < numSyms; i++) {
			I sym = alphabet.getSymbol(i);
			HTransition<I, O, SP, TP> trans = new HTransition<I,O,SP,TP>(state, sym, dtree.getRoot());
			state.setTransition(i, trans);
			newTransitions.add(trans);
			openTransitions.offer(trans);
		}
		
		return state;
	}
	
	
	protected void close() {
		HTransition<I,O,SP,TP> curr;
		
		while((curr = openTransitions.poll()) != null) {
			Word<I> as = curr.getAccessSequence();
			DTNode<I,O,SP,TP> leaf = dtree.sift(curr.getDTTarget(), as, oracle);
			if(leaf.getHypothesisState() == null) {
				HypothesisState<I,O,SP,TP> state = createHypothesisState(curr);
				curr.makeTree(state);
			}
			else {
				curr.setDTTarget(leaf);
				leaf.getHypothesisState().addNonTreeIncoming(curr);
			}
		}
	}
	
	protected void updateProperties() {
		List<Query<I,O>> queries = new ArrayList<>();
		
		for(HypothesisState<I,O,SP,TP> s : newStates) {
			Query<I,O> q = stateProperty(s);
			if(q != null)
				queries.add(q);
		}
		
		for(HTransition<I,O,SP,TP> t : newTransitions) {
			Query<I,O> q = transitionProperty(t);
			if(q != null)
				queries.add(q);
		}
		
		if(!queries.isEmpty())
			oracle.processQueries(queries);
	}
	
	
	protected Query<I,O> stateProperty(HypothesisState<I,O,SP,TP> state) {
		return null;
	}
	
	protected Query<I,O> transitionProperty(HTransition<I,O,SP,TP> trans) {
		return null;
	}

}
