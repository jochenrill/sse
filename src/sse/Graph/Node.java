package sse.Graph;
import java.util.Collection;
import java.util.Hashtable;

public class Node {
    private Hashtable<Character, Edge> edges;
    private Node suffixLink;
    private int length;
    private long id;
    private int location = -1;
    public boolean visited = false;
    private boolean hasVector = false;

    public int getLocation() {
        return this.location;
    }
    
    public void setVector(boolean value){
    	hasVector = value;
    }

    public boolean hasVector() {
    	return hasVector;
    }
    public void setLocation(int loc) {
        this.location = loc;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Node getSuffixLink() {
        return suffixLink;
    }

    public void setSuffixLink(Node suffix) {
        this.suffixLink = suffix;
    }

    public Node(long id) {
        this.id = id;
        edges = new Hashtable<Character, Edge>();
        // locations = new LinkedList<Integer>();
    }

    @SuppressWarnings("unchecked")
	public Node duplicateLazy(long nodeCount) {
        Node d = new Node(nodeCount);
        d.edges = (Hashtable<Character, Edge>) this.edges.clone();
        d.suffixLink = this.suffixLink;
        d.length = this.length;
        // d.locations = (LinkedList<Integer>) this.locations.clone();
        return d;
    }

    public Collection<Edge> getEdges() {
        return edges.values();
    }

    public Edge getEdge(char c) {
        return edges.get(c);
    }

    public boolean addEdge(char c, Edge e) {
        return (edges.put(c, e) == null);
    }

    public boolean removeEdge(Edge e) {
        return (edges.values().remove(e));
    }

    @Override
    public String toString() {
        return Long.toString(this.id);
    }

    public long getId() {
        return id;
    }

  
}
