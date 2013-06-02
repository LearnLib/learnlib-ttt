package de.learnlib.algorithms.ttt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.dtree.DiscriminationTree;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis;
import de.learnlib.algorithms.ttt.stree.STNode;
import de.learnlib.algorithms.ttt.stree.SuffixTree;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

public abstract class AbstractTTTLearner<I, O, SP, TP, M, H extends TTTHypothesis<I, O, SP, TP, ?>> implements
		LearningAlgorithm<M, I, O> {
	
	private final Alphabet<I> alphabet;
	private final DiscriminationTree<I, O, SP, TP> dtree;
	private final SuffixTree<I> stree = new SuffixTree<>();
	protected final H hypothesis;
	
	private final Queue<HTransition<I,O,SP,TP>> openTransitions = new ArrayDeque<>();
	private final List<HypothesisState<I,O,SP,TP>> newStates = new ArrayList<>();
	private final List<HTransition<I,O,SP,TP>> newTransitions = new ArrayList<>();
	
	private final MembershipOracle<I,O> oracle;
	
	protected AbstractTTTLearner(Alphabet<I> alphabet, MembershipOracle<I,O> oracle, H hypothesis) {
		this.alphabet = alphabet;
		this.hypothesis = hypothesis;
		this.dtree = new DiscriminationTree<>();
		this.oracle = oracle;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#startLearning()
	 */
	@Override
	public void startLearning() {
		DTNode<I,O,SP,TP> initNode = dtree.sift(Word.<I>epsilon(), oracle);
		if(initNode.getHypothesisState() != null) {
			throw new IllegalStateException("Cannot start learning: Discrimination tree already contains states");
		}
		HypothesisState<I,O,SP,TP> initState = hypothesis.getInitialState();
		initNode.setHypothesisState(initState);
		initializeState(initState);
		
		close();
		updateProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		Word<I> ceWord = ceQuery.getInput();
		
		if(!handleCounterexample(ceWord))
			return false;
		
		close();
		updateProperties();
		return true;
	}
	
	protected boolean handleCounterexample(Word<I> ceWord) {
		int i = 0;
		int ceLen = ceWord.length();
		
		HypothesisState<I, O, SP, TP> curr = hypothesis.getInitialState();
		HTransition<I, O, SP, TP> next = null;
		
		boolean found = false;
		// STEP 1: Follow the counterexample word until the first non-tree
		//         transition is found. Afterwards, curr will be the outgoing
		//         state of this transition, and next will be the transition itself.
		//         This is the first point where the access sequence transformation
		//         actually changes the word, and requires no membership queries.
		while(i < ceLen) {
			I sym = ceWord.getSymbol(i++);
			next = hypothesis.getInternalTransition(curr, sym);
			HypothesisState<I, O, SP, TP> nextState = next.getTreeTarget();
			if(nextState == null) {
				found = true;
				break;
			}
			curr = nextState;
		}
		
		if(!found)
			return false;
		
		// STEP 2: Follow the counterexample word until a point is reached where the
		//         access sequence transformation changes the output (respective to
		//         the remaining suffix).
		Word<I> suffix;
		HypothesisState<I, O, SP, TP> tgtState;
		O preOut, postOut;
		Word<I> transAs;
		boolean cont;
		do {
			suffix = ceWord.subWord(i);
			tgtState = next.currentTargetState();
			transAs = next.getAccessSequence();
			Word<I> tgtAs = tgtState.getAccessSequence();
			
			preOut = MQUtil.query(oracle, transAs, suffix);
			postOut = MQUtil.query(oracle, tgtAs, suffix);
			
			if(!Objects.equals(preOut, postOut)) {
				found = true;
				break;
			}

			cont = (i < ceLen);
			if(cont) {
				I sym = ceWord.getSymbol(i++);
				next = hypothesis.getInternalTransition(tgtState, sym);
			}
		} while(cont);
		
		if(!found)
			return false;
		
		HypothesisState<I, O, SP, TP> newState = createHypothesisState(next);
		
		// SPECIAL CASE: Empty suffix can be used to split states, but is not yet
		//               on the path of the discrimination tree. Add it and we're
		//               done.
		if(suffix.isEmpty()) {	
			splitState(tgtState, stree.getRoot(), postOut, newState, preOut);
			return true;
		}
		
		STNode<I> tmpDiscr = new STNode<>(suffix);
		DTNode<I, O, SP, TP> splitter = splitState(tgtState, tmpDiscr, postOut, newState, preOut);
		
		Deque<CEHandlingContext<I,O,SP,TP>> stack = new ArrayDeque<>();
		stack.push(new CEHandlingContext<>(tmpDiscr, splitter, tgtState, newState));
		
		Deque<CEHandlingContext<I,O,SP,TP>> defer = new ArrayDeque<>();
		STNode<I> backtrack = tmpDiscr;
		
		while(!stack.isEmpty()) {
			CEHandlingContext<I, O, SP, TP> ctx = stack.pop();
			tmpDiscr = ctx.getTempDiscriminator();
			
			if(backtrack == null) {
				// We do not need to backtrack any further, since we have either found
				// an existing discriminator or reached the end of the string
				// In that case, replace the temporary discriminators by their version
				// in the suffix trees
				STNode<I> tmpParent = tmpDiscr.getParent();
				STNode<I> finalParent = tmpParent.getFinalReplacement();
				assert finalParent != null;
				STNode<I> finalDiscr = stree.add(tmpDiscr.getSymbol(), finalParent);
				tmpDiscr.setFinalReplacement(finalDiscr);
				splitter = ctx.getSplitter();
				splitter.setDiscriminator(finalDiscr);
			}
			else if(tmpDiscr != backtrack) {
				// We have to backtrack past this entry, so postpone it
				defer.push(ctx);
			}
			else { // tmpDiscr == backtrack
				while(!defer.isEmpty())
					stack.push(defer.pop());
				
				I nextSym = ceWord.getSymbol(i++);
				
				HypothesisState<I, O, SP, TP> oldState = ctx.getOldState();
				newState = ctx.getNewState();
				
				HTransition<I, O, SP, TP> oldTrans = hypothesis.getInternalTransition(oldState, nextSym);
				HTransition<I, O, SP, TP> newTrans = hypothesis.getInternalTransition(newState, nextSym);
				
				DTNode<I, O, SP, TP> oldDt = updateTransition(oldTrans);
				DTNode<I, O, SP, TP> newDt = updateTransition(newTrans);
				
				if(oldDt == newDt) {
					suffix = ceWord.subWord(i);
					O oldOut = MQUtil.query(oracle, oldTrans.getAccessSequence(), suffix);
					O newOut = MQUtil.query(oracle, newTrans.getAccessSequence(), suffix);
					
					if(!Objects.equals(oldOut, newOut)) {
						tmpDiscr = new STNode<>(suffix);
						newState = createHypothesisState(newTrans);
						splitter = splitState(oldDt.getHypothesisState(), tmpDiscr, oldOut, newState, newOut);
						
						CEHandlingContext<I, O, SP, TP> newCtx
							= new CEHandlingContext<>(tmpDiscr, splitter, oldState, newState);
						stack.push(ctx);
						stack.push(newCtx);
						backtrack = newCtx.getTempDiscriminator();
					}
					else { // confluence
						STNode<I> finalDiscr = stree.add(nextSym, stree.getRoot());
						tmpDiscr.setFinalReplacement(finalDiscr);
						ctx.getSplitter().setDiscriminator(finalDiscr);
						backtrack = null;
					}
				}
				else {
					DTNode<I,O,SP,TP> ca = dtree.commonAncestor(oldDt, newDt);
					STNode<I> succDiscr = ca.getDiscriminator();
					if(succDiscr == tmpDiscr) {
						tmpDiscr.setTempWord(ceWord.subWord(i));
						stack.push(ctx);
					}
					else if(succDiscr.isTemp()) {
						defer.push(ctx);
						backtrack = succDiscr;
					}
					else {
						STNode<I> finalDiscr = stree.add(nextSym, succDiscr);
						tmpDiscr.setFinalReplacement(finalDiscr);
						splitter = ctx.getSplitter();
						splitter.setDiscriminator(finalDiscr);
						backtrack = null;
					}
				}
			}
		}
		
		return true;
	}
	
	protected DTNode<I, O, SP, TP> splitState(HypothesisState<I, O, SP, TP> oldState, STNode<I> discriminator,
			O oldOut, HypothesisState<I, O, SP, TP> newState, O newOut) {
		for(HTransition<I, O, SP, TP> trans : oldState.getNonTreeIncoming()) {
			if(!trans.isTree())
				openTransitions.offer(trans);
		}
		oldState.clearNonTreeIncoming();
		DTNode<I, O, SP, TP> splitter = oldState.getDTLeaf();
		dtree.split(splitter, discriminator, oldOut, newState, newOut);
		return splitter;
	}
	
	public SuffixTree<I> getSuffixTree() {
		return stree;
	}
	
	public DiscriminationTree<I, O, SP, TP> getDiscriminationTree() {
		return dtree;
	}
	
	public H getHypothesisTree() {
		return hypothesis;
	}
	
	protected void initializeState(HypothesisState<I,O,SP,TP> state) {
		newStates.add(state);
		
		int numSyms = alphabet.size();
		for(int i = 0; i < numSyms; i++) {
			I sym = alphabet.getSymbol(i);
			HTransition<I, O, SP, TP> trans = new HTransition<I,O,SP,TP>(state, sym, dtree.getRoot());
			state.setTransition(i, trans);
			newTransitions.add(trans);
			openTransitions.offer(trans);
		}
	}
	
	protected HypothesisState<I, O, SP, TP> createHypothesisState(HTransition<I,O,SP,TP> treeIncoming) {
		HypothesisState<I,O,SP,TP> state = hypothesis.createState(treeIncoming);
		initializeState(state);
		
		return state;
	}
	
	
	protected DTNode<I, O, SP, TP> updateTransition(HTransition<I, O, SP, TP> transition) {
		Word<I> as = transition.getAccessSequence();
		DTNode<I,O,SP,TP> leaf = dtree.sift(transition.getDTTarget(), as, oracle);
		if(leaf.getHypothesisState() == null) {
			HypothesisState<I, O, SP, TP> state = createHypothesisState(transition);
			leaf.setHypothesisState(state);
		}
		else {
			transition.updateDTTarget(leaf);
			leaf.getHypothesisState().addNonTreeIncoming(transition);
		}
		
		return leaf;
	}
	
	protected void close() {
		HTransition<I,O,SP,TP> curr;
		
		while((curr = openTransitions.poll()) != null) {
			Word<I> as = curr.getAccessSequence();
			DTNode<I,O,SP,TP> leaf = dtree.sift(curr.getDTTarget(), as, oracle);
			if(leaf.getHypothesisState() == null) {
				HypothesisState<I,O,SP,TP> state = createHypothesisState(curr);
				leaf.setHypothesisState(state);
			}
			else {
				curr.updateDTTarget(leaf);
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
