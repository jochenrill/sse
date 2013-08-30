package sse.Graph;

/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/

import java.util.HashMap;

import gnu.trove.list.array.TCharArrayList;
import gnu.trove.map.hash.TCharObjectHashMap;

/**
 * This class provides an node representation for the creation of the DAWG.
 * 
 * @author Jochen Rill
 * 
 */
public class Node {
	// private TCharObjectHashMap<Node> edges;
	private HashMap<Character, Node> edges;
	private TCharArrayList primary;
	private Node suffixLink;
	private int id;
	private int numOccurs = 0;
	private int block;
	private int index;

	public Node getSuffixLink() {
		return suffixLink;
	}

	public void setSuffixLink(Node suffix) {
		this.suffixLink = suffix;
	}

	public Node(int id) {
		this.id = id;
		// edges = new TCharObjectHashMap<Node>();+
		edges = new HashMap<Character, Node>();
		// places = new TIntLinkedList();

		primary = new TCharArrayList();

	}

	public HashMap<Character,Node> getEdges() {
		return edges;
	}

	public Node getEdge(char c) {
		return edges.get(c);
	}

	public boolean addEdge(char c, Node n, boolean primary) {
		if (primary) {
			this.primary.add(c);
		}
		return edges.put(c, n) != null;

	}

	public void setEdgePrimary(char c, boolean flag) {
		if (flag) {
			primary.add(c);
		} else {
			primary.remove(c);
		}
	}

	public boolean isEdgePrimary(char c) {
		return primary.contains(c);
	}

	public boolean removeEdge(char c) {

		return edges.remove(c) != null;
	}

	@Override
	public String toString() {
		return Long.toString(this.id);
	}

	public int getId() {
		return id;
	}

	public void setNumOccurs(int numOccurs) {
		this.numOccurs = numOccurs;
	}

	public int getNumOccurs() {
		return numOccurs;
	}

	public boolean hasEdges() {
		return edges.size() != 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return (((Node) o).getId() == id);
		} else {
			return false;
		}
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
