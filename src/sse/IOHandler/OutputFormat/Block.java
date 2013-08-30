package sse.IOHandler.OutputFormat;

import java.io.Serializable;
import java.util.ArrayList;

public class Block implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1435903327118960257L;
	private ArrayList<Edge> edges;
	private int numOccurs;

	public Block(int numOccurs) {
		this.edges = new ArrayList<Edge>();
		this.numOccurs = numOccurs;
	}

	public void addEdge(Edge e) {
		edges.add(e);
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public int getNumOccurs() {
		return numOccurs;
	}

}
