package sse.IOHandler;

import java.util.LinkedList;

import sse.Graph.Node;

/**
 * This class represents a block used for allocation of nodes to blocks in the
 * BinaryWriter class.
 * 
 * @author Jochen Rill
 * 
 */
public class Block {

	private int size;
	private int bytesIncluded;
	private LinkedList<Node> nodes;
	private int id;

	public Block(int size, int id) {
		this.setSize(size);
		this.setBytesIncluded(0);
		nodes = new LinkedList<Node>();
		this.setId(id);

	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getBytesIncluded() {
		return bytesIncluded;
	}

	public void setBytesIncluded(int bytesIncluded) {
		this.bytesIncluded = bytesIncluded;
	}

	public boolean addNode(Node n) {
		bytesIncluded += n.getSize();
		return nodes.add(n);
	}

	public LinkedList<Node> getNodes() {
		return nodes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
