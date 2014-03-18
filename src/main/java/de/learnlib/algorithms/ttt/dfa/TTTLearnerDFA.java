package de.learnlib.algorithms.ttt.dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.misberner.jdtree.binary.BDTEvaluator;
import com.github.misberner.jdtree.binary.BDTNode;
import com.github.misberner.jdtree.binary.BDTNodeMap;
import com.github.misberner.jdtree.binary.BinaryDTree;

import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.tries.SuffixTrie;
import net.automatalib.util.tries.SuffixTrieNode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class TTTLearnerDFA<I> implements DFALearner<I> {
	
	private static final class PrefixEvaluator<I> implements BDTEvaluator<Word<I>,Word<I>> {
		private final MembershipOracle<I, Boolean> oracle;
		
		public PrefixEvaluator(MembershipOracle<I, Boolean> oracle) {
			this.oracle = oracle;
		}
		@Override
		public boolean evaluate(Word<I> object, Word<I> discriminator) {
			return MQUtil.output(oracle, object, discriminator).booleanValue();
		}
		
	}
	
	private final class ReplaceMarkEvaluator implements BDTEvaluator<BDTNode<Word<I>>,Word<I>> {

		@Override
		public boolean evaluate(BDTNode<Word<I>> leaf, Word<I> discriminator) {
			TTTStateDFA<I> state = getStateForLeaf(leaf);
			return state.replaceMark;
		}
	}
	
	private static final Logger LOG = Logger.getLogger(TTTLearnerDFA.class.getName());
	
	private final Alphabet<I> alphabet;
	private final MembershipOracle<I, Boolean> oracle;
	
	private final BinaryDTree<Word<I>> discriminationTree;
	private final TTTHypothesisDFA<I> hypothesis;
	
	private final BDTEvaluator<Word<I>, Word<I>> wordEval;
	private final BDTEvaluator<BDTNode<Word<I>>,Word<I>> replaceMarkEval
		= new ReplaceMarkEvaluator();
	
	private final SuffixTrie<I> suffixTrie = new SuffixTrie<>(true);
	
	private final Deque<TTTTransitionDFA<I>> openTransitions
		= new ArrayDeque<>();
		
	private final LocalSuffixFinder<? super I, ? super Boolean> suffixFinder
		= LocalSuffixFinders.RIVEST_SCHAPIRE;

	public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.discriminationTree = new BinaryDTree<>();
		
		this.wordEval = new PrefixEvaluator<>(oracle);
		this.hypothesis = new TTTHypothesisDFA<I>(alphabet);
	}

	@Override
	public void startLearning() {
		if(wasStarted()) {
			throw new IllegalStateException("startLearning has already been called");
		}
		
		LOG.fine("Starting learning ...");
		
		initialize();
		closeTransitions();
	}
	
	
	@Override
	public DFA<?,I> getHypothesisModel() {
		return hypothesis;
	}
	
	protected static final class StatePair<I> {
		public final TTTStateDFA<I> state1;
		public final TTTStateDFA<I> state2;
		
		public StatePair(TTTStateDFA<I> state1, TTTStateDFA<I> state2) {
			this.state1 = state1;
			this.state2 = state2;
		}
	}
	
	protected static final class CEAnalysis<I> {
		public final TTTTransitionDFA<I> transition;
		public final Word<I> suffix;
		
		public CEAnalysis(TTTTransitionDFA<I> transition, Word<I> suffix) {
			this.transition = transition;
			this.suffix = suffix;
		}
	}
	
	protected CEAnalysis<I> analyze(Query<I, Boolean> query) {
		int suffixIdx = suffixFinder.findSuffixIndex(query, hypothesis, hypothesis, oracle);
		Word<I> input = query.getInput();
		Word<I> suffix = input.subWord(suffixIdx);
		
		I sym = input.getSymbol(suffixIdx - 1);
		int symIdx = alphabet.getSymbolIndex(sym);
		
		Word<I> sourcePrefix = input.prefix(suffixIdx - 1);
		
		TTTStateDFA<I> sourceState = hypothesis.getState(sourcePrefix);
		TTTTransitionDFA<I> trans = sourceState.transitions[symIdx];
		
		return new CEAnalysis<>(trans, suffix);
	}
	
	
	protected boolean checkState(TTTStateDFA<I> state, Word<I> suffix, boolean expectedOutput) {
		boolean hypOut = hypothesis.getSuccessor(state, suffix).accepting;
		
		if(hypOut == expectedOutput) {
			return false;
		}
		
		DefaultQuery<I, Boolean> ceQuery = new DefaultQuery<>(state.getAccessSequence(), suffix, expectedOutput);
		
		CEAnalysis<I> analysis = analyze(ceQuery);
		
		TTTTransitionDFA<I> transition = analysis.transition;
		
		TTTStateDFA<I> oldState = getTransitionTarget(transition);
		TTTStateDFA<I> newState = splitState(transition, suffix);
		
		fixDiscriminator(oldState, newState);
		
		return true;
	}
	
	protected BDTNode<Word<I>> fixDiscriminator(TTTStateDFA<I> state1, TTTStateDFA<I> state2) {
		if(state1 == state2) {
			return null;
		}
		
		BDTNode<Word<I>> leaf1 = getLeafForState(state1);
		BDTNode<Word<I>> leaf2 = getLeafForState(state2);
		
		LCAI
		
		BDTNode<Word<I>> separator = discriminationTree.leastCommonAncestor(leaf1, leaf2);
		
		if(!isTempSeparator(separator)) {
			return separator;
		}
		
		I sym = suffix.firstSymbol();
		
		TTTStateDFA<I> succ1 = hypothesis.getSuccessor(state1, sym);
		TTTStateDFA<I> succ2 = hypothesis.getSuccessor(state2, sym);
		
		BDTNode<Word<I>> succSeparator = finalizeDiscriminator(succ1, succ2, suffix.subWord(1));
		
		assert succSeparator != null;
		assert !isTempSeparator(succSeparator);
		
		// Might have changed, so update it
		leaf1 = getLeafForState(state1);
		leaf2 = getLeafForState(state2);
		
		separator = discriminationTree.leastCommonAncestor(leaf1, leaf2);
		
		if(!isTempSeparator(separator)) {
			return separator;			
		}
		
		Word<I> succDiscriminator = succSeparator.getDiscriminator();
		
		Word<I> newDiscriminator = newDiscriminator(sym, succDiscriminator);
		
		replaceDiscriminator(separator, newDiscriminator);
		
		return separator;
	}
	
	protected boolean refineHypothesisSimple(DefaultQuery<I, Boolean> ceQuery) {
		Deque<StatePair<I>> splits = new ArrayDeque<>();
		
		while(isCounterexample(ceQuery)) {
			
		}
	}
	
	
	protected BDTNode<Word<I>> getLeafForState(TTTStateDFA<I> state) {
		return discriminationTree.getLeaf(state.id);
	}
	
	protected TTTStateDFA<I> getStateForLeaf(BDTNode<Word<I>> leaf) {
		return hypothesis.getState(leaf.getLeafId());
	}
	
	protected TTTStateDFA<I> updateTransition(TTTTransitionDFA<I> transition) {
		if(transition.isTree()) {
			return transition.getTreeTarget();
		}
		BDTNode<Word<I>> dtTarget = transition.dtTarget;
		TTTStateDFA<I> target;
		if(dtTarget.isInner()) {
			dtTarget = discriminationTree.sift(dtTarget, transition.getAccessSequence(), wordEval);
			target = getStateForLeaf(dtTarget);
			if(target == null) {
				target = createStateSpecial(transition);
			}
		}
		else {
			target = getStateForLeaf(dtTarget);
		}
		
		return target;
	}
	
	
	protected void ensureTransitionsDefined(TTTStateDFA<I> state, Word<I> suffix) {
		TTTStateDFA<I> currentState = state;
		for(I sym : suffix) {
			int symIdx = alphabet.getSymbolIndex(sym);
			
			TTTTransitionDFA<I> trans = currentState.transitions[symIdx];
			
			TTTStateDFA<I> nextState;
			if(!trans.isTree()) {
				BDTNode<Word<I>> dtTarget = trans.dtTarget;
				if(!dtTarget.isLeaf()) {
					BDTNode<Word<I>> newDtTarget = discriminationTree.sift(dtTarget, trans.getAccessSequence(), wordEval);
					trans.dtTarget = newDtTarget;
					nextState = getStateForLeaf(newDtTarget);
					nextState.addIncoming(trans);
				}
				nextState = getStateForLeaf(dtTarget);
			}
			else {
				nextState = trans.getTreeTarget();
			}
			
			currentState = nextState;
		}
	}
	
	
	
	
	
	protected void finalizeDiscriminator(BDTNode<Word<I>> separator, Word<I> finalDiscriminator) {
		List<TTTStateDFA<I>> states = new ArrayList<>();
		for(BDTNode<Word<I>> leaf : discriminationTree.subtreeLeaves(separator)) {
			TTTStateDFA<I> state = getStateForLeaf(leaf);
			states.add(state);
			TTTStateDFA<I> altState = null;
			
			boolean subtree = MQUtil.output(oracle, state.getAccessSequence(), finalDiscriminator).booleanValue();
			state.replaceMark = subtree;
			
			List<TTTTransitionDFA<I>> incoming = state.fetchIncoming();
			for(TTTTransitionDFA<I> trans : incoming) {
				boolean transSubtree = MQUtil.output(oracle, trans.getAccessSequence(), finalDiscriminator).booleanValue();
				if(transSubtree != subtree) {
					if(altState == null) {
						discriminationTree.split(leaf, null, subtree);
						altState = createState(trans, state.isAccepting());
						altState.replaceMark = transSubtree;
						states.add(altState);
					}
					else {
						altState.addIncoming(trans);
					}
				}
				else {
					state.addIncoming(trans);
				}
			}
		}
		
		discriminationTree.replaceDiscriminator(separator, finalDiscriminator, replaceMarkEval);
		
		for(TTTStateDFA<I> state : states) {
			BDTNode<Word<I>> leaf = getLeafForState(leaf);
			state.updateIncomingDT(leaf);
		}
	}
	
	protected BDTNode<Word<I>> finalizeDiscriminator(TTTStateDFA<I> state1, TTTStateDFA<I> state2, Word<I> suffix) {
		if(state1 == state2) {
			return null;
		}
		
		BDTNode<Word<I>> leaf1 = getLeafForState(state1);
		BDTNode<Word<I>> leaf2 = getLeafForState(state2);
		
		BDTNode<Word<I>> separator = discriminationTree.leastCommonAncestor(leaf1, leaf2);
		
		if(!isTempSeparator(separator)) {
			return separator;
		}
		
		I sym = suffix.firstSymbol();
		
		TTTStateDFA<I> succ1 = hypothesis.getSuccessor(state1, sym);
		TTTStateDFA<I> succ2 = hypothesis.getSuccessor(state2, sym);
		
		BDTNode<Word<I>> succSeparator = finalizeDiscriminator(succ1, succ2, suffix.subWord(1));
		
		assert succSeparator != null;
		assert !isTempSeparator(succSeparator);
		
		// Might have changed, so update it
		leaf1 = getLeafForState(state1);
		leaf2 = getLeafForState(state2);
		
		separator = discriminationTree.leastCommonAncestor(leaf1, leaf2);
		
		if(!isTempSeparator(separator)) {
			return separator;			
		}
		
		Word<I> succDiscriminator = succSeparator.getDiscriminator();
		
		Word<I> newDiscriminator = newDiscriminator(sym, succDiscriminator);
		
		replaceDiscriminator(separator, newDiscriminator);
		
		return separator;
	}
	
	private boolean isTempSeparator(BDTNode<Word<I>> innerNode) {
		Word<I> discr = innerNode.getDiscriminator();
		return !(discr instanceof SuffixTrieNode);
	}
	
	private BDTNode<Word<I>> getLeafForState(TTTStateDFA<I> state) {
		int id = state.getId();
		return discriminationTree.getLeaf(id);
	}
	
	private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
		if(succDiscriminator instanceof SuffixTrieNode) {
			SuffixTrieNode<I> trieNode = (SuffixTrieNode<I>)succDiscriminator;
			return suffixTrie.add(symbol, trieNode);
		}
		throw new IllegalArgumentException();
	}
	
	
	public boolean refineSimple(DefaultQuery<I,Boolean> ceQuery) {

		
		while (!hypothesis.computeSuffixOutput(ceQuery.getPrefix(),
				ceQuery.getSuffix()).equals(ceQuery.getOutput())) {
			Word<I> input = ceQuery.getInput();
			
			int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, hypothesis,
					hypothesis, oracle);
			assert suffixIdx >= 0;
			

			Word<I> oldPrefix = input.prefix(suffixIdx);
			TTTStateDFA<I> oldState = hypothesis.getState(oldPrefix);

			Word<I> sourcePrefix = input.prefix(suffixIdx - 1);
			TTTStateDFA<I> sourceState = hypothesis.getState(sourcePrefix);
			I sym = input.getSymbol(suffixIdx - 1);

			Word<I> suffix = input.subWord(suffixIdx);

			TTTTransitionDFA<I> trans = hypothesis.getInternalTransition(
					sourceState, sym);

			TTTStateDFA<I> newState = splitStateSimple(oldState, trans, suffix);

			//stack.offer(new SplitPair<>(suffixIdx, oldState, newState));
			//splitPairs.add(new SplitPair<>(suffixIdx, oldState, newState));
			
//			Iterator<SplitPair<I>> it = splitPairs.iterator();
//			while(it.hasNext()) {
//				SplitPair<I> pair = it.next();
//
//				BDTNode<Word<I>> separator = discriminationTree
//						.leastCommonAncestor(pair.state1.dtLeaf, pair.state2.dtLeaf);
//
//				Word<I> discriminator = separator.getDiscriminator();
//				LOG.info("Attempting to replace discriminator " + discriminator);
//
//				SuffixTrieNode<I> newSuffix = findSuffixReplacement(pair.state1,
//						pair.state2);
//				
//				if (newSuffix == null) {
//					break;
//				}
//				
//				it.remove();
//				
//
//				replaceDiscriminator(separator, newSuffix);
//			}
			
			while(!stack.isEmpty()) {
				//visualize();
				SplitPair<I> pair = stack.element();

				BDTNode<Word<I>> separator = discriminationTree
						.leastCommonAncestor(pair.state1.dtLeaf, pair.state2.dtLeaf);

				Word<I> discriminator = separator.getDiscriminator();
				LOG.info("Attempting to replace discriminator " + discriminator);

				SuffixTrieNode<I> newSuffix = findSuffixReplacement(pair.state1,
						pair.state2);
				
				if (newSuffix == null) {
					break;
				}
				stack.poll();
				

				replaceDiscriminator(separator, newSuffix);
			}
		}
		
		assert stack.isEmpty();
		
		return true;
	}
	
	
	public SuffixTrieNode<I> findSuffixReplacement(TTTStateDFA<I> state1, TTTStateDFA<I> state2) {
		int alphabetSize = alphabet.size();
		
		for(int i = 0; i < alphabetSize; i++) {
			TTTTransitionDFA<I> trans1 = state1.transitions[i];
			TTTTransitionDFA<I> trans2 = state2.transitions[i];
			
			BDTNode<Word<I>> leaf1 = getDTTarget(trans1);
			BDTNode<Word<I>> leaf2 = getDTTarget(trans2);
			
			if(leaf1 != leaf2) {
				BDTNode<Word<I>> succSeparator = discriminationTree.leastCommonAncestor(leaf1, leaf2);
				Word<I> succDiscriminator = succSeparator.getDiscriminator();
				
				if(succDiscriminator instanceof SuffixTrieNode) {
					I sym = alphabet.getSymbol(i);
					SuffixTrieNode<I> trieNode = (SuffixTrieNode<I>)succDiscriminator;
					
					SuffixTrieNode<I> newTrieDiscriminator = suffixTrie.add(sym, trieNode);
					
					return newTrieDiscriminator;
				}
			}
		}
		
		return null;
	}
	

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		if(!wasStarted()) {
			throw new IllegalStateException("startLearning has not yet been called");
		}
		
		if(LOG.isLoggable(Level.FINE)) {
			LOG.fine("Attempting to refine hypothesis with counterexample " + ceQuery);
		}
		
		if(refineSimple(ceQuery)) {
			return true;
		}
		
		Word<I> input = ceQuery.getInput();
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, hypothesis, hypothesis, oracle);
		if(suffixIdx < 0) {
			return false;
		}
		
		Word<I> oldPrefix = input.prefix(suffixIdx);
		TTTStateDFA<I> oldState = hypothesis.getState(oldPrefix);
		
		Word<I> sourcePrefix = input.prefix(suffixIdx - 1);
		TTTStateDFA<I> sourceState = hypothesis.getState(sourcePrefix);
		I sym = input.getSymbol(suffixIdx - 1);
		
		TTTTransitionDFA<I> trans = hypothesis.getInternalTransition(sourceState, sym);
		
		Word<I> suffix = input.subWord(suffixIdx);
		
		splitState(oldState, trans, suffix);
		
		return true;
	}
	

	@Override
	public DFA<?, I> getHypothesisModel() {
		if(!wasStarted()) {
			throw new IllegalStateException("startLearning has not yet been called");
		}
		return hypothesis;
	}
	
	protected TTTStateDFA<I> splitStateSimple(TTTStateDFA<I> oldState, TTTTransitionDFA<I> transition,
			Word<I> suffix) {
		assert oldState.dtLeaf == transition.dtTarget; 
		TTTStateDFA<I> newState = createState(transition, oldState.isAccepting());
		
		openIncoming(oldState);
		
		BDTSplitResult<Word<I>> split = discriminationTree.split(oldState.dtLeaf, suffix);
		
		// FIXME: This is duplicate, but we need to adapt the LocalSuffixFinder in order
		// to resolve this
		boolean oldEval = MQUtil.output(oracle, oldState.getAccessSequence(), suffix).booleanValue();
				
		if(oldEval) {
			assignToLeaf(oldState, split.newTrueChild);
			assignToLeaf(newState, split.newFalseChild);
		}
		else {
			assignToLeaf(oldState, split.newFalseChild);
			assignToLeaf(newState, split.newTrueChild);
		}
				
		closeTransitions();
		
		//verify();
		
		return newState;
	}
	
	protected void splitState(TTTStateDFA<I> state, TTTTransitionDFA<I> transition, Word<I> suffix) {
		TTTStateDFA<I> newState = createState(transition, state.isAccepting());
		
		openIncoming(state);
		BDTSplitResult<Word<I>> split = discriminationTree.split(state.dtLeaf, suffix);
		
		// FIXME: This is duplicate, but we need to adapt the LocalSuffixFinder in order
		// to resolve this
		boolean oldEval = MQUtil.output(oracle, state.getAccessSequence(), suffix).booleanValue();
		
		if(oldEval) {
			assignToLeaf(state, split.newTrueChild);
			assignToLeaf(newState, split.newFalseChild);
		}
		else {
			assignToLeaf(state, split.newFalseChild);
			assignToLeaf(newState, split.newTrueChild);
		}
		
		closeTransitions();
		
		// Search for confluence
		TTTStateDFA<I> oldCurr = state;
		TTTStateDFA<I> newCurr = state;
		int i = 0;
		int confluence = -1;
		for(I sym : suffix) {
			int symIdx = alphabet.getSymbolIndex(sym);
			TTTTransitionDFA<I> oldTrans = oldCurr.transitions[symIdx];
			TTTTransitionDFA<I> newTrans = newCurr.transitions[symIdx];
			
			TTTStateDFA<I> oldSucc = getTransitionTarget(oldTrans);
			TTTStateDFA<I> newSucc = getTransitionTarget(newTrans);
			
			if(oldSucc == newSucc) {
				confluence = i;
				break; // confluence!
			}
			
			oldCurr = oldSucc;
			newCurr = newSucc;
			i++;
		}
		
		if(confluence > 0) {
			Word<I> newSuffix = suffix.subWord(confluence);
			LOG.info("Found confluence, remaining suffix: " + newSuffix);
			DefaultQuery<I, Boolean> query = new DefaultQuery<>(newCurr.getAccessSequence(), newSuffix);
			boolean refined = refineHypothesis(query);
			assert refined;
		}
		else {
			LOG.info("No confluence, hypothesis is canonic");
		}
		
		cleanupDiscriminationTree(state, newState);
	}
	
	protected void cleanupDiscriminationTree(TTTStateDFA<I> oldState, TTTStateDFA<I> newState) {
		BDTNode<Word<I>> separator = discriminationTree.leastCommonAncestor(oldState.dtLeaf, newState.dtLeaf);
		assert separator.isInner();
		
		Word<I> discriminator = separator.getDiscriminator();
		
		if(discriminator instanceof SuffixTrieNode) {
			LOG.finer("No cleanup necessary to separate states " + oldState + " and " + newState);
			return;
		}
		
		if(LOG.isLoggable(Level.FINER)) {
			LOG.finer("Cleaning up discrimination tree to properly separate " + oldState
					+ " [" + oldState.getAccessSequence() + "] and " + newState
					+ " [" + newState.getAccessSequence() + "], currently separated by " + separator.getDiscriminator());
		}
		
		int alphabetSize = alphabet.size();
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			TTTTransitionDFA<I> oldTrans = oldState.transitions[i];
			TTTTransitionDFA<I> newTrans = newState.transitions[i];
			
			BDTNode<Word<I>> oldLeaf = getDTTarget(oldTrans);
			assert oldLeaf.isLeaf();
			
			BDTNode<Word<I>> newLeaf = getDTTarget(newTrans);
			assert newLeaf.isLeaf();
			
			if(oldLeaf != newLeaf) {
				BDTNode<Word<I>> succSeparator = discriminationTree.leastCommonAncestor(oldLeaf, newLeaf);
				Word<I> succDiscriminator = succSeparator.getDiscriminator();
				
				if(!(succDiscriminator instanceof SuffixTrieNode)) {
					LOG.fine("Transition " + sym + " leads to states split by temp suffix");
					continue;
				}
				
				if(LOG.isLoggable(Level.FINER)) {
					LOG.finer("Transition " + sym + " splits, successors separated by " + succDiscriminator);
				}
				
				Word<I> newDiscriminator = suffixTrie.add(sym, (SuffixTrieNode<I>)succDiscriminator);
				replaceDiscriminator(separator, newDiscriminator);
				return;
			}
		}
		
		System.err.println("No suitable splitter found!");
		visualize();
		throw new AssertionError();
	}
	
	
	
	protected void replaceDiscriminator(BDTNode<Word<I>> separator, Word<I> newDiscriminator) {
		Word<I> discriminator = separator.getDiscriminator();
		if(LOG.isLoggable(Level.FINER)) {
			LOG.finer("Replacing discriminator " + discriminator + " by " + newDiscriminator);
		}
		
		//verify();
		
		BDTNodeMap<BDTNode<Word<I>>> oldLeaves = discriminationTree.replaceDiscriminator(separator, newDiscriminator, leafEval);
		
		if(oldLeaves == null) {
			LOG.severe("Couldn't replace discriminators");
			visualize();
			throw new AssertionError();
		}
		
		List<TTTStateDFA<I>> states = new ArrayList<>();
		// Pass 1: Assign new leaves to states
		for(BDTNode<Word<I>> newLeaf : discriminationTree.subtreeLeaves(separator)) {
			BDTNode<Word<I>> oldLeaf = oldLeaves.get(newLeaf);
			TTTStateDFA<I> state = stateForLeaf.apply(oldLeaf);
			state.dtLeaf = newLeaf;
			states.add(state);
		}
		
		// Pass 2: Assign new leaves to states
		for(TTTStateDFA<I> state : states) {
			stateForLeaf.put(state.dtLeaf, state);
			
			List<TTTTransitionDFA<I>> incoming = state.fetchIncoming();
			openTransitions.addAll(incoming);
			for(TTTTransitionDFA<I> trans : incoming) {
				trans.dtTarget = separator;
			}
		}
		
		//verify();
		
		closeTransitions();
		
		//verify();
	}
	
	
	
	protected void openIncoming(TTTStateDFA<I> state) {
		openTransitions.addAll(state.fetchIncoming());
	}
	
	protected void closeTransitions() {
		while(!openTransitions.isEmpty()) {
			TTTTransitionDFA<I> trans = openTransitions.poll();
			if(trans.isTree()) {
				continue;
			}
			updateTransition(trans);
		}
	}
	
	protected TTTStateDFA<I> createState(TTTTransitionDFA<I> transition, boolean accepting) {
		TTTStateDFA<I> state = hypothesis.createState(transition, accepting);
		initState(state);
		return state;
	}
	
	protected TTTStateDFA<I> createStateSpecial(TTTTransitionDFA<I> transition) {
		TTTStateDFA<I> state = hypothesis.createStateSpecial(transition);
		initState(state);
		return state;
	}
	
	
	private void initState(TTTStateDFA<I> state) {
		int alphabetSize = alphabet.size();
		BDTNode<Word<I>> root = discriminationTree.getRoot();
		
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			TTTTransitionDFA<I> newTransition = new TTTTransitionDFA<>(state, sym, root);
			state.transitions[i] = newTransition;
			
			openTransitions.offer(newTransition);
		}
	}
	
	private void initialize() {
		Word<I> eps = Word.epsilon();
		boolean initAccepting = MQUtil.output(oracle, eps).booleanValue();
		discriminationTree.split(discriminationTree.getRoot(), suffixTrie.getRoot(), initAccepting);
		
		TTTStateDFA<I> initialState = hypothesis.initialize(initAccepting);
		initState(initialState);
	}
	
	
	public void verify() {
		for(TTTStateDFA<I> state : hypothesis.getStates()) {
			Word<I> stateAs = state.getAccessSequence();
			
			BDTNode<Word<I>> dtLeaf = discriminationTree.sift(stateAs, wordEval);
			
			assert state.id == dtLeaf.getLeafId();
			
			
			for(int i = 0; i < alphabet.size(); i++) {
				TTTTransitionDFA<I> trans = state.transitions[i];
				if(trans.isTree()) {
					continue;
				}
				Word<I> transAs = trans.getAccessSequence();
					
				BDTNode<Word<I>> curr = discriminationTree.getRoot();
				
				while(curr != trans.dtTarget && curr.isInner()) {
					curr = curr.getChild(wordEval.evaluate(transAs, curr.getDiscriminator()));
				}
				
				assert curr == trans.dtTarget : "Transition " + trans.getAccessSequence() + " has wrong target";
			}
		}
		
		for(BDTNode<Word<I>> inner : discriminationTree.getInnerNodes()) {
			assert !isTempSeparator(inner);
		}
	}
}
