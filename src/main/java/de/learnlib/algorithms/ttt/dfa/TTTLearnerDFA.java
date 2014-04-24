package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import javax.annotation.Nonnull;

import net.automatalib.automata.concepts.SuffixOutput;
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
	// private final SuffixTrie<I> suffixTrie = new SuffixTrie<>();
	
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
		
		DefaultQuery<I, Boolean> currCe = ceQuery;
		
		while(currCe != null) {
			while(refineHypothesisSingle(currCe)) {}
		
			currCe = checkHypothesisConsistency();
		}
		return true;
	}
	

	@Override
	public TTTHypothesisDFA<I> getHypothesisModel() {
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
		
		splitState(trans, v);
		
		while(repair()) {}
		
		closeTransitions();
		
		return true;
	}
	
	private boolean finalizeAny() {
		GlobalSplitter<I> splitter = findSplitterGlobal();
		if(splitter != null) {
			finalizeDiscriminator(splitter.blockRoot, splitter.localSplitter);
			return true;
		}
		return false;
	}
	
	private boolean repair() {
		while(finalizeAny()) {}
		if(blockList.isEmpty()) {
			return false;
		}
		DTNode<I> blockRoot = blockList.chooseBlock();
		makeConsistent(blockRoot);
		return true;
	}
	
	
	private void makeConsistent(DTNode<I> blockRoot) {
		DTNode<I> separator = blockRoot.getExtremalChild(false).getParent();
		Word<I> discriminator = separator.getDiscriminator();
		
		DTNode<I> falseChild = separator.getFalseChild();
		if(ensureConsistency(falseChild.state, discriminator, false)) {
			return;
		}
		DTNode<I> trueChild = separator.getTrueChild().getExtremalChild(false);
		boolean wasInconsistent = ensureConsistency(trueChild.state, discriminator, true);
		
		
		assert wasInconsistent;
//		if(!wasInconsistent) {
//			try(Writer w = DOT.createDotWriter(true)) {
//				GraphDOT.write(hypothesis, alphabet, w);
//			}
//			catch(IOException ex) {
//				throw new AssertionError(ex);
//			}
//		}
	}
	
	
	private boolean ensureConsistency(TTTStateDFA<I> state, Word<I> suffix, boolean realOutcome) {
		boolean hypOutcome = computeHypothesisOutput(state, suffix);
		if(hypOutcome == realOutcome) {
			return false;
		}
		DefaultQuery<I, Boolean> query = new DefaultQuery<>(state.getAccessSequence(), suffix, realOutcome);
		
		while(refineHypothesisSingle(query)) {}
		
		return true;
	}
	
	
	private static final class Splitter<I> {
		public final int symbolIdx;
		public final DTNode<I> succSeparator;
		
		public Splitter(int symbolIdx, DTNode<I> succSeparator) {
			assert !succSeparator.isTemp() && succSeparator.isInner();
			
			this.symbolIdx = symbolIdx;
			this.succSeparator = succSeparator;
		}
	}
	
	private static final class GlobalSplitter<I> {
		public final Splitter<I> localSplitter;
		public final DTNode<I> blockRoot;
		
		public GlobalSplitter(DTNode<I> blockRoot, Splitter<I> localSplitter) {
			this.blockRoot = blockRoot;
			this.localSplitter = localSplitter;
		}
	}
	
	private GlobalSplitter<I> findSplitterGlobal() {
		boolean optimizeGlobal = true;
		
		DTNode<I> bestBlockRoot = null;
		
		Splitter<I> bestSplitter = null;
		
		Iterator<DTNode<I>> blocksIt = blockList.iterator();
		while(blocksIt.hasNext()) {
			DTNode<I> blockRoot = blocksIt.next();
			Splitter<I> splitter = findSplitter(blockRoot);
			if(splitter != null) {
				if(bestSplitter == null || splitter.succSeparator.getDiscriminator().length()
						< bestSplitter.succSeparator.getDiscriminator().length()) {
					bestSplitter = splitter;
					bestBlockRoot = blockRoot;
				}
				
				if(!optimizeGlobal) {
					break;
				}
			}
		}
		
		if(bestSplitter == null) {
			return null;
		}
		
		return new GlobalSplitter<>(bestBlockRoot, bestSplitter);
	}
	
	@SuppressWarnings("unchecked")
	private Splitter<I> findSplitter(DTNode<I> blockRoot) {
		boolean optimizeLocal = true;
		
		Iterator<TTTStateDFA<I>> statesIt = blockRoot.subtreeStatesIterator();
		
		assert statesIt.hasNext();
		
		DTNode<I>[] dtTargets = new DTNode[alphabet.size()];
		
		TTTStateDFA<I> state = statesIt.next();
		
		for(int i = 0; i < dtTargets.length; i++) {
			TTTTransitionDFA<I> trans = state.transitions[i];
			dtTargets[i] = updateDTTarget(trans, false);
		}
		
		assert statesIt.hasNext();
		
		int bestI = -1;
		DTNode<I> bestLCA = null;
		
		while(statesIt.hasNext()) {
			state = statesIt.next();
			
			for(int i = 0; i < dtTargets.length; i++) {
				TTTTransitionDFA<I> trans = state.transitions[i];
				DTNode<I> tgt1 = dtTargets[i];
				DTNode<I> tgt2 = updateDTTarget(trans, false);
				
				
				DTNode<I> lca = dtree.leastCommonAncestor(tgt1, tgt2);
				if(!lca.isTemp() && lca.isInner()) {
					if(!optimizeLocal) {
						return new Splitter<>(i, lca);
					}
					if(bestLCA == null || bestLCA.getDiscriminator().length() > lca.getDiscriminator().length()) {
						bestI = i;
						bestLCA = lca;
					}
					dtTargets[i] = lca;
				}
				else {
					dtTargets[i] = lca;
				}
			}
		}
		
		if(bestLCA == null) {
			return null;
		}
		return new Splitter<>(bestI, bestLCA);
	}
	
	private DefaultQuery<I, Boolean> checkHypothesisConsistency() {
		for(DTNode<I> leaf : dtree.getRoot().subtreeLeaves()) {
			TTTStateDFA<I> state = leaf.state;
			if(state == null) {
				continue;
			}
			
			DTNode<I> curr = state.dtLeaf;
			DTNode<I> next = curr.getParent();
			
			while(next != null) {
				Word<I> discr = next.getDiscriminator();
				boolean expected = curr.getParentEdgeLabel();
				
				if(computeHypothesisOutput(state, discr) != expected) {
					return new DefaultQuery<>(state.getAccessSequence(), discr, expected);
				}
				curr = next;
				next = curr.getParent();
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
		
		Word<I> finalDiscriminator = prepareSplit(node, splitter);
		
		DTNode<I> falseSubtree = extractSubtree(node, false);
		DTNode<I> trueSubtree = extractSubtree(node, true);
		
		node.setFalseChild(falseSubtree);
		node.setTrueChild(trueSubtree);
		
		node.temp = false;
		node.setDiscriminator(finalDiscriminator);
		node.removeFromBlockList();
		
		// Register as blocks
		if(falseSubtree.isInner()) {
			blockList.insertBlock(falseSubtree);
		}
		if(trueSubtree.isInner()) {
			blockList.insertBlock(trueSubtree);
		}
	}
	
	private Word<I> prepareSplit(DTNode<I> node, Splitter<I> splitter) {
		Deque<DTNode<I>> dfsStack = new ArrayDeque<>();
		
		DTNode<I> succSeparator = splitter.succSeparator;
		int symbolIdx = splitter.symbolIdx;
		I symbol = alphabet.getSymbol(symbolIdx);
		
		Word<I> discriminator = succSeparator.getDiscriminator().prepend(symbol);
		
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
			
			if(curr.isInner()) {
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
		
		return discriminator;
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
		assert root.splitData != null && root.splitData.isMarked(label);
		
		Deque<ExtractRecord<I>> stack = new ArrayDeque<>();
		
		DTNode<I> firstExtracted = new DTNode<>(root, label);
		
		stack.push(new ExtractRecord<>(root, firstExtracted));
		while(!stack.isEmpty()) {
			ExtractRecord<I> curr = stack.pop();
			
			DTNode<I> original = curr.original;
			DTNode<I> extracted = curr.extracted;
			
			moveIncoming(extracted, original, label);
			
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
					extracted.temp = true;
					
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
	
	private void moveIncoming(DTNode<I> newNode, DTNode<I> oldNode, boolean label) {
		newNode.getIncoming().insertAllIncoming(oldNode.splitData.getIncoming(label));
	}
	
	
	private void createNewState(DTNode<I> newNode) {
		TTTTransitionDFA<I> newTreeTrans = newNode.getIncoming().choose();
		assert newTreeTrans != null;
		
		boolean accepting = dtree.getRoot().subtreeLabel(newNode).booleanValue();
		
		TTTStateDFA<I> newState = createState(newTreeTrans, accepting);
		
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
	
	private DTNode<I> splitState(TTTTransitionDFA<I> transition, Word<I> tempDiscriminator) {
		assert !transition.isTree();
		
		DTNode<I> dtNode = transition.getNonTreeTarget();
		TTTStateDFA<I> oldState = dtNode.state;
		assert oldState != null;
		
		TTTStateDFA<I> newState = createState(transition, oldState.isAccepting());
		
		boolean newOut = query(transition, tempDiscriminator);
		
		dtNode.split(tempDiscriminator, newOut, newState);
		
		link(dtNode.getChild(!newOut), oldState);
		link(dtNode.getChild(newOut), newState);
		
		if(isOld(oldState)) {
			for(TTTTransitionDFA<I> incoming : dtNode.getIncoming()) {
				openTransitions.offer(incoming);
			}
		}
		
		dtNode.temp = true;
		
		if(!dtNode.getParent().isTemp()) {
			blockList.insertBlock(dtNode);
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
	
	public DiscriminationTree<I> getDiscriminationTree() {
		return dtree;
	}

}
