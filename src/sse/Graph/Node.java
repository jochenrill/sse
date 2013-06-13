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
package sse.Graph;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import sse.Constants;

public class Node {
	private LinkedList<Edge> edges;
	private Node suffixLink;
	private long id;
	private int numOccurs = 1;
	private int location;
	private int block;
	private LinkedHashSet<Integer> places;

	public Node getSuffixLink() {
		return suffixLink;
	}

	public void setSuffixLink(Node suffix) {
		this.suffixLink = suffix;
	}

	public Node(long id) {
		this.id = id;
		edges = new LinkedList<Edge>();
		places = new LinkedHashSet<Integer>();

	}

	public LinkedList<Edge> getEdges() {
		return edges;
	}

	public Edge getEdge(char c) {
		for (Edge e : edges) {
			if (e.getEdgeLabel() == c) {
				return e;
			}
		}
		return null;
	}

	public Edge getEdge(Node n) {
		for (Edge e : edges) {
			if (e.getEnd() == n) {
				return e;
			}
		}
		return null;
	}

	public boolean addEdge(Edge e) {
		return (edges.add(e));
	}

	public boolean removeEdge(Edge e) {

		return (edges.remove(e));
	}

	@Override
	public String toString() {
		return Long.toString(this.id);
	}

	public long getId() {
		return id;
	}

	public void setNumOccurs(int numOccurs) {
		this.numOccurs = numOccurs;
	}

	public int getNumOccurs() {
		return numOccurs;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return (((Node) o).getId() == id);
		} else {
			return false;
		}
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getSize() {

		/*
		 * 1 bytes for end marker, then every edge has to contain its
		 * label (one character) the block number and the position in the block
		 * it is leading to as well as the number of occurrences that specific
		 * suffix has
		 */
		return 1
				+ edges.size()
				* (1 + Constants.BLOCK_REFERENCE_BYTES + Constants.EDGE_REFERENCE_BYTES)
				+ Constants.NUMOCCURS_BYTES;
	}

	public LinkedHashSet<Integer> getPlaces() {
		return places;
	}

	public void addPlace(Integer place) {
		this.places.add(place);
	}
}
