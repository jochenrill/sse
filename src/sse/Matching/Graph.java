package sse.Matching;

import java.util.ArrayList;
import java.util.HashMap;
import sse.Graph.CDWAG;
import sse.Graph.Node;
import sse.Vectors.Constants;

public class Graph<T, K> {

	private ArrayList<BipartiteNode<T>> leftSet;
	private HashMap<Integer, BipartiteNode<K>> rightMap;
	public ArrayList<Edge<T, K>> matching;
	private sse.Graph.Node sink;

	public Graph() {
		leftSet = new ArrayList<BipartiteNode<T>>();
		matching = new ArrayList<Edge<T, K>>();
		rightMap = new HashMap<Integer, BipartiteNode<K>>();
	}

	public void constructFromCDWAG(CDWAG c) {
		sink = c.sink;
		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
		for (sse.Graph.Edge e : c.source.getEdges()) {
			if (visited.get(e.getEnd()) == null) {
				constructFromCDWAG(e.getEnd(), visited);
			}

		}

	}

	/*
	 * This method creates a bipartite graph from the given cdwag. The resulting
	 * graph has Nodes on the left and places on the right side.
	 */

	@SuppressWarnings("unchecked")
	private void constructFromCDWAG(Node n, HashMap<Node, Boolean> visited) {

		if (n == sink) {
			return;
		}
		visited.put(n, true);
		BipartiteNode<T> newNode = new BipartiteNode<T>((T) n);
		leftSet.add(newNode);
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

		if (Constants.DEBUG) {
			for (BipartiteNode<T> n : leftSet) {
				if (!n.matched) {
					System.out.println("Node " + n + " unmatched");
					System.out.println("Places: "
							+ ((Node) n.getData()).getPlaces());
				}
			}
		}

	}

	private ArrayList<Edge<T, K>> augmentingPath() {
		ArrayList<Edge<T, K>> path = new ArrayList<Edge<T, K>>();
		for (BipartiteNode<T> node : leftSet) {
			if (!node.matched) {

				if (augmentingPath(path, node, true)) {

					return path;
				}
			}
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	private boolean augmentingPath(ArrayList<Edge<T, K>> path,
			BipartiteNode<?> n, boolean left) {

		for (Edge<?, ?> e : n.getEdges()) {
			if (left) {
				if (!e.matched && !path.contains(e)) {
					path.add((Edge<T, K>) e);
					if (augmentingPath(path, e.getRight(), false)) {
						return true;
					} else {
						path.remove(e);
					}

				}
			} else if (!left) {
				if (!n.matched) {
					return true;
				} else if (e.matched && !path.contains(e)) {
					path.add((Edge<T, K>) e);
					if (augmentingPath(path, e.getLeft(), true)) {
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
