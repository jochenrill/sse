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

public class OutOfMemoryVG {

	private CDWAG graph;

	public OutOfMemoryVG(CDWAG graph) {
		this.graph = graph;

	}

	public void printListOfVectors(File vectorFile) throws IOException {

		ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(
				vectorFile));
		// this.edgeFile = new ObjectOutputStream(new FileOutputStream(new File(
		// edgeFile)));

		graph.sink.setLocation(graph.text.length());
		Stack<Node> s = new Stack<Node>();
		s.push(graph.source);
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
		// this.edgeFile.writeObject(new String("EOF"));
		o.close();
		// this.edgeFile.close();

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

	private SuffixVector getRootVectorToFile() throws IOException {
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
			// listOfEdges.add(p);

			/*
			 * } else { p = mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)); }
			 */
			r.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
			// edgeFile.writeObject(p);
		}
		return r;
	}

	private SuffixVector printSuffixToFile(Node n) throws IOException {
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
			// listOfEdges.add(p);

			/*
			 * } else { p = mapOfPositions.get(e.getEnd().getLocation() -
			 * (e.getEdgeLabelEnd() - e.getEdgeLabelStart() + 1)); }
			 */
			tmp.addEdge(graph.text.charAt(e.getEdgeLabelStart()), p);
			// edgeFile.writeObject(p);

		}
		return tmp;
	}
}
