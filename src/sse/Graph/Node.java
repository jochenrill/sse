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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import sse.Constants;

/**
 * This class provides an node representation for the creation of the DAWG.
 * 
 * @author Jochen Rill
 * 
 */
public class Node {
	private HashMap<Character, Edge> edges;
	private Node suffixLink;
	private long id;
	private int numOccurs = 1;
	private int location;
	private long block;
	private HashSet<Integer> places;

	public Node getSuffixLink() {
		return suffixLink;
	}

	public void setSuffixLink(Node suffix) {
		this.suffixLink = suffix;
	}

	public Node(long id) {
		this.id = id;
		edges = new HashMap<Character, Edge>();
		places = new HashSet<Integer>();

	}

	public Collection<Edge> getEdges() {
		return edges.values();
	}

	public LinkedList<Edge> getEdgesList() {
		return new LinkedList<Edge>(edges.values());
	}

	public Edge getEdge(char c) {
		return edges.get(c);
	}

	public boolean addEdge(Edge e) {
		return edges.put(e.getEdgeLabel(), e) != null;

	}

	public boolean removeEdge(Edge e) {

		return edges.remove(e.getEdgeLabel()) != null;
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

	public long getBlock() {
		return block;
	}

	public void setBlock(long block) {
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

	public HashSet<Integer> getPlaces() {
		return places;
	}

	public void addPlace(Integer place) {
		this.places.add(place);
	}
}
