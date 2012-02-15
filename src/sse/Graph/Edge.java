package sse.Graph;

public class Edge {
	private char edgeLabel;

	private Node start;
	private Node end;
	private boolean primary;
	private boolean natural;

	public Edge(char edgeLabel, Node start, Node end) {
		this.edgeLabel = edgeLabel;

		this.start = start;
		this.end = end;
		this.primary = true;
	}

	public char getEdgeLabel() {
		return edgeLabel;
	}

	public Node getStart() {
		return start;
	}

	public Node getEnd() {
		return end;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public boolean isNatural() {
		return natural;
	}

	public void setNatural(boolean natural) {
		this.natural = natural;
	}
}
