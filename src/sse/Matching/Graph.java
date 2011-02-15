package sse.Matching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import sse.Graph.CDWAG;
import sse.Graph.Node;

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

	public void constructFromCDWAG(CDWAG c) {
		source = c.source;
		sink = c.sink;
		for (sse.Graph.Edge e : c.source.getEdges()) {
			if (!e.getEnd().usedInMatching) {
				constructFromCDWAG(e.getEnd());
			}
		}

		// try {
		// BufferedWriter w = new BufferedWriter(new FileWriter("bitpartite"));
		// w.write("/* this is a generated dot file: www.graphviz.org */\n"
		// + "digraph suffixtree {\n"
		// + "\trankdir=LR\nnode[shape=box]\n");
		// for (BipartiteNode<T> root : rightSet) {
		// for (Edge e : root.getEdges()) {
		// String string = e.getLeft().getData() + "->"
		// + e.getRight().getData()+"100" + ";\n";
		// w.write(string);
		//
		// }
		// }
		// w.write("}");
		// w.close();
		// } catch (IOException e) {
		// System.out.println("File not found");
		// }

	}

	private void constructFromCDWAG(sse.Graph.Node n) {

		if (n == sink) {
			return;
		}
		BipartiteNode<T> newNode = new BipartiteNode<T>((T) n);
		leftSet.add(newNode);
		n.usedInMatching = true;
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
				ArrayList<BipartiteNode<?>> visited = new ArrayList<BipartiteNode<?>>();
				if (augmentingPath(path, node, true, visited)) {

					return path;
				}
			}
		}
		return null;

	}

	private boolean augmentingPath(ArrayList<Edge<T, K>> path,
			BipartiteNode<?> n, boolean left,
			ArrayList<BipartiteNode<?>> visited) {

		// boolean stop = false;
		// boolean b = true;
		// while (!stop || b) {
		//
		// b = false;
		// for (Edge<T, K> e : n.getEdges()) {
		// if (left) {
		// if (!e.matched) {
		// path.add(e);
		// left = !left;
		// n = e.getRight();
		// b = true;
		// break;
		// // return augmentingPath(path, e.getRight(), false);
		//
		// }
		// } else if (!left) {
		// if (!n.isMatched()) {
		// return true;
		// } else if (e.matched) {
		// path.add(e);
		// left = true;
		// n = e.getLeft();
		// b = true;
		// break;
		// //return augmentingPath(path, e.getLeft(), true);
		// }
		// }
		// }
		// stop = true;
		//
		// }
		// return false;
		visited.add(n);
		for (Edge<T, K> e : n.getEdges()) {
			if (left) {
				if (!e.matched && !visited.contains(e.getRight())) {
					path.add(e);
					return augmentingPath(path, e.getRight(), false, visited);

				}
			} else if (!left) {
				if (!n.matched) {
					return true;
				} else if (e.matched && !visited.contains(e.getLeft())) {
					path.add(e);
					return augmentingPath(path, e.getLeft(), true, visited);
				}
			}
		}
		return false;
	}

}
