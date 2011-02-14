package sse.Matching;

import java.util.ArrayList;

public class BipartiteNode<T> {
	private T data;
	private ArrayList<Edge> edges;
	public boolean matched;

	public BipartiteNode(T data) {
		this.data = data;
		edges = new ArrayList<Edge>();
	}

	public T getData() {
		return data;
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BipartiteNode) {
			BipartiteNode tmp = (BipartiteNode) o;
			return ((BipartiteNode) o).getData().equals(this.getData());
		} else {
			return false;
		}
	}
}
