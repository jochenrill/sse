package sse.Matching;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.plaf.basic.BasicDirectoryModel;



import sse.Graph.CDWAG;
import sse.Graph.Node;
import sse.Graph.Pair;

public class Graph<T, K> {

	private ArrayList<BipartiteNode<T>> leftSet;
	private HashMap<Integer, BipartiteNode<K>> rightMap;
	public ArrayList<Edge<T, K>> matching;
	private sse.Graph.Node source;
	private sse.Graph.Node sink;

	public Graph() {
		leftSet = new ArrayList<BipartiteNode<T>>();
		matching = new ArrayList<Edge<T, K>>();
		rightMap = new HashMap<Integer, BipartiteNode<K>>();
	}

	@SuppressWarnings("rawtypes")
	public void constructFromCDWAG(CDWAG c) {
		source = c.source;
		sink = c.sink;
		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
		for (sse.Graph.Edge e : c.source.getEdges()) {
			if (visited.get(e.getEnd()) == null) {
				constructFromCDWAG(e.getEnd(), visited);
			}

		}
		System.out.println("Bipartite Nodes: " + leftSet.size());
		System.out.println("Places: " + rightMap.keySet().size());

//		DirectedSparseMultigraph<BipartiteNode, Edge> tree = new DirectedSparseMultigraph<BipartiteNode, Edge>();
//		BipartiteNode source = new BipartiteNode(new Node(-1));
//		BipartiteNode sink = new BipartiteNode(new Node(-2));
//
//		tree.addVertex(source);
//
//		for (BipartiteNode n : leftSet) {
//			Edge sourceEdge = new Edge();
//			sourceEdge.setLeft(source);
//			sourceEdge.setRight(n);
//			sourceEdge.capacity = 1;
//
//			tree.addEdge(sourceEdge, source, n);
//
//		}
//
//		for (BipartiteNode n : leftSet) {
//			for (int i = 0; i < n.getEdges().size(); i++) {
//				Edge e = (Edge) n.getEdges().get(i);
//
//				tree.addEdge(e, e.getLeft(), e.getRight());
//
//			}
//		}
//		for (BipartiteNode n : rightMap.values()) {
//			Edge sinkEdge = new Edge();
//			sinkEdge.setLeft(n);
//			sinkEdge.setRight(sink);
//			sinkEdge.capacity = 1;
//			tree.addEdge(sinkEdge, n, sink);
//
//		}
//		Transformer<Edge, Number> trans = new Transformer<Edge, Number>() {
//
//			@Override
//			public Number transform(Edge arg0) {
//				return arg0.capacity;
//			}
//		};
//		Map<Edge, Number> map = new HashMap<Edge, Number>();
//		Factory<Edge> factory = new Factory<Edge>() {
//
//			@Override
//			public Edge create() {
//				
//			//	return new Edge(new BipartiteNode(new Node(10)),new BipartiteNode(new Node(11)));
//				return new Edge();
//			}
//		};
//		DAGLayout<BipartiteNode, Edge> l = new DAGLayout<BipartiteNode, Edge>(tree);
//		l.setStretch(10);
//		l.setSize(new Dimension(500, 500));
//		l.setForceMultiplier(10);
//		BasicVisualizationServer<BipartiteNode, Edge> vis = new BasicVisualizationServer<BipartiteNode, Edge>(l);
//		vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<BipartiteNode>());
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.getContentPane().add(vis);
//		frame.pack();
//		frame.setVisible(true);
//		EdmondsKarpMaxFlow<BipartiteNode, Edge> flow = new EdmondsKarpMaxFlow<BipartiteNode, Edge>(
//				tree, source, sink, trans, map, factory);
//		flow.evaluate();
//		
//		for(Edge e : tree.getEdges()){
//			if( !((e.getRight().getData()) instanceof Node) && map.get(e).intValue() != 0  ){
//				matching.add(e);
//				System.out.println(e);
//			}
//		}
//		
		
		
		
		 try {
		 BufferedWriter w = new BufferedWriter(new FileWriter("bitpartite"));
		 w.write("/* this is a generated dot file: www.graphviz.org */\n"
		 + "digraph suffixtree {\n"
		 + "\trankdir=LR\nnode[shape=box]\n");
		 for (BipartiteNode<T> root : leftSet) {
		 for (Edge e : root.getEdges()) {
		 String string = e.getLeft().getData() + "->"
		 + e.getRight().getData()+"100" + ";\n";
		 w.write(string);
		
		 }
		 }
		 w.write("}");
		 w.close();
		 } catch (IOException e) {
		 System.out.println("File not found");
		 }

	}

	private void constructFromCDWAG(Node n, HashMap<Node, Boolean> visited) {

		if (n == sink) {
			return;
		}
		visited.put(n, true);
		BipartiteNode<T> newNode = new BipartiteNode<T>((T) n);
		leftSet.add(newNode);
		// n.usedInMatching = true;
		for (Integer i : n.getPlaces()) {
			BipartiteNode<K> rightNode = null;
			if (rightMap.get(i) == null) {
				rightNode = new BipartiteNode<K>((K) i);
				rightMap.put(i, rightNode);
			} else {
				rightNode = rightMap.get(i);
			}
			Edge<T, K> newEdge = new Edge<T, K>();
			newEdge.setLeft(newNode);
			newEdge.setRight(rightNode);
			newNode.addEdge(newEdge);
			rightNode.addEdge(newEdge);
		}

		for (sse.Graph.Edge e : n.getEdges()) {
			if (visited.get(e.getEnd()) == null) {
				constructFromCDWAG(e.getEnd(), visited);
			}
		}

	}

	public void calculateMatching() {
//		 System.out.println("Start testing for marriage theorem");
//		 HashSet<BipartiteNode<T>> set = new HashSet<BipartiteNode<T>>();
//		 for (BipartiteNode<T> p : leftSet) {
//		 set.add(p);
//		 }
//		 Set<Set<BipartiteNode<T>>> allSubset = Sets.powerSet(set);
//		 for (Set<BipartiteNode<T>> s : allSubset) {
//		 HashMap<BipartiteNode<?>, Boolean> map = new
//		 HashMap<BipartiteNode<?>, Boolean>();
//		 for (BipartiteNode<T> n : s) {
//		 for (Edge<T, K> e : n.getEdges()) {
//		 if (map.get(e.getRight()) == null) {
//		 map.put(e.getRight(), true);
//		 }
//		 }
//		 }
//		 if (s.size() > map.keySet().size()) {
//		 System.out.println("Marriage theorem failed");
//		 }
//		 }
//		 System.out.println("Done testing for marriage theorem");

		ArrayList<Edge<T, K>> path;
		path = augmentingPath();
		// HashSet<Edge<T,K>> matching = new HashSet<Edge<T,K>>();
		matching = new ArrayList<Edge<T, K>>();

		while (path != null) {

			// switch colors
			for (Edge<T, K> e : path) {
				e.matched = !e.matched;

				e.getRight().matched = !e.getRight().matched;
				e.getLeft().matched = !e.getLeft().matched;
				if (e.matched) {
					matching.add(e);
				} else {
					matching.remove(e);
				}
			}

			path = augmentingPath();
		}

		for (BipartiteNode<T> n : leftSet) {
			if (!n.matched) {
				System.out.println("Node " + n + " unmatched");
				System.out.println("Places: "
						+ ((Node) n.getData()).getPlaces());
			}
		}
		// test print matching

		// for (Edge<T, K> e : matching) {
		// System.out.println("Node "
		// + ((sse.Graph.Node) e.getLeft().getData()).getId()
		// + " matched with " + ((Integer) e.getRight().getData()));
		// }

	}

	private ArrayList<Edge<T, K>> augmentingPath() {
		ArrayList<Edge<T, K>> path = new ArrayList<Edge<T, K>>();
		for (BipartiteNode<T> node : leftSet) {
			if (!node.matched) {
				 sse.Graph.Node n = (sse.Graph.Node) node.getData();
				 String s = n.toString();
			//	ArrayList<BipartiteNode<?>> visited = new ArrayList<BipartiteNode<?>>();
				if (augmentingPath(path, node, true)) {

					return path;
				}
			}
		}
		return null;

	}

	private boolean augmentingPath(ArrayList<Edge<T, K>> path,
			BipartiteNode<?> n, boolean left) {

		//visited.add(n);
		for (Edge<T, K> e : n.getEdges()) {
			if (left) {
				if (!e.matched && !path.contains(e)) {
					path.add(e);
					if(augmentingPath(path, e.getRight(), false)){
						return true;
 					} else {
 						path.remove(e);
 					}

				}
			} else if (!left) {
				if (!n.matched) {
					return true;
				} else if (e.matched && !path.contains(e)) {
					path.add(e);
					if( augmentingPath(path, e.getLeft(), true)){
						return true;
					} else {
						path.remove(e);
					}
				}
			}
		}
		return false;
	}

}
