package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import javax.annotation.Nonnull;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.tries.SuffixTrie;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

public class TTTLearnerDFA<I> implements DFALearner<I>, AccessSequenceTransformer<I>, SuffixOutput<I, Boolean> {
	
	private final Alphabet<I> alphabet;
	private final TTTHypothesisDFA<I> hypothesis;
	private final MembershipOracle<I, Boolean> oracle;
	
	private final DiscriminationTree<I> dtree;
	private final SuffixTrie<I> suffixTrie = new SuffixTrie<>();
	
	private final Queue<TTTTransitionDFA<I>> openTransitions = new ArrayDeque<>();
	
	private final LocalSuffixFinder<? super I, ? super Boolean> suffixFinder;
	
	private int lastGeneration;
	
	private final BlockList<I> blockList = new BlockList<>();
	
	public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle,
			LocalSuffixFinder<? super I, ? super Boolean> suffixFinder) {
		this.alphabet = alphabet;
		this.hypothesis = new TTTHypothesisDFA<>(alphabet);
		this.oracle = oracle;
		this.dtree = new DiscriminationTree<>(oracle);
		this.suffixFinder = suffixFinder;
	}

	@Override
	public void startLearning() {
		if(hypothesis.isInitialized()) {
			throw new IllegalStateException();
		}
		
		// Initialize epsilon as the root of the discrimination tree
		dtree.getRoot().split(Word.<I>epsilon(), false, null);
		
		boolean initialAccepting = MQUtil.output(oracle, Word.<I>epsilon());
		
		TTTStateDFA<I> init = hypothesis.initialize(initialAccepting);
		
		link(dtree.getRoot().getChild(initialAccepting), init);
		
		
		initializeState(init);
		
		closeTransitions();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		if(!refineHypothesisSingle(ceQuery)) {
			return false;
		}
		
		while(refineHypothesisSingle(ceQuery)) {}
		
		return true;
	}
	

	@Override
	public DFA<?, I> getHypothesisModel() {
		return hypothesis;
	}
	
	
	
	
	private void initializeState(TTTStateDFA<I> state) {
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			TTTTransitionDFA<I> trans = new TTTTransitionDFA<>(state, sym);
			trans.setNonTreeTarget(dtree.getRoot());
			state.transitions[i] = trans;
			openTransitions.offer(trans);
		}
	}
	
	
	private boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery) {
		TTTStateDFA<I> state = getState(ceQuery.getPrefix());
		boolean out = computeHypothesisOutput(state, ceQuery.getSuffix());
		
		if(out == ceQuery.getOutput()) {
			return false;
		}
		
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, this, hypothesis, oracle);
		assert suffixIdx != -1;
		
		Word<I> ceInput = ceQuery.getInput();
		
		Word<I> u = ceInput.prefix(suffixIdx - 1);
		I a = ceInput.getSymbol(suffixIdx - 1);
		int aIdx = alphabet.getSymbolIndex(a);
		Word<I> v = ceInput.subWord(suffixIdx);
		
		TTTStateDFA<I> pred = getState(u);
		TTTTransitionDFA<I> trans = pred.transitions[aIdx];
		
		split(trans, v);
		
		return true;
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
	
	
	private static final class Splitter<I> {
		public final int symbolIdx;
		public final DTNode<I> succSeparator;
		
		public Splitter(int symbolIdx, DTNode<I> succSeparator) {
			assert !succSeparator.isTemp();
			
			this.symbolIdx = symbolIdx;
			this.succSeparator = succSeparator;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Splitter<I> findSplitter(Iterator<? extends TTTStateDFA<I>> statesIt) {
		if(!statesIt.hasNext()) {
			return null;
		}
		
		DTNode<I>[] dtTargets = new DTNode[alphabet.size()];
		
		TTTStateDFA<I> state = statesIt.next();
		
		for(int i = 0; i < dtTargets.length; i++) {
			TTTTransitionDFA<I> trans = state.transitions[i];
			dtTargets[i] = updateDTTarget(trans, false);
		}
		
		while(statesIt.hasNext()) {
			state = statesIt.next();
			
			for(int i = 0; i < dtTargets.length; i++) {
				TTTTransitionDFA<I> trans = state.transitions[i];
				DTNode<I> tgt1 = dtTargets[i];
				DTNode<I> tgt2 = updateDTTarget(trans, false);
				
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
					return new Splitter<>(i, lca);
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

	
	
	private void finalizeDiscriminator(DTNode<I> node, Splitter<I> splitter) {
		assert node.isBlockRoot();
		
		prepareSplit(node, splitter);
		
		DTNode<I> falseSubtree = extractSubtree(node, false);
		DTNode<I> trueSubtree = extractSubtree(node, true);
		
		node.setFalseChild(falseSubtree);
		node.setTrueChild(trueSubtree);
		
		node.temp = false;
		node.removeFromBlockList();
		
		// Register as blocks
		if(falseSubtree.isInner()) {
			blockList.insertBlock(falseSubtree);
		}
		if(trueSubtree.isInner()) {
			blockList.insertBlock(trueSubtree);
		}
	}
	
	private void prepareSplit(DTNode<I> node, Splitter<I> splitter) {
		Deque<DTNode<I>> dfsStack = new ArrayDeque<>();
		
		DTNode<I> succSeparator = splitter.succSeparator;
		int symbolIdx = splitter.symbolIdx;
		I symbol = alphabet.getSymbol(symbolIdx);
		
		Word<I> discriminator = node.getDiscriminator().prepend(symbol);
		
		dfsStack.push(node);
		
		while(!dfsStack.isEmpty()) {
			DTNode<I> curr = dfsStack.pop();
			assert curr.splitData == null;
			
			curr.splitData = new SplitData<>();
			
			
			for(TTTTransitionDFA<I> trans : curr.getIncoming()) {
				boolean outcome = query(trans, discriminator);
				curr.splitData.getIncoming(outcome).insertIncoming(trans);
				markAndPropagate(curr, outcome);
			}
			
			if(node.isInner()) {
				dfsStack.push(curr.getTrueChild());
				dfsStack.push(curr.getFalseChild());
			}
			else {
				TTTStateDFA<I> state = curr.state;
				assert state != null;
				
				// Try to deduct the outcome from the DT target of
				// the respective transition
				TTTTransitionDFA<I> trans = state.transitions[symbolIdx];
				DTNode<I> dtTarget = updateDTTarget(trans, false);
				Boolean succOutcome = succSeparator.subtreeLabel(dtTarget);
				boolean outcome;
				if(succOutcome != null) {
					outcome = succOutcome.booleanValue();
				}
				else {
					// OK, we need to do a membership query here
					outcome = query(state, discriminator);
				}
				curr.splitData.setStateLabel(outcome);
				markAndPropagate(curr, outcome);
			}
		}
	}
	
	private void markAndPropagate(DTNode<I> node, boolean label) {
		DTNode<I> curr = node;
		
		while(curr != null && curr.splitData != null) {
			if(!curr.splitData.mark(label)) {
				return;
			}
			curr = curr.getParent();
		}
	}
	
	private static final class ExtractRecord<I> {
		public final DTNode<I> original;
		public final DTNode<I> extracted;
		
		public ExtractRecord(DTNode<I> original, DTNode<I> extracted) {
			this.original = original;
			this.extracted = extracted;
		}
	}
	
	private DTNode<I> extractSubtree(DTNode<I> root, boolean label) {
		Deque<ExtractRecord<I>> stack = new ArrayDeque<>();
		
		DTNode<I> firstExtracted = new DTNode<>(root, label);
		
		stack.push(new ExtractRecord<>(root, firstExtracted));
		while(!stack.isEmpty()) {
			ExtractRecord<I> curr = stack.pop();
			
			DTNode<I> original = curr.original;
			DTNode<I> extracted = curr.extracted;
			
			moveIncoming(extracted, original);
			
			if(original.isLeaf()) {
				if(original.splitData.getStateLabel() == label) {
					link(extracted, original.state);
				}
				else {
					createNewState(extracted);
				}
				extracted.updateIncoming();
			}
			else {
				DTNode<I> falseChild = original.getFalseChild();
				DTNode<I> trueChild = original.getTrueChild();
				
				boolean falseChildMarked = falseChild.splitData.isMarked(label);
				boolean trueChildMarked = trueChild.splitData.isMarked(label);
				
				if(falseChildMarked && trueChildMarked) {
					extracted.split(original.getDiscriminator());
					DTNode<I> falseChildExtracted = extracted.getFalseChild();
					DTNode<I> trueChildExtracted = extracted.getTrueChild();
					extracted.updateIncoming();
					
					stack.push(new ExtractRecord<>(falseChild, falseChildExtracted));
					stack.push(new ExtractRecord<>(trueChild, trueChildExtracted));
				}
				else if(falseChildMarked) { // && !trueChildMarked
					stack.push(new ExtractRecord<>(falseChild, extracted));
				}
				else if(trueChildMarked) { // && !falseChildMarked
					stack.push(new ExtractRecord<>(trueChild, extracted));
				}
				else { // !falseChildMarked && !trueChildMarked
					// No leaves in this extracted subtree, but incoming transitions
					// induce new state
					createNewState(extracted);
					extracted.updateIncoming();
				}
			}	
		}
		
		return firstExtracted;
	}
	
	private void moveIncoming(DTNode<I> newNode, DTNode<I> oldNode) {
		newNode.getIncoming().insertAllIncoming(oldNode.getIncoming());
	}
	
	
	private void createNewState(DTNode<I> newNode) {
		TTTTransitionDFA<I> newTreeTrans = newNode.getIncoming().choose();
		assert newTreeTrans != null;
		
		boolean accepting = dtree.getRoot().subtreeLabel(newNode).booleanValue();
		TTTStateDFA<I> newState = hypothesis.createState(newTreeTrans, accepting);
		
		link(newNode, newState);
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
	
	private DTNode<I> splitState(TTTTransitionDFA<I> transition, Word<I> tempDiscriminator, boolean newOut) {
		assert !transition.isTree();
		
		DTNode<I> dtNode = transition.getNonTreeTarget();
		TTTStateDFA<I> oldState = dtNode.state;
		assert oldState != null;
		
		TTTStateDFA<I> newState = createState(transition, oldState.isAccepting());
		
		dtNode.split(tempDiscriminator, newOut, newState);
		
		if(isOld(oldState)) {
			for(TTTTransitionDFA<I> incoming : dtNode.getIncoming()) {
				openTransitions.offer(incoming);
			}
		}
		
		return dtNode;
	}
	
	
	private boolean isOld(@Nonnull TTTStateDFA<I> state) {
		return state.id < lastGeneration;
	}

	private void closeTransitions() {
		while(!openTransitions.isEmpty()) {
			TTTTransitionDFA<I> trans = openTransitions.poll();
			closeTransition(trans);
		}
		this.lastGeneration = hypothesis.size();
	}
	
	private void closeTransition(TTTTransitionDFA<I> trans) {
		if(trans.isTree()) {
			return;
		}
		
		DTNode<I> dtTarget = updateDTTarget(trans);

		if(dtTarget.state == null) {
			TTTStateDFA<I> state = createState(trans, dtTarget.getParentEdgeLabel());
			link(dtTarget, state);
		}
	}
	
	private DTNode<I> updateDTTarget(TTTTransitionDFA<I> transition) {
		return updateDTTarget(transition, true);
	}
	
	private DTNode<I> updateDTTarget(TTTTransitionDFA<I> transition, boolean hard) {
		if(transition.isTree()) {
			return transition.getTreeTarget().dtLeaf;
		}
		
		DTNode<I> dt = transition.getNonTreeTarget();
		dt = dtree.sift(dt, transition, hard);
		transition.setNonTreeTarget(dt);
		
		return dt;
	}
	
	
	private boolean query(Word<I> prefix, Word<I> suffix) {
		return MQUtil.output(oracle, prefix, suffix);
	}
	
	private boolean query(AccessSequenceProvider<I> accessSeqProvider, Word<I> suffix) {
		return query(accessSeqProvider.getAccessSequence(), suffix);
	}

}
