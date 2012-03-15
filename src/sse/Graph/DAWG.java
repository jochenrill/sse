package sse.Graph;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class represents and constructs the DWAG (also called
 * "compact suffix automaton") for a given text. The algorithm used runs in O(n)
 * and uses the algorithm by Blumer et. al. In addition the different
 * equivalence sets are calculated on the fly, as well as the natural edge
 * labeling.
 * 
 * @author Jochen Rill
 * 
 */
public class DAWG implements Iterable<Node> {
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
	public long nodeCount = 0;
	public Node sink;

	private int length;
	private LinkedList<Node> nodeList;

	/**
	 * Initializes and constructs the graph for a given text.
	 * 
	 * The constructor initializes the needed variables and constructs the
	 * graph.
	 * 
	 * @param text
	 *            The text the graph will be constructed for.
	 */
	public DAWG(String text) {
		this.text = text;
		this.length = text.length();
		this.nodeList = new LinkedList<Node>();
		// creates three start node the algorithm has to work with
		this.source = new Node(nodeCount++);
		for (int i = -1; i < text.length(); i++) {
			source.addPlace(i);
		}
		nodeList.add(source);
		sink = source;
		update();

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

				writer.write("[" + "label=\"" + e.getEdgeLabel() + "["
						+ (e.isNatural() ? "*" : "") + "]" + "\"];\n");

				if (!visited.contains(e.getEnd())) {
					printDot(e.getEnd(), writer, visited);
				}
				visited.add(e.getEnd());
			}
		}
	}

	private void update() {

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			Node newSink = new Node(nodeCount++);
			nodeList.add(newSink);

			Edge primEdge = new Edge(c, sink, newSink);
			sink.addEdge(primEdge);

			// mark the natural edge. this is needed for decryption later on
			primEdge.setNatural(true);

			/*
			 * for (Integer z : sink.getPlaces()) { if ((z + 1) < length &&
			 * text.charAt(z + 1) == c) { newSink.addPlace(z + 1); }
			 * 
			 * }
			 */
			updatePlaces(sink, newSink, c);

			Node w = sink.getSuffixLink();

			while (w != null && w.getEdge(c) == null) {
				Edge secEdge = new Edge(c, w, newSink);
				secEdge.setPrimary(false);
				w.addEdge(secEdge);
				w = w.getSuffixLink();
			}

			Node v = null;
			if (w != null) {
				Edge tmpE = w.getEdge(c);
				if (tmpE != null) {
					v = tmpE.getEnd();
				}
			}

			if (w == null) {
				newSink.setSuffixLink(source);
			} else if (w.getEdge(c) != null && w.getEdge(c).isPrimary()) {
				newSink.setSuffixLink(v);
			} else {
				Node newNode = new Node(nodeCount++);

				nodeList.add(newNode);
				if (v != null) {
					for (Edge e : v.getEdges()) {
						Edge secEdge = new Edge(e.getEdgeLabel(), newNode,
								e.getEnd());
						secEdge.setPrimary(false);
						newNode.addEdge(secEdge);
					}
				}
				Edge tmpEdge = w.getEdge(c);
				Edge primEdge2 = new Edge(tmpEdge.getEdgeLabel(), w, newNode);
				w.removeEdge(tmpEdge);
				w.addEdge(primEdge2);

				/*
				 * for (Integer z : w.getPlaces()) { if ((z + 1) < length &&
				 * text.charAt(z + 1) == primEdge2.getEdgeLabel()) {
				 * newNode.addPlace(z + 1); }
				 * 
				 * }
				 */
				updatePlaces(w, newNode, primEdge2.getEdgeLabel());

				newSink.setSuffixLink(newNode);
				newNode.setSuffixLink(v.getSuffixLink());
				v.setSuffixLink(newNode);
				w = w.getSuffixLink();

				while (w != null && w.getEdge(c) != null
						&& w.getEdge(c).getEnd() == v
						&& !w.getEdge(c).isPrimary()) {
					Edge tEdge = w.getEdge(c);
					Edge nEdge = new Edge(tEdge.getEdgeLabel(),
							tEdge.getStart(), newNode);
					nEdge.getStart().removeEdge(tEdge);

					nEdge.getStart().addEdge(nEdge);

					nEdge.setPrimary(false);
					w = w.getSuffixLink();

				}

			}

			sink = newSink;
		}
	}

	private void updatePlaces(Node w, Node newNode, char c) {
		for (Integer z : w.getPlaces()) {
			if ((z + 1) < length && text.charAt(z + 1) == c) {
				newNode.addPlace(z + 1);
			}

		}
	}

	public void storeUniquePaths(boolean paths) {

		/*
		 * It seems to be a bad idea to count every path in the DAWG because the
		 * graph is much bigger than the CDAWG. Fortunately we have an
		 * alternative method of updating the equivalence classes while creating
		 * the graph
		 */
		if (paths) {
			for (Node n : nodeList) {
				if (n != sink && n != source) {
					n.setNumOccurs((findPlace(n)));
				}
			}
		} else {

			for (Node n : nodeList) {
				if (n == source) {
					n.setNumOccurs(0);
				} else if (n == sink) {
					n.setNumOccurs(1);
				} else {
					n.setNumOccurs(n.getPlaces().size());
				}
			}
		}
	}

	private int findPlace(Node n) {
		ArrayList<Integer> places = new ArrayList<Integer>();
		findPlace(n, places);

		if (places.size() == 0) {
			throw new IllegalStateException("No place found for Node " + n);
		}

		return places.size();

	}

	private void findPlace(Node n, ArrayList<Integer> places) {
		for (Edge e : n.getEdges()) {

			while (e.getEnd().getEdges().size() == 1) {
				e = e.getEnd().getEdgesList().getFirst();
			}
			if (e.getEnd() == sink) {
				places.add(1);
			} else {
				findPlace(e.getEnd(), places);
			}

		}
	}

	@Override
	public Iterator<Node> iterator() {
		return toList().iterator();
	}

	@SuppressWarnings("unchecked")
	public LinkedList<Node> toList() {
		return (LinkedList<Node>) nodeList.clone();
	}

}
