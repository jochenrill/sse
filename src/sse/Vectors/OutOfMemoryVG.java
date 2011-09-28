package sse.Vectors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import sse.Graph.CDWAG;
import sse.Graph.Edge;
import sse.Graph.Node;
import sse.Matching.Graph;

public class OutOfMemoryVG {
	private CDWAG graph;

	public OutOfMemoryVG(CDWAG graph) {
		this.graph = graph;
	}

	/**
	 * This method prints the suffix vectors to a given file in form of a java
	 * object stream
	 * 
	 * @param vectorFile
	 *            the file the vectors will be printed to
	 * @throws IOException
	 */
	public void printListOfVectors(File vectorFile) throws IOException {
		ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(
				vectorFile));
		graph.sink.setLocation(graph.text.length());

		// use a stack to traverse the tree
		Stack<Node> s = new Stack<Node>();
		s.push(graph.source);
		if (Constants.DEBUG)
			System.out.println("Calculating places");
		// calculate a list of all places for each node
		calculatePlaces();
		if (Constants.DEBUG)
			System.out.println("Calculating matching");
		// run a bipartite matching alogrithm
		if (Constants.EXACT_MATCHING) {
			bipartiteMatching();
		}
		while (!s.isEmpty()) {
			Node n = s.pop();
			n.visited = true;
			for (Edge e : n.getEdges()) {
				if (e.getEnd() != graph.sink) {
					if (!e.getEnd().hasVector()) {
						SuffixVector tmp = printSuffixToFile(e.getEnd());
						e.getEnd().setVector(true);
						o.writeObject(tmp);
						o.reset();
						tmp = null;
					}
					if (!e.getEnd().visited) {
						s.push(e.getEnd());
					}
				}
			}
		}
		SuffixVector rootVector = getRootVectorToFile();
		o.writeObject(rootVector);
		// Write a sentinel Object to locate the end of the stream
		o.writeObject(new String("EOF"));
		o.close();
	}

	private void bipartiteMatching() {
		Graph<Node, Integer> g = new Graph<Node, Integer>();
		g.constructFromCDWAG(graph);
		g.calculateMatching();
		ArrayList<Integer> places = new ArrayList<Integer>();
		// Adapt matching
		if (Constants.DEBUG)
			System.out.println("Adapt matching and collision detection");

		for (sse.Matching.Edge<Node, Integer> e : g.matching) {

			if (Constants.DEBUG) {
				if (places.contains(e.getRight().getData())) {
					System.out.println("Collision on place "
							+ e.getRight().getData());

				} else {
					places.add(e.getRight().getData());
				}
			}
			e.getLeft().getData()
					.setLocation(e.getRight().getData().intValue());

			e.getLeft().getData().setPlaces(null);
		}
	}

	private void calculatePlaces() {

		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
		Node n = graph.source;
		for (Edge e : n.getEdges()) {
			if (e.getEnd() != graph.sink) {
				calculatePlaces(visited, e.getEnd());

			}
		}
	}

	private void calculatePlaces(HashMap<Node, Boolean> visited, Node n) {

		n.setPlaces(findPlace(n));
		visited.put(n, true);

		if (!Constants.EXACT_MATCHING) {
			Random rnd = new Random();
			n.setLocation(n.getPlaces().get(rnd.nextInt(n.getPlaces().size())));
		}
		for (Edge e : n.getEdges()) {

			if (visited.get(e.getEnd()) == null && e.getEnd() != graph.sink) {
				calculatePlaces(visited, e.getEnd());
			}
		}
	}

	private ArrayList<Integer> findPlace(Node n) {
		ArrayList<Integer> places = new ArrayList<Integer>();
		findPlace(n, 0, places);
		Collections.sort(places);
		if (places.size() == 0) {
			throw new IllegalStateException("No place found for Node " + n);
		}
		n.setNumOccurs(places.size());

		return places;

	}

	private void findPlace(Node n, int pathLength, ArrayList<Integer> places) {
		for (Edge e : n.getEdges()) {
			if (e.getEnd() == graph.sink) {
				places.add(graph.text.length() - pathLength
						- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1));
			} else {
				findPlace(
						e.getEnd(),
						pathLength
								+ (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1),
						places);
			}
		}
	}

	private SuffixVector getRootVectorToFile() throws IOException {
		SuffixVector r = new SuffixVector(0);
		r.setDepth(0);
		for (Edge e : graph.source.getEdges()) {

			boolean sink = e.getEnd() == graph.sink;
			EdgePosition p;
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1), sink);
			r.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return r;
	}

	private SuffixVector printSuffixToFile(Node n) throws IOException {

		SuffixVector tmp = new SuffixVector(n.getLocation());
		tmp.setNumOccurs(n.getNumOccurs());
		tmp.setDepth(n.getLength());
		for (Edge e : n.getEdges()) {

			boolean sink = e.getEnd() == graph.sink;
			EdgePosition p;
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1), sink);
			tmp.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return tmp;
	}
}
