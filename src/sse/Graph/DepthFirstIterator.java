package sse.Graph;

import java.util.ArrayList;
import java.util.Iterator;

public class DepthFirstIterator implements Iterator<Node> {

	private ArrayList<Node> nodeList;

	private int index = 0;

	public DepthFirstIterator(DAWG graph) {
		this.nodeList = new ArrayList<Node>();
		expand(graph.source, new boolean[graph.size()]);
	}

	@Override
	public boolean hasNext() {
		return index < nodeList.size();
	}

	@Override
	public Node next() {

		return nodeList.get(index++);
	}

	@Override
	public void remove() {
		nodeList.remove(index);
	}

	private void expand(Node n, boolean visited[]) {
		visited[n.getId()] = true;
		for (char c : n.getEdges()) {
			if (!visited[n.getEdge(c).getId()]) {
				expand(n.getEdge(c), visited);
			}
		}
		nodeList.add(n);
	}

}
