package sse.Vectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import sse.Graph.CDWAG;
import sse.Graph.Edge;
import sse.Graph.Node;

public class InMemoryVG {

	private CDWAG graph;

	public InMemoryVG(CDWAG graph) {
		this.graph = graph;

	}

	public ArrayList<SuffixVector> getListOfVectors() {
		ArrayList<SuffixVector> list = new ArrayList<SuffixVector>();
		graph.sink.setLocation(graph.text.length());
		Stack<Node> s = new Stack<Node>();
		s.push(graph.source);
		while (!s.isEmpty()) {
			Node n = s.pop();
			n.visited = true;
			for (Edge e : n.getEdges()) {
				if (e.getEnd() != graph.sink) {
					if (!e.getEnd().hasVector()) {
						SuffixVector tmp = printSuffix(e.getEnd());
						e.getEnd().setVector(true);
						list.add(tmp);
					}
					if (!e.getEnd().visited) {
						s.push(e.getEnd());
					}
				}
			}
		}
		SuffixVector rootVector = getRootVector();
		list.add(rootVector);
		return list;
	}

	private int findPlace(Node n) {
		ArrayList<Integer> places = new ArrayList<Integer>();
		findPlace(n, 0, places);
		Collections.sort(places);
		// int size = n.getLength() -1;
		int size = 0;
		for (int i = 0; i < places.size(); i++) {
			/*
			 * if (vectorMap.get(places.get(i) + size) == null) {
			 * vectorMap.put(places.get(i) + size, n); return places.get(i) +
			 * size; }
			 */
			if (!graph.isOccPosition[places.get(i) + size]) {
				graph.isOccPosition[places.get(i) + size] = true;
				return places.get(i) + size;
			}
		}
		return -1;
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

	private SuffixVector getRootVector() {
		SuffixVector r = new SuffixVector(0);
		r.setDepth(0);
		for (Edge e : graph.source.getEdges()) {
			if (e.getEnd().getLocation() == -1) {
				e.getEnd().setLocation(findPlace(e.getEnd()));
			}
			EdgePosition p;
			/*
			 * if (mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)) == null) {
			 */
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1));
			/*
			 * mapOfPositions.put(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1), p);
			 */
			graph.listOfEdges.add(p);

			/*
			 * } else { p = mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)); }
			 */
			r.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return r;
	}

	private SuffixVector printSuffix(Node n) {
		if (n.getLocation() == -1) {
			n.setLocation(findPlace(n));
		}
		SuffixVector tmp = new SuffixVector(n.getLocation());
		tmp.setDepth(n.getLength());
		for (Edge e : n.getEdges()) {
			if (e.getEnd().getLocation() == -1) {
				e.getEnd().setLocation(findPlace(e.getEnd()));
			}
			EdgePosition p;
			/*
			 * if (mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)) == null) {
			 */
			p = new EdgePosition(e.getEnd().getLocation()
					- (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1));
			/*
			 * mapOfPositions.put(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1), p);
			 */
			graph.listOfEdges.add(p);

			/*
			 * } else { p = mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)); }
			 */
			tmp.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
		}
		return tmp;
	}

	public ArrayList<EdgePosition> getListOfEdges() {
		return graph.listOfEdges;
	}

}
