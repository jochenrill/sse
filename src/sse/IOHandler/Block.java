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

	private long size;
	private long bytesIncluded;
	private LinkedList<Node> nodes;
	private long id;

	public Block(long size, long id) {
		this.setSize(size);
		this.setBytesIncluded(0);
		nodes = new LinkedList<Node>();
		this.setId(id);

	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getBytesIncluded() {
		return bytesIncluded;
	}

	public void setBytesIncluded(long bytesIncluded) {
		this.bytesIncluded = bytesIncluded;
	}

	public boolean addNode(Node n) {
		bytesIncluded += n.getSize();
		return nodes.add(n);
	}

	public LinkedList<Node> getNodes() {
		return nodes;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
