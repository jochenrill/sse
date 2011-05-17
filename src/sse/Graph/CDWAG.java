package sse.Graph;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import sse.Vectors.EdgePosition;

/**
 * This class represents and constructs the CDWAG (also called
 * "compact suffix automaton") for a given text. The algorithm used runs in
 * O(n).
 * 
 * @author Jochen Rill
 * 
 */
public class CDWAG {
	/**
	 * Contains the text the graph was constructed for.
	 */
	public String text;
	/**
	 * Contains the root node.
	 */
	public Node source;
	/**
	 * Contains the final node.
	 */
	public Node bottom;
	public Node sink;
	public long nodeCount = 0;

	/**
	 * Contains the list of EdgePositions for obtaining the SuffixVectors later
	 * on.
	 */
	// public ArrayList<EdgePosition> listOfEdges = new
	// ArrayList<EdgePosition>();

	/**
	 * Initializes and constructs the graph for a given text.
	 * 
	 * The constructor initializes the needed variables and constructs the
	 * graph.
	 * 
	 * @param text
	 *            The text the graph will be constructed for.
	 */
	public CDWAG(String text) {
		this.text = text;

		// creates three start node the algorithm has to work with
		this.source = new Node(nodeCount++);
		this.sink = new Node(nodeCount++);
		this.bottom = new Node(nodeCount++);
		for (int i = 0; i < text.length(); i++) {
			Edge newEdge = new Edge(i, i, bottom, source);
			bottom.addEdge(text.charAt(i), newEdge);
		}
		source.setLength(0);
		bottom.setLength(-1);
		source.setSuffixLink(bottom);
		Node s = source;
		int k = 0;
		for (int i = 0; i < text.length(); i++) {
			Pair result = update(s, k, i);
			s = result.s;
			k = result.k;
			result = canonize(s, k, i);
			s = result.s;
			k = result.k;
		}
	}

	/**
	 * Write the graph to a file.
	 * 
	 * This method prints the graph to a file in "dot representation". It can be
	 * viewed with "dotty" in the graphviz package. This only works for graphs
	 * with reasonable sizes.
	 * 
	 * @param fileName
	 *            The name of the file the graph will be printed to.
	 */
	public void printToFile(String fileName) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
			w.write("/* this is a generated dot file: www.graphviz.org */\n"
					+ "digraph suffixtree {\n"
					+ "\trankdir=LR\nnode[shape=box]");
			printDot(source, w, new LinkedList<Node>());
			w.write("}");
			w.close();
		} catch (IOException e) {
			System.out.println("File not found");
		}
	}

	// Recursive method to traverse the tree
	private void printDot(Node root, BufferedWriter writer,
			LinkedList<Node> visited) throws IOException {
		if (root.getEdges() != null) {
			for (Edge e : root.getEdges()) {
				String string = root.getId() + "->" + e.getEnd().getId() + "\n";
				writer.write(string);
				if (e.getEdgeLabelEnd() == e.getEdgeLabelStart()) {
					writer.write("[" + "label=\""
							+ text.charAt(e.getEdgeLabelStart()) + "["
							+ e.getEdgeLabelStart() + "," + e.getEdgeLabelEnd()
							+ "]" + "\"];\n");
				} else {
					writer.write("["
							+ "label=\""
							+ text.substring(e.getEdgeLabelStart(),
									e.getEdgeLabelEnd() + 1) + "["
							+ e.getEdgeLabelStart() + ","
							+ (e.getEdgeLabelEnd()) + "]" + "\"];\n");
				}
				if (!visited.contains(e.getEnd())) {
					printDot(e.getEnd(), writer, visited);
				}
				visited.add(e.getEnd());
			}
		}
	}

	private Pair update(Node s, int k, int i) {
		Node e = null;
		Node oldr = null;
		Node r = null;
		char c = text.charAt(i);
		while (!checkEndPoint(s, k, i - 1, c)) {
			if (k <= i - 1) {
				if (e == extension(s, k, i - 1)) {
					redirect(s, k, i - 1, r);
					Pair result = canonize(s.getSuffixLink(), k, i - 1);
					s = result.s;
					k = result.k;
					continue;
				} else {
					e = extension(s, k, i - 1);
					r = splitEdge(s, k, i - 1);
				}
			} else {
				r = s;
			}
			Edge newEdge = new Edge(i, text.length() - 1, r, sink);
			r.addEdge(text.charAt(i), newEdge);
			if (oldr != null) {
				oldr.setSuffixLink(r);
			}
			oldr = r;
			Pair result = canonize(s.getSuffixLink(), k, i - 1);
			s = result.s;
			k = result.k;
		}
		if (oldr != null) {
			oldr.setSuffixLink(s);
		}
		return separateNode(s, k, i);
	}

	private Pair separateNode(Node s, int k, int p) {
		Pair result = canonize(s, k, p);
		Node s1 = result.s;
		int k1 = result.k;
		if (k1 <= p) {
			return new Pair(s1, k1);
		}
		if (s1.getLength() == s.getLength() + (p - k + 1)) {
			return new Pair(s1, k1);
		}
		Node r1 = s1.duplicateLazy(nodeCount++);
		r1.setSuffixLink(s1.getSuffixLink());
		s1.setSuffixLink(r1);
		r1.setLength(s.getLength() + (p - k + 1));
		do {
			Edge endEdge = s.getEdge(text.charAt(k));
			Edge newEdge = new Edge(k, p, s, r1);
			s.addEdge(text.charAt(k), newEdge);
			s.removeEdge(endEdge);
			Pair pair = canonize(s.getSuffixLink(), k, p - 1);
			s = pair.s;
			k = pair.k;
		} while (new Pair(s1, k1).equals(canonize(s, k, p)));
		return new Pair(r1, p + 1);
	}

	private Node splitEdge(Node s, int k, int p) {
		Edge endEdge = s.getEdge(text.charAt(k));
		int k1 = endEdge.getEdgeLabelStart();
		int p1 = endEdge.getEdgeLabelEnd();
		Node r = new Node(nodeCount++);
		Edge newEdge1 = new Edge(k1, k1 + p - k, s, r);
		Edge newEdge2 = new Edge(k1 + p - k + 1, p1, r, endEdge.getEnd());
		s.addEdge(text.charAt(k1), newEdge1);
		r.addEdge(text.charAt(k1 + p - k + 1), newEdge2);
		s.removeEdge(endEdge);
		r.setLength(s.getLength() + (p - k + 1));
		return r;
	}

	private Pair canonize(Node s, int k, int p) {
		if (k > p) {
			return new Pair(s, k);
		} else {
			Edge endEdge = s.getEdge(text.charAt(k));
			int p1 = endEdge.getEdgeLabelEnd();
			int k1 = endEdge.getEdgeLabelStart();
			Node s1 = endEdge.getEnd();
			while (p1 - k1 <= p - k) {
				k = k + p1 - k1 + 1;
				s = s1;
				if (k <= p) {
					endEdge = s.getEdge(text.charAt(k));
					p1 = endEdge.getEdgeLabelEnd();
					k1 = endEdge.getEdgeLabelStart();
					s1 = endEdge.getEnd();
				}
			}
			return new Pair(s, k);
		}
	}

	private void redirect(Node s, int k, int p, Node r) {
		Edge endEdge = s.getEdge(text.charAt(k));
		Edge newEdge = new Edge(endEdge.getEdgeLabelStart(),
				endEdge.getEdgeLabelStart() + p - k, s, r);
		s.addEdge(text.charAt(endEdge.getEdgeLabelStart()), newEdge);
		s.removeEdge(endEdge);
	}

	private Node extension(Node s, int k, int p) {
		if (k > p) {
			return s;
		} else {
			Edge endEdge = s.getEdge(text.charAt(k));
			return endEdge.getEnd();
		}
	}

	private boolean checkEndPoint(Node s, int k, int p, char c) {
		if (k <= p) {
			Edge endEdge = s.getEdge(text.charAt(k));
			return (c == text.charAt(endEdge.getEdgeLabelStart() + p - k + 1));
		} else {
			Edge endEdge = s.getEdge(c);
			return endEdge != null;
		}
	}

	public int edgeCount() {
		LinkedList<Node> l = new LinkedList<Node>();
		HashMap<Node, Integer> m = new HashMap<Node, Integer>();
		edgeCount(source, m);
		int count = 0;
		for (Integer n : m.values()) {
			count += n;
		}
		return count;
	}

	private void edgeCount(Node n, HashMap<Node, Integer> visited) {

		if (n != sink) {

			visited.put(n, n.getEdges().size());
		}
		for (Edge e : n.getEdges()) {
			edgeCount(e.getEnd(), visited);
		}

	}

}
