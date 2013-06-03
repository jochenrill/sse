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

public class Edge {
	private char edgeLabel;

	private Node start;
	private Node end;
	private boolean primary;

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
}
