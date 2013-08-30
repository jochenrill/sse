package sse.Graph;

/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
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
	private int nodeCount = 0;
	public Node sink;

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

		// creates three start node the algorithm has to work with
		this.source = new Node(nodeCount++);
		sink = source;
		update();

		setNumOccurs();

	}

	private void setNumOccurs() {
		// set number of occurrences by traversing the graph post-order
		for (Node n : this) {
			if (n.hasEdges()) {
				for (char c : n.getEdges().keySet()) {
					n.setNumOccurs(n.getNumOccurs()
							+ n.getEdge(c).getNumOccurs());
				}
			} else {
				n.setNumOccurs(1);
			}
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
			for (char c : root.getEdges().keySet()) {
				writer.write(root.getId() + "[label=\"" + root.getId() + " ("
						+ root.getNumOccurs() + ")\"];\n");
				writer.write(root.getEdge(c) + "[label=\""
						+ root.getEdge(c).getId() + " ("
						+ root.getEdge(c).getNumOccurs() + ")\"];\n");
				String string = root.getId() + "->" + root.getEdge(c).getId()
						+ "\n";
				writer.write(string);

				writer.write("[" + "label=\"" + c + "\"];\n");

				if (!visited.contains(root.getEdge(c))) {
					printDot(root.getEdge(c), writer, visited);
				}
				visited.add(root.getEdge(c));
			}
		}
	}

	private void update() {

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			Node newSink = new Node(nodeCount++);

			sink.addEdge(c, newSink, true);

			// mark the natural edge. this is needed for decryption later on
			// primEdge.setNatural(true);

			Node w = sink.getSuffixLink();

			while (w != null && w.getEdge(c) == null) {
				w.addEdge(c, newSink, false);
				w = w.getSuffixLink();
			}

			Node v = null;
			if (w != null) {
				Node tmpE = w.getEdge(c);
				if (tmpE != null) {
					v = tmpE;
				}
			}

			if (w == null) {
				newSink.setSuffixLink(source);
			} else if (w.getEdge(c) != null && w.isEdgePrimary(c)) {
				newSink.setSuffixLink(v);
			} else {
				Node newNode = new Node(nodeCount++);

				if (v != null) {
					for (char cr : v.getEdges().keySet()) {
						newNode.addEdge(cr, v.getEdge(cr), false);
					}

				}

				w.removeEdge(c);
				w.addEdge(c, newNode, true);

				newSink.setSuffixLink(newNode);
				newNode.setSuffixLink(v.getSuffixLink());
				v.setSuffixLink(newNode);
				w = w.getSuffixLink();

				while (w != null && w.getEdge(c) != null && w.getEdge(c) == v
						&& !w.isEdgePrimary(c)) {

					w.removeEdge(c);
					w.addEdge(c, newNode, false);
					w = w.getSuffixLink();

				}

			}

			sink = newSink;
		}
	}

	public Iterator<Node> getDepthFirstIterator() {
		return new DepthFirstIterator(this);
	}

	@Override
	public Iterator<Node> iterator() {
		return getDepthFirstIterator();
	}

	public int size() {
		return nodeCount;
	}

}
