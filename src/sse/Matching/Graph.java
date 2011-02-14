package sse.Matching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import sse.Graph.CDWAG;

public class Graph<T, K> {

	private ArrayList<BipartiteNode<T>> rightSet;
	public ArrayList<Edge<T, K>> matching;
	private sse.Graph.Node source;
	private sse.Graph.Node sink;

	public Graph() {
		rightSet = new ArrayList<BipartiteNode<T>>();
		matching = new ArrayList<Edge<T, K>>();
	}

	public void constructFromCDWAG(CDWAG c) {
		source = c.source;
		sink = c.sink;
		for (sse.Graph.Edge e : c.source.getEdges()) {
			if (!e.getEnd().usedInMatching) {
				constructFromCDWAG(e.getEnd());
			}
		}

	}

	private void constructFromCDWAG(sse.Graph.Node n) {

		if (n == sink) {
			return;
		}
		BipartiteNode<T> newNode = new BipartiteNode<T>((T) n);
		rightSet.add(newNode);
		n.usedInMatching = true;
		for (Integer i : n.getPlaces()) {
			BipartiteNode<K> rightNode = new BipartiteNode<K>((K) i);
			Edge<T, K> newEdge = new Edge<T, K>();
			newEdge.setLeft(newNode);
			newEdge.setRight(rightNode);
			newNode.addEdge(newEdge);
		}

		for (sse.Graph.Edge e : n.getEdges()) {
			if (!e.getEnd().usedInMatching) {
				constructFromCDWAG(e.getEnd());
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
		// test print matching
		
	/*	for (Edge<T, K> e : matching) {
			System.out.println("Node "
					+ ((sse.Graph.Node) e.getLeft().getData()).getId()
					+ " matched with " + ((Integer) e.getRight().getData()));
		}*/
	}

	private ArrayList<Edge<T, K>> augmentingPath() {
		ArrayList<Edge<T, K>> path = new ArrayList<Edge<T, K>>();
		for (BipartiteNode<T> node : rightSet) {
			if (!node.matched) {
				augmentingPath(path, node, true);
				return path;
			}
		}
		return null;

	}

	private boolean augmentingPath(ArrayList<Edge<T, K>> path, BipartiteNode<T> n,
			boolean right) {

		for (Edge<T, K> e : n.getEdges()) {
			if (right) {
				if (!e.matched && !e.getRight().matched) {

					path.add(e);
					return true;
				} else if (!e.matched) {
					augmentingPath(path, n, !right);
				} else {
					return false;
				}

			} else if (!right) {
				if (e.matched) {
					path.add(e);
					augmentingPath(path, n, !right);
				} else {
					return false;
				}
			}
		}
		return false;
	}

}
