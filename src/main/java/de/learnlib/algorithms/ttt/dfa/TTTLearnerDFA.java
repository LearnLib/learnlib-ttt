package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.tries.SuffixTrie;
import net.automatalib.util.tries.SuffixTrieNode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class TTTLearnerDFA<I> implements DFALearner<I>, AccessSequenceTransformer<I>, SuffixOutput<I, Boolean> {
	
	private final Alphabet<I> alphabet;
	private final TTTHypothesisDFA<I> hypothesis;
	private final MembershipOracle<I, Boolean> oracle;
	private final DiscriminationTree<I> dtree;
	private final SuffixTrie<I> suffixTrie = new SuffixTrie<>();
	
	private final Queue<TTTTransitionDFA<I>> openTransitions = new ArrayDeque<>();
	
	private final LocalSuffixFinder<? super I, ? super Boolean> suffixFinder = LocalSuffixFinders.RIVEST_SCHAPIRE;

	public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
		this.alphabet = alphabet;
		this.hypothesis = new TTTHypothesisDFA<>(alphabet);
		this.oracle = oracle;
		this.dtree = new DiscriminationTree<>(oracle);
	}

	@Override
	public void startLearning() {
		if(hypothesis.isInitialized()) {
			throw new IllegalStateException();
		}
		
		boolean initialAccepting = MQUtil.output(oracle, Word.<I>epsilon());
		
		TTTStateDFA<I> init = hypothesis.initialize(initialAccepting);
		
		initializeState(init);
		
		closeTransitions();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		boolean refined = false;
		
		while(isCounterexample(ceQuery)) {
			doRefine(ceQuery);
			refined = true;
		}
		
		return refined;
	}
	

	@Override
	public DFA<?, I> getHypothesisModel() {
		return hypothesis;
	}
	
	
	private void initializeState(TTTStateDFA<I> state) {
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			TTTTransitionDFA<I> trans = new TTTTransitionDFA<>(state, sym);
			trans.nonTreeTarget = dtree.getRoot();
			state.transitions[i] = trans;
			openTransitions.add(trans);
		}
	}
	
	
	private void closeTransitions() {
		while(!openTransitions.isEmpty()) {
			TTTTransitionDFA<I> trans = openTransitions.poll();
			closeTransition(trans);
		}
	}
	
	private DTNode<I> closeTransition(TTTTransitionDFA<I> trans) {
		if(trans.isTree()) {
			return trans.treeTarget.dtLeaf;
		}
		
		DTNode<I> dtTarget = updateDTTarget(trans);
		
		// new state - this can only happen if 
		if(dtTarget.state == null) {
			TTTStateDFA<I> state = createState(trans, dtTarget.getParentEdgeLabel());
			link(dtTarget, state);
		}
		
		return dtTarget;
	}
	
	
	private void refine(DefaultQuery<I,Boolean> ceQuery) {
		while(isCounterexample(ceQuery)) {
			doRefine(ceQuery);
		}
	}
	
	private void doRefine(DefaultQuery<I, Boolean> ceQuery) {
		Word<I> ceInput = ceQuery.getInput();
		
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, this, this, oracle);
		
		
		// Decompose CE
		Word<I> u = ceInput.prefix(suffixIdx-1);
		I a = ceInput.getSymbol(suffixIdx - 1);
		Word<I> v = ceInput.subWord(suffixIdx);
		
	}
	
	/* 
	 * Consistency check
	 */
	
	private static final class CheckConsistencyRecord<I> {
		public final DTNode<I> node;
		public boolean value;
		
		public CheckConsistencyRecord(DTNode<I> node) {
			this.node = node;
			this.value = false;
		}
	}
	
	private DefaultQuery<I, Boolean> checkConsistency() {
		DTNode<I> current = dtree.getRoot();
		Deque<CheckConsistencyRecord<I>> stack = new ArrayDeque<>();
		
		while(current != null) {
			if(current.isInner()) {
				CheckConsistencyRecord<I> rec = new CheckConsistencyRecord<>(current);
				stack.push(rec);
				current = current.getChild(rec.value);
			}
			
			if(current.isLeaf()) {
				DefaultQuery<I, Boolean> ce = checkStateConsistency(current.state, stack);
				if(ce != null) {
					return ce;
				}
				
				current = null;
				while(!stack.isEmpty() && current == null) {
					CheckConsistencyRecord<I> rec = stack.pop();
					if(!rec.value) {
						rec.value = true;
						current = rec.node.getChild(rec.value);
						stack.push(rec);
					}
				}
			}
		}
		
		return null;
	}
	
	private Word<I> findSplitter(Collection<? extends TTTStateDFA<I>> states) {
		Iterator<? extends TTTStateDFA<I>> statesIt = states.iterator();
		
		if(!statesIt.hasNext()) {
			return null;
		}
		
		DTNode<I>[] dtTargets = new DTNode[alphabet.size()];
		
		TTTStateDFA<I> state = statesIt.next();
		
		for(int i = 0; i < dtTargets.length; i++) {
			TTTTransitionDFA<I> trans = state.transitions[i];
			dtTargets[i] = trans.getDTTarget();
		}
		
		while(statesIt.hasNext()) {
			state = statesIt.next();
			
			for(int i = 0; i < dtTargets.length; i++) {
				TTTTransitionDFA<I> trans = state.transitions[i];
				DTNode<I> tgt1 = dtTargets[i];
				DTNode<I> tgt2 = trans.getDTTarget();
				
				// Make sure tgt1.depth <= tgt2.depth
				if(tgt1.getDepth() > tgt2.getDepth()) {
					DTNode<I> tmp = tgt1;
					tgt1 = tgt2;
					tgt2 = tmp;
				}
				
				DTNode<I> lca = dtree.leastCommonAncestor(tgt1, tgt2);
				
				if(lca == tgt1) {
					dtTargets[i] = tgt2;
				}
				else if(!lca.isTemp()) {
					I sym = alphabet.getSymbol(i);
					SuffixTrieNode<I> succDiscr = (SuffixTrieNode<I>)lca.getDiscriminator();
					return suffixTrie.add(sym, succDiscr);
				}
			}
		}
		
		return null;
	}
	
	private DefaultQuery<I, Boolean> checkStateConsistency(TTTStateDFA<I> state, Collection<? extends CheckConsistencyRecord<I>> checks) {
		for(CheckConsistencyRecord<I> rec : checks) {
			Word<I> suffix = rec.node.getDiscriminator();
			boolean expected = rec.value;
			
			boolean actual = computeHypothesisOutput(state, suffix);
			if(expected != actual) {
				Word<I> prefix = state.getAccessSequence();
				DefaultQuery<I, Boolean> ce = new DefaultQuery<>(prefix, suffix, expected);
				return ce;
			}
		}
		
		return null;
	}
	
	
	private TTTStateDFA<I> createState(TTTTransitionDFA<I> transition, boolean accepting) {
		TTTStateDFA<I> newState = hypothesis.createState(transition, accepting);
		initializeState(newState);
		
		return newState;
	}
	
	private DTNode<I> updateDTTarget(TTTTransitionDFA<I> transition) {
		DTNode<I> dt = transition.nonTreeTarget;
		dt = dtree.sift(dt, transition.getAccessSequence());
		transition.nonTreeTarget = dt;
		
		return dt;
	}
	
	private TTTStateDFA<I> getTarget(TTTTransitionDFA<I> trans) {
		if(trans.isTree()) {
			return trans.getTreeTarget();
		}
		DTNode<I> dtTarget = updateDTTarget(trans);
		return dtTarget.state;
	}
	
	
	private boolean computeHypothesisOutput(TTTStateDFA<I> state, Iterable<? extends I> suffix) {
		TTTStateDFA<I> endState = getState(state, suffix);
		return endState.accepting;
	}
	
	private TTTStateDFA<I> getState(TTTStateDFA<I> start, Iterable<? extends I> suffix) {
		TTTStateDFA<I> curr = start;
		
		for(I sym : suffix) {
			TTTTransitionDFA<I> trans = hypothesis.getInternalTransition(curr, sym);
			curr = getTarget(trans);
		}
		
		return curr;
	}
	
	private TTTStateDFA<I> getState(Iterable<? extends I> suffix) {
		return getState(hypothesis.getInitialState(), suffix);
	}

	
	private boolean isCounterexample(DefaultQuery<I, Boolean> ce) {
		TTTStateDFA<I> prefixState = getState(ce.getPrefix());
		
		boolean hypOut = computeHypothesisOutput(prefixState, ce.getSuffix());
		
		return (ce.getOutput().booleanValue() != hypOut);
	}
	
	private static <I> void link(DTNode<I> dtNode, TTTStateDFA<I> state) {
		assert dtNode.isLeaf();
		
		dtNode.state = state;
		state.dtLeaf = dtNode;
	}

	@Override
	public Boolean computeOutput(Iterable<? extends I> input) {
		return computeHypothesisOutput(hypothesis.getInitialState(), input);
	}

	@Override
	public Boolean computeSuffixOutput(Iterable<? extends I> prefix,
			Iterable<? extends I> suffix) {
		TTTStateDFA<I> prefixState = getState(prefix);
		return computeHypothesisOutput(prefixState, suffix);
	}

	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		return getState(word).getAccessSequence();
	}

	@Override
	public boolean isAccessSequence(Word<I> word) {
		TTTStateDFA<I> curr = hypothesis.getInitialState();
		for(I sym : word) {
			TTTTransitionDFA<I> trans = hypothesis.getInternalTransition(curr, sym);
			if(!trans.isTree()) {
				return false;
			}
		}
		
		return true;
	}

}
