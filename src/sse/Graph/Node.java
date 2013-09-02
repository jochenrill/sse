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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * This class provides an node representation for the creation of the DAWG.
 * 
 * @author Jochen Rill
 * 
 */
public class Node {

	private BitSet edgeVector;
	private BitSet primaryVector;
	private NodeList nodeArray;

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

		nodeArray = new NodeList();
		edgeVector = new BitSet();
		primaryVector = new BitSet();

	}

	public LinkedList<Character> getEdges() {
		LinkedList<Character> edgeList = new LinkedList<Character>();
		for (char i = 0; i <= 255; i++) {
			if (edgeVector.get(i)) {
				edgeList.add(i);
			}
		}
		return edgeList;

	}

	public Node getEdge(char c) {
		if (edgeVector.get(c)) {
			return nodeArray.get(getPosition(c) - 1);
		} else {
			return null;
		}
	}

	public void addEdge(char c, Node n, boolean primary) {
		if (primary) {
			setEdgePrimary(c, true);
		}

		edgeVector.set(c);

		nodeArray.add(getPosition(c) - 1, n);

	}

	public void setEdgePrimary(char c, boolean flag) {
		primaryVector.set(c, flag);
	}

	public boolean isEdgePrimary(char c) {

		return primaryVector.get(c);
	}

	public void removeEdge(char c) {

		if (edgeVector.get(c)) {
			nodeArray.remove(getPosition(c) - 1);
			edgeVector.set(c, false);
		}

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
		return !edgeVector.isEmpty();
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

	private int getPosition(char c) {
		BitSet mask = new BitSet(c);
		mask.set(0, c, true);
		BitSet result = (BitSet) edgeVector.clone();
		result.andNot(mask);
		return result.cardinality();
	}
}
