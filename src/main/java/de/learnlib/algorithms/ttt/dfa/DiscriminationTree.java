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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Word;

import com.google.common.collect.Iterators;

import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

/**
 * The discrimination tree data structure.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class DiscriminationTree<I> {

	private final DTNode<I> root;
	
	private final MembershipOracle<I, Boolean> oracle;
	
	public DiscriminationTree(MembershipOracle<I, Boolean> oracle) {
		this.root = new DTNode<>();
		this.oracle = oracle;
	}
	
	/**
	 * Retrieves the root node of this tree.
	 * @return the root node of this tree.
	 */
	public DTNode<I> getRoot() {
		return root;
	}
	
	/**
	 * Sifts an access sequence provided by an object into the tree, starting
	 * at the root. This operation performs a "hard" sift, i.e.,
	 * it will not stop at temporary nodes.
	 * 
	 * @param asp the object providing the access sequence
	 * @return the leaf resulting from the sift operation
	 */
	public DTNode<I> sift(AccessSequenceProvider<I> asp) {
		return sift(asp, true);
	}
	
	/**
	 * Sifts an access sequence provided by an object into the tree,
	 * starting at the root. This can either be a "soft" sift, which stops
	 * either at the leaf <b>or</b> at the first temporary node, or a "hard" sift,
	 * stopping only at a leaf.
	 * 
	 * @param asp the object providing the access sequence
	 * @param hard
	 * @return
	 */
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
	
	
	public class GraphView extends AbstractGraph<DTNode<I>, DTNode<I>> implements DOTPlottableGraph<DTNode<I>, DTNode<I>> {

		@Override
		public Collection<? extends DTNode<I>> getNodes() {
			List<DTNode<I>> nodes = new ArrayList<>();
			
			Iterators.addAll(nodes, root.subtreeNodesIterator());
			
			return nodes;
		}

		@Override
		public Collection<? extends DTNode<I>> getOutgoingEdges(DTNode<I> node) {
			if(node.isLeaf()) {
				return Collections.emptyList();
			}
			return Arrays.asList(node.getFalseChild(), node.getTrueChild());
		}

		@Override
		public DTNode<I> getTarget(DTNode<I> edge) {
			return edge;
		}

		@Override
		public GraphDOTHelper<DTNode<I>, DTNode<I>> getGraphDOTHelper() {
			return new DefaultDOTHelper<DTNode<I>,DTNode<I>>() {

				@Override
				public boolean getNodeProperties(DTNode<I> node,
						Map<String, String> properties) {
					if(node.isLeaf()) {
						properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
						properties.put(NodeAttrs.LABEL, String.valueOf(node.state));
					}
					else {
						properties.put(NodeAttrs.LABEL, node.getDiscriminator().toString());
						if(!node.isTemp()) {
							properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
						}
						else if(node.isBlockRoot()) {
							properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLEOCTAGON);
						}
						else {
							properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
						}
					}
					
					return true;
				}

				@Override
				public boolean getEdgeProperties(DTNode<I> src, DTNode<I> edge,
						DTNode<I> tgt, Map<String, String> properties) {
					if(!edge.getParentEdgeLabel()) {
						properties.put(EdgeAttrs.STYLE, "dashed");
					}
					return true;
				}
				
			};
		}
		
	}
	
	public GraphView graphView() {
		return new GraphView();
	}

}
