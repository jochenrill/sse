package sse.Graph;
public class Edge {
    private int edgeLabelStart;
    private int edgeLabelEnd;
    private Node start;
    private Node end;

    public Edge(int edgeLabelStart, int edgeLabelEnd, Node start, Node end) {
        this.edgeLabelStart = edgeLabelStart;
        this.edgeLabelEnd = edgeLabelEnd;
        this.start = start;
        this.end = end;
    }

    public Edge(int beginIndex, int endIndex, Node startNode, long id) {
        this.edgeLabelStart = beginIndex;
        this.edgeLabelEnd = endIndex;
        this.start = startNode;
        this.end = new Node(id);
    }

    public int getEdgeLabelStart() {
        return edgeLabelStart;
    }

    public int getEdgeLabelEnd() {
        return edgeLabelEnd;
    }

    public int getSpan() {
        return edgeLabelStart - edgeLabelEnd;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }
}
