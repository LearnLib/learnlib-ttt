/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib-TTT, https://github.com/LearnLib/learnlib-ttt/
 * 
 * LearnLib-TTT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LearnLib-TTT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LearnLib-TTT.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * The TTT learning algorithm for {@link DFA}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class TTTLearnerDFA<I> implements DFALearner<I>, AccessSequenceTransformer<I>, SuffixOutput<I, Boolean> {
	
	private final Alphabet<I> alphabet;
	private final TTTHypothesisDFA<I> hypothesis;
	private final MembershipOracle<I, Boolean> oracle;
	
	private final DiscriminationTree<I> dtree;
	// private final SuffixTrie<I> suffixTrie = new SuffixTrie<>();
	
	/**
	 * Open transitions, i.e., transitions that possibly point to a non-leaf
	 * node in the discrimination tree.
	 */
	private final Queue<TTTTransitionDFA<I>> openTransitions = new ArrayDeque<>();
	
	/**
	 * Suffix finder to be used for counterexample analysis.
	 */
	private final LocalSuffixFinder<? super I, ? super Boolean> suffixFinder;
	
	/**
	 * The size of the hypothesis after the last call to {@link #closeTransitions()}.
	 * This allows classifying states as "old" by means of their ID, which is necessary
	 * to determine whether its transitions need to be added to the list
	 * of "open" transitions.
	 */
	private int lastGeneration;
	
	/**
	 * The blocks during a split operation. A block is a maximal subtree of the
	 * discrimination tree containing temporary discriminators at its root. 
	 */
	private final BlockList<I> blockList = new BlockList<>();
	
	public TTTLearnerDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle,
			LocalSuffixFinder<? super I, ? super Boolean> suffixFinder) {
		this.alphabet = alphabet;
		this.hypothesis = new TTTHypothesisDFA<>(alphabet);
		this.oracle = oracle;
		this.dtree = new DiscriminationTree<>(oracle);
		this.suffixFinder = suffixFinder;
	}
	
	/*
	 * DFALearner interface methods
	 */

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#startLearning()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
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
	

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#getHypothesisModel()
	 */
	@Override
	public TTTHypothesisDFA<I> getHypothesisModel() {
		return hypothesis;
	}
	
	
	
	/*
	 * Private helper methods.
	 */
	
	
	/**
	 * Initializes a state. Creates its outgoing transition objects, and adds them
	 * to the "open" list.
	 * @param state the state to initialize
	 */
	private void initializeState(TTTStateDFA<I> state) {
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			TTTTransitionDFA<I> trans = new TTTTransitionDFA<>(state, sym);
			trans.setNonTreeTarget(dtree.getRoot());
			state.transitions[i] = trans;
			openTransitions.offer(trans);
		}
	}
	
	
	/**
	 * Performs a single refinement of the hypothesis, i.e., without 
	 * repeated counterexample evaluation. The parameter and return value
	 * have the same significance as in {@link #refineHypothesis(DefaultQuery)}.
	 * 
	 * @param ceQuery the counterexample (query) to be used for refinement
	 * @return {@code true} if the hypothesis was refined, {@code false} otherwise
	 */
	private boolean refineHypothesisSingle(DefaultQuery<I, Boolean> ceQuery) {
		TTTStateDFA<I> state = getState(ceQuery.getPrefix());
		boolean out = computeHypothesisOutput(state, ceQuery.getSuffix());
		
		if(out == ceQuery.getOutput()) {
			return false;
		}
		
		// Determine a counterexample decomposition (u, a, v)
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, this, hypothesis, oracle);
		assert suffixIdx != -1;
		
		Word<I> ceInput = ceQuery.getInput();
		
		Word<I> u = ceInput.prefix(suffixIdx - 1);
		I a = ceInput.getSymbol(suffixIdx - 1);
		int aIdx = alphabet.getSymbolIndex(a);
		Word<I> v = ceInput.subWord(suffixIdx);
		
		
		TTTStateDFA<I> pred = getState(u);
		TTTTransitionDFA<I> trans = pred.transitions[aIdx];
		
		// Split the state reached by ua
		splitState(trans, v);
		
		// "Repair" the hypothesis
		while(!repair()) {}
		
		// Close all open transitions
		closeTransitions();
		
		return true;
	}
	
	/**
	 * Chooses a block root, and finalizes the corresponding discriminator.
	 * @return {@code true} if a splittable block root was found, {@code false}
	 * otherwise.
	 */
	private boolean finalizeAny() {
		GlobalSplitter<I> splitter = findSplitterGlobal();
		if(splitter != null) {
			finalizeDiscriminator(splitter.blockRoot, splitter.localSplitter);
			return true;
		}
		return false;
	}
	
	/**
	 * "Repairs" the data structures of the algorithm by subsequently
	 * finalizing discriminators of block roots. If this alone is insufficient (i.e.,
	 * there are blocks with discriminators that cannot be finalized),
	 * consistency between the discrimination tree and the hypothesis is restored
	 * by calling {@link #makeConsistent(DTNode)}.
	 * <p>
	 * <b>Note:</b> In the latter case, this method has to be called again. Whether
	 * or not this is necessary can be determined by examining the return value.
	 * 
	 * @return {@code true} if the hypothesis was successfully repaired, {@code false}
	 * otherwise (i.e., if a subsequent call to this method is required)
	 */
	private boolean repair() {
		while(finalizeAny()) {}
		if(blockList.isEmpty()) {
			return true;
		}
		DTNode<I> blockRoot = blockList.chooseBlock();
		makeConsistent(blockRoot);
		return false;
	}
	
	/**
	 * Restores consistency between the discriminator info contained in the subtree
	 * of the given block, and the hypothesis. As counterexample reevaluation might result
	 * in queries of relatively high length, only a single discriminator and
	 * two states it separated are considered. Hence, this method may have to be revoked
	 * repeatedly in order to allow further discriminator finalization.
	 *  
	 * @param blockRoot the root of the block in which to restore consistency
	 */
	private void makeConsistent(DTNode<I> blockRoot) {
		// TODO currently, we have a very simplistic approach: we take the
		// leftmost inner node, its left child, and the leftmost child of its
		// new subtree. While this does not impair correctness, a heuristic
		// trying to minimize the length of discriminators and state access sequences might be worth
		// exploring.
		DTNode<I> separator = blockRoot.getExtremalChild(false).getParent();
		Word<I> discriminator = separator.getDiscriminator();
		
		DTNode<I> falseChild = separator.getFalseChild();
		if(ensureConsistency(falseChild.state, discriminator, false)) {
			return;
		}
		DTNode<I> trueChild = separator.getTrueChild().getExtremalChild(false);
		boolean wasInconsistent = ensureConsistency(trueChild.state, discriminator, true);
		
		
		assert wasInconsistent;
	}
	
	
	/**
	 * Ensures that the given state's output for the specified suffix in the hypothesis
	 * matches the provided real outcome, as determined by means of a membership query.
	 * This is achieved by analyzing the derived counterexample, if the hypothesis
	 * in fact differs from the provided real outcome.
	 * 
	 * @param state the state
	 * @param suffix the suffix
	 * @param realOutcome the real outcome, previously determined through a membership query
	 * @return {@code true} if the hypothesis was refined (i.e., was inconsistent when
	 * this method was called), {@code false} otherwise
	 */
	private boolean ensureConsistency(TTTStateDFA<I> state, Word<I> suffix, boolean realOutcome) {
		boolean hypOutcome = computeHypothesisOutput(state, suffix);
		if(hypOutcome == realOutcome) {
			return false;
		}
		DefaultQuery<I, Boolean> query = new DefaultQuery<>(state.getAccessSequence(), suffix, realOutcome);
		
		while(refineHypothesisSingle(query)) {}
		
		return true;
	}
	
	
	/**
	 * Data structure for representing a splitter.
	 * <p>
	 * A splitter is represented by an input symbol, and a DT node
	 * that separates the successors (wrt. the input symbol) of the original
	 * states. From this, a discriminator can be obtained by prepending the input
	 * symbol to the discriminator that labels the separating successor.
	 * <p>
	 * <b>Note:</b> as the discriminator finalization is applied to the root
	 * of a block and affects all nodes, there is no need to store references
	 * to the source states from which this splitter was obtained.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class Splitter<I> {
		public final int symbolIdx;
		public final DTNode<I> succSeparator;
		
		public Splitter(int symbolIdx, DTNode<I> succSeparator) {
			assert !succSeparator.isTemp() && succSeparator.isInner();
			
			this.symbolIdx = symbolIdx;
			this.succSeparator = succSeparator;
		}
	}
	
	/**
	 * A global splitter. In addition to the information stored in a (local)
	 * {@link Splitter}, this class also stores the block the local splitter
	 * applies to.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class GlobalSplitter<I> {
		public final Splitter<I> localSplitter;
		public final DTNode<I> blockRoot;
		
		public GlobalSplitter(DTNode<I> blockRoot, Splitter<I> localSplitter) {
			this.blockRoot = blockRoot;
			this.localSplitter = localSplitter;
		}
	}
	
	/**
	 * Determines a global splitter, i.e., a splitter for any block.
	 * This method may (but is not required to) employ heuristics
	 * to obtain a splitter with a relatively short suffix length.
	 * 
	 * @return a splitter for any of the blocks
	 */
	private GlobalSplitter<I> findSplitterGlobal() {
		// TODO: Make global option
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
	
	/**
	 * Determines a (local) splitter for a given block. This method may
	 * (but is not required to) employ heuristics to obtain a splitter
	 * with a relatively short suffix.
	 *  
	 * @param blockRoot the root of the block
	 * @return a splitter for this block, or {@code null} if no such splitter
	 * could be found.
	 */
	@SuppressWarnings("unchecked")
	private Splitter<I> findSplitter(DTNode<I> blockRoot) {
		// TODO: Make global option
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
	
	/**
	 * Checks whether the hypothesis is consistent with the discrimination tree.
	 * If an inconsistency is discovered, it is returned in the form of a counterexample.
	 * 
	 * @return a counterexample uncovering an inconsistency, or {@code null}
	 * if the hypothesis is consistent with the discrimination tree
	 */
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
	
	/**
	 * Creates a state in the hypothesis. This method cannot be used for the initial
	 * state, which has no incoming tree transition.
	 * 
	 * @param transition the "parent" transition in the spanning tree
	 * @param accepting whether or not the new state state is accepting
	 * @return the newly created state
	 */
	private TTTStateDFA<I> createState(@Nonnull TTTTransitionDFA<I> transition, boolean accepting) {
		TTTStateDFA<I> newState = hypothesis.createState(transition, accepting);
		initializeState(newState);
		
		return newState;
	}
	
	
	/**
	 * Retrieves the target state of a given transition. This method works for both tree
	 * and non-tree transitions. If a non-tree transition points to a non-leaf node,
	 * it is updated accordingly before a result is obtained.
	 * 
	 * @param trans the transition
	 * @return the target state of this transition (possibly after it having been updated)
	 */
	private TTTStateDFA<I> getTarget(TTTTransitionDFA<I> trans) {
		if(trans.isTree()) {
			return trans.getTreeTarget();
		}
		DTNode<I> dtTarget = updateDTTarget(trans);
		return dtTarget.state;
	}
	
	/**
	 * Computes the state output for a sequence of input symbols.
	 * 
	 * @param state the state
	 * @param suffix the sequence of input symbols
	 * @return the state output for the specified suffix
	 */
	private boolean computeHypothesisOutput(TTTStateDFA<I> state, Iterable<? extends I> suffix) {
		TTTStateDFA<I> endState = getState(state, suffix);
		return endState.accepting;
	}
	
	/**
	 * Retrieves the successor for a given state and a suffix sequence.
	 * 
	 * @param start the originating state
	 * @param suffix the sequence of input symbols to process
	 * @return the state reached after processing {@code suffix}, starting from
	 * {@code start}
	 */
	private TTTStateDFA<I> getState(TTTStateDFA<I> start, Iterable<? extends I> suffix) {
		TTTStateDFA<I> curr = start;
		
		for(I sym : suffix) {
			TTTTransitionDFA<I> trans = hypothesis.getInternalTransition(curr, sym);
			curr = getTarget(trans);
		}
		
		return curr;
	}
	
	/**
	 * Retrieves the state reached by the given sequence of symbols, starting
	 * from the initial state.
	 * @param suffix the sequence of symbols to process
	 * @return the state reached after processing the specified symbols
	 */
	private TTTStateDFA<I> getState(Iterable<? extends I> suffix) {
		return getState(hypothesis.getInitialState(), suffix);
	}
	
	/**
	 * Finalize a discriminator. Given a block root and a {@link Splitter},
	 * replace the discriminator at the block root by the one derived from the
	 * splitter, and update the discrimination tree accordingly.
	 * 
	 * @param blockRoot the block root whose discriminator to finalize
	 * @param splitter the splitter to use for finalization
	 */
	private void finalizeDiscriminator(DTNode<I> blockRoot, Splitter<I> splitter) {
		assert blockRoot.isBlockRoot();
		
		Word<I> finalDiscriminator = prepareSplit(blockRoot, splitter);
		
		DTNode<I> falseSubtree = extractSubtree(blockRoot, false);
		DTNode<I> trueSubtree = extractSubtree(blockRoot, true);
		
		blockRoot.setFalseChild(falseSubtree);
		blockRoot.setTrueChild(trueSubtree);
		
		blockRoot.temp = false;
		blockRoot.splitData = null;
		
		assert blockRoot.getFalseChild().splitData == null;
		assert blockRoot.getTrueChild().splitData == null;
		
		blockRoot.setDiscriminator(finalDiscriminator);
		blockRoot.removeFromBlockList();
		
		// Register as blocks, if they are non-trivial subtrees
		if(falseSubtree.isInner()) {
			blockList.insertBlock(falseSubtree);
		}
		if(trueSubtree.isInner()) {
			blockList.insertBlock(trueSubtree);
		}
	}
	
	/**
	 * Prepare a split operation on a block, by marking all the nodes and
	 * transitions in the subtree (and annotating them with
	 * {@link SplitData} objects).
	 * 
	 * @param node the block root to be split
	 * @param splitter the splitter to use for splitting the block
	 * @return the discriminator to use for splitting
	 */
	private Word<I> prepareSplit(DTNode<I> node, Splitter<I> splitter) {
		Deque<DTNode<I>> dfsStack = new ArrayDeque<>();
		
		DTNode<I> succSeparator = splitter.succSeparator;
		int symbolIdx = splitter.symbolIdx;
		I symbol = alphabet.getSymbol(symbolIdx);
		
		Word<I> discriminator = succSeparator.getDiscriminator().prepend(symbol);
		
		dfsStack.push(node);
		assert node.splitData == null;
		
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
				// This used to be updateDTTarget(), but this would make 
				// the "incoming" information inconsistent!
				DTNode<I> dtTarget = trans.getDTTarget();
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
	
	/**
	 * Marks a node, and propagates the label up to all nodes on the path from the block
	 * root to this node.
	 * 
	 * @param node the node to mark
	 * @param label the label to mark the node with
	 */
	private static <I> void markAndPropagate(DTNode<I> node, boolean label) {
		DTNode<I> curr = node;
		
		while(curr != null && curr.splitData != null) {
			if(!curr.splitData.mark(label)) {
				return;
			}
			curr = curr.getParent();
		}
	}
	
	/**
	 * Data structure required during an extract operation. The latter basically
	 * works by copying nodes that are required in the extracted subtree, and this
	 * data structure is required to associate original nodes with their extracted copies.
	 *  
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class ExtractRecord<I> {
		public final DTNode<I> original;
		public final DTNode<I> extracted;
		
		public ExtractRecord(DTNode<I> original, DTNode<I> extracted) {
			this.original = original;
			this.extracted = extracted;
		}
	}
	
	/**
	 * Extract a (reduced) subtree containing all nodes with the given label
	 * from the subtree given by its root. "Reduced" here refers to the fact that
	 * the resulting subtree will contain no inner nodes with only one child.
	 * <p>
	 * The tree returned by this method (represented by its root) will have
	 * as a parent node the root that was passed to this method.
	 *  
	 * @param root the root of the subtree from which to extract
	 * @param label the label of the nodes to extract
	 * @return the extracted subtree
	 */
	private DTNode<I> extractSubtree(DTNode<I> root, boolean label) {
		assert root.splitData != null;
		assert root.splitData.isMarked(label);
		
		
		
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
			
			assert extracted.splitData == null;
		}
		
		return firstExtracted;
	}
	
	/**
	 * Moves all transition from the "incoming" list (for a given label) of an
	 * old node to the "incoming" list of a new node.
	 *   
	 * @param newNode the new node
	 * @param oldNode the old node
	 * @param label the label to consider
	 */
	private static <I> void moveIncoming(DTNode<I> newNode, DTNode<I> oldNode, boolean label) {
		newNode.getIncoming().insertAllIncoming(oldNode.splitData.getIncoming(label));
	}
	
	/**
	 * Create a new state during extraction on-the-fly. This is required if a node
	 * in the DT has an incoming transition with a certain label, but in its subtree
	 * there are no leaves with this label as their state label.
	 * 
	 * @param newNode the extracted node
	 */
	private void createNewState(DTNode<I> newNode) {
		TTTTransitionDFA<I> newTreeTrans = newNode.getIncoming().choose();
		assert newTreeTrans != null;
		
		boolean accepting = dtree.getRoot().subtreeLabel(newNode).booleanValue();
		
		TTTStateDFA<I> newState = createState(newTreeTrans, accepting);
		
		link(newNode, newState);
	}
	
	/**
	 * Establish the connection between a node in the discrimination tree
	 * and a state of the hypothesis.
	 * 
	 * @param dtNode the node in the discrimination tree
	 * @param state the state in the hypothesis
	 */
	private static <I> void link(DTNode<I> dtNode, TTTStateDFA<I> state) {
		assert dtNode.isLeaf();
		
		dtNode.state = state;
		state.dtLeaf = dtNode;
	}

	/*
	 * Access Sequence Transformer API
	 */
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.Output#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Boolean computeOutput(Iterable<? extends I> input) {
		return computeHypothesisOutput(hypothesis.getInitialState(), input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.SuffixOutput#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Boolean computeSuffixOutput(Iterable<? extends I> prefix,
			Iterable<? extends I> suffix) {
		TTTStateDFA<I> prefixState = getState(prefix);
		return computeHypothesisOutput(prefixState, suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#transformAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		return getState(word).getAccessSequence();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#isAccessSequence(net.automatalib.words.Word)
	 */
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
	
	/**
	 * Splits a state in the hypothesis, using a temporary discriminator. The state
	 * to be split is identified by an incoming non-tree transition. This transition is
	 * subsequently turned into a spanning tree transition.
	 * 
	 * @param transition the transition
	 * @param tempDiscriminator the temporary discriminator
	 * @return the discrimination tree node separating the old and the new node, labeled
	 * by the specified temporary discriminator
	 */
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
	
	/**
	 * Checks whether the given state is old, i.e., was added to the hypothesis before the
	 * most recent call to {@link #closeTransitions()}.
	 * 
	 * @param state the state to check
	 * @return {@code true} if this state is old, {@code false} otherwise
	 */
	private boolean isOld(@Nonnull TTTStateDFA<I> state) {
		return state.id < lastGeneration;
	}

	/**
	 * Ensures that all non-tree transitions in the hypothesis point to leaf nodes.
	 */
	private void closeTransitions() {
		while(!openTransitions.isEmpty()) {
			TTTTransitionDFA<I> trans = openTransitions.poll();
			closeTransition(trans);
		}
		this.lastGeneration = hypothesis.size();
	}
	
	/**
	 * Ensures that the specified transition points to a leaf-node. If the transition
	 * is a tree transition, this method has no effect.
	 * 
	 * @param trans the transition
	 */
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
	
	/**
	 * Updates the transition to point to a leaf in the discrimination tree, and
	 * returns this leaf.
	 * 
	 * @param transition the transition
	 * @return the DT leaf corresponding to the transition's target state
	 */
	private DTNode<I> updateDTTarget(TTTTransitionDFA<I> transition) {
		return updateDTTarget(transition, true);
	}
	
	/**
	 * Updates the transition to point to either a leaf in the discrimination tree,
	 * or---if the {@code hard} parameter is set to {@code false}---to a block
	 * root.
	 * 
	 * @param transition the transition
	 * @param hard whether to consider leaves as sufficient targets only
	 * @return the new target node of the transition
	 */
	private DTNode<I> updateDTTarget(TTTTransitionDFA<I> transition, boolean hard) {
		if(transition.isTree()) {
			return transition.getTreeTarget().dtLeaf;
		}
		
		DTNode<I> dt = transition.getNonTreeTarget();
		dt = dtree.sift(dt, transition, hard);
		transition.setNonTreeTarget(dt);
		
		return dt;
	}
	
	
	/**
	 * Performs a membership query.
	 * 
	 * @param prefix the prefix part of the query
	 * @param suffix the suffix part of the query
	 * @return the output
	 */
	private boolean query(Word<I> prefix, Word<I> suffix) {
		return MQUtil.output(oracle, prefix, suffix);
	}
	
	/**
	 * Performs a membership query, using an access sequence as its prefix.
	 * 
	 * @param accessSeqProvider the object from which to obtain the access sequence
	 * @param suffix the suffix part of the query
	 * @return the output
	 */
	private boolean query(AccessSequenceProvider<I> accessSeqProvider, Word<I> suffix) {
		return query(accessSeqProvider.getAccessSequence(), suffix);
	}
	
	/**
	 * Returns the discrimination tree.
	 * @return the discrimination tree
	 */
	public DiscriminationTree<I> getDiscriminationTree() {
		return dtree;
	}

}
