package de.learnlib.algorithms.ttt.stree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class SuffixTree<I> extends AbstractGraph<STNode<I>,STNode<I>> implements DOTPlottableGraph<STNode<I>,STNode<I>>, NodeIDs<STNode<I>> {
	
	private final STNode<I> root;
	private final List<STNode<I>> nodes = new ArrayList<>();

	public SuffixTree() {
		this.root = new STNode<>();
		this.nodes.add(root);
	}

	@Override
	public Collection<STNode<I>> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	@Override
	public NodeIDs<STNode<I>> nodeIDs() {
		return this;
	}

	@Override
	public Collection<STNode<I>> getOutgoingEdges(STNode<I> node) {
		STNode<I> parent = node.getParent();
		if(parent == null)
			return Collections.emptySet();
		return Collections.singleton(parent);
	}

	@Override
	public STNode<I> getTarget(STNode<I> edge) {
		return edge;
	}

	@Override
	public GraphDOTHelper<STNode<I>, STNode<I>> getGraphDOTHelper() {
		return new DefaultDOTHelper<STNode<I>,STNode<I>>() {

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getNodeProperties(STNode<I> node,
					Map<String, String> properties) {
				if(!super.getNodeProperties(node, properties))
					return false;
				if(node.isRoot())
					properties.put(LABEL, "<HTML>&epsilon;");
				else
					properties.put(LABEL, String.valueOf(node.getSymbol()));
				return true;
			}

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.lang.Object, java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getEdgeProperties(STNode<I> src, STNode<I> edge,
					STNode<I> tgt, Map<String, String> properties) {
				if(!super.getEdgeProperties(src, edge, tgt, properties))
					return false;
				properties.put(LABEL, "");
				return true;
			}
			
		};
	}

	@Override
	public STNode<I> getNode(int id) {
		return nodes.get(id);
	}

	@Override
	public int getNodeId(STNode<I> node) {
		return node.getId();
	}

	
	public STNode<I> add(I symbol, STNode<I> parent) {
		STNode<I> n = new STNode<>(symbol, parent, nodes.size());
		nodes.add(n);
		return n;
	}

}
