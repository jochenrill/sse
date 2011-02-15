package sse.Vectors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

	public void printListOfVectors(File vectorFile) throws IOException {
		ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(
				vectorFile));
		graph.sink.setLocation(graph.text.length());
		Stack<Node> s = new Stack<Node>();
		s.push(graph.source);
		calculatePlaces();
		bipartiteMatching();
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
//		System.out.println("Adapt matching and collision detection");
		for (sse.Matching.Edge<Node, Integer> e : g.matching) {
			
//			// TODO: temporary collision detection, remove when fixed
//			if (places.contains(e.getRight().getData())) {
//				System.out.println("Collision on place "
//						+ e.getRight().getData());
//			} else {
//				places.add(e.getRight().getData());
//			}
			e.getLeft().getData()
					.setLocation(e.getRight().getData().intValue());

			e.getLeft().getData().setPlaces(null);
		}
	}

	private void calculatePlaces() {
		Stack<Node> s = new Stack<Node>();
		s.push(graph.source);
		while (!s.isEmpty()) {
			Node n = s.pop();
			for (Edge e : n.getEdges()) {
				if (e.getEnd() != graph.sink) {
					if (e.getEnd().getPlaces() == null) {
						e.getEnd().setPlaces(findPlace(e.getEnd()));
					}
					s.push(e.getEnd());

				}
			}
		}
	}

	private ArrayList<Integer> findPlace(Node n) {
		ArrayList<Integer> places = new ArrayList<Integer>();
		findPlace(n, 0, places);
		Collections.sort(places);
		n.setNumOccurs(places.size());
//		System.out.println("Node " + n +": "+places);
		// n.setPlaces(places);
		return places;
		/*
		 * int size = 0;
		 * 
		 * for (int i = 0; i < places.size(); i++) { if
		 * (!graph.isOccPosition[places.get(i) -1 + size]) {
		 * graph.isOccPosition[places.get(i) -1 + size] = true; return
		 * places.get(i) + size; } }
		 * 
		 * throw new IllegalStateException("No place for node " + n.getId() +
		 * " found");
		 */
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
			/*
			 * if (e.getEnd().getLocation() == -1) {
			 * e.getEnd().setLocation(findPlace(e.getEnd())); }
			 */
			EdgePosition p;
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1));
			r.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return r;
	}

	private SuffixVector printSuffixToFile(Node n) throws IOException {
		/*
		 * if (n.getLocation() == -1) { n.setLocation(findPlace(n)); }
		 */
		SuffixVector tmp = new SuffixVector(n.getLocation());
		tmp.setNumOccurs(n.getNumOccurs());
		tmp.setDepth(n.getLength());
		for (Edge e : n.getEdges()) {
			/*
			 * if (e.getEnd().getLocation() == -1) {
			 * e.getEnd().setLocation(findPlace(e.getEnd())); }
			 */
			EdgePosition p;
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1));
			tmp.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return tmp;
	}
}
