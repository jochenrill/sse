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
	
	public boolean isMatched(){
		for(Edge e : edges){
			if(e.matched){
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString(){
		return data.toString();
	}

}
