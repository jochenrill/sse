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

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import sse.Backend.Backend;
import sse.Graph.DAWG;
import sse.Graph.Node;
import sse.IOHandler.OutputFormat.Block;
import sse.IOHandler.OutputFormat.Edge;

/**
 * This class provides the algorithms used for writing out the encrypted data.
 * It manages block allocation, as well as representation of the information in
 * each blocks. For it to work, it needs a correctly implemented backend.
 * 
 * @author Jochen Rill
 * 
 */
public class EncryptionEngine {

	private Backend backend;

	public EncryptionEngine(Backend backend) {

		this.backend = backend;

	}

	public void writeBlocks(DAWG graph) throws IOException {

		// generate only 100 blocks max
		int threshhold = (graph.size() / 100) + 1;

		LinkedList<Integer> blockNumbers = new LinkedList<Integer>();

		int blockNumber = (100 < graph.size()) ? 100 : graph.size();
		for (int i = 1; i <= blockNumber; i++) {
			blockNumbers.add(i);
		}
		Collections.shuffle(blockNumbers, new SecureRandom());

		// important: Graph needs to implements a depth-first iterator!
		int nodesInBlock = threshhold;
		ArrayList<Block> blockList = new ArrayList<Block>();
		int block = -1;

		for (Node n : graph) {

			if (n != graph.source) {
				if (nodesInBlock == threshhold) {
					// draw new block number and write blockArray
					if (block != -1) {
						// this is the first run, dont write anything
						backend.writeBlockArray(blockList, block);
						blockList.clear();
					}

					block = blockNumbers.pop();

					nodesInBlock = 0;

				}

				Block b = new Block(n.getNumOccurs());
				n.setBlock(block);
				n.setIndex(nodesInBlock);
				for (char c : n.getEdges()) {
					int targetBlock = n.getEdge(c).getBlock();
					int targetIndex = n.getEdge(c).getIndex();
					b.addEdge(new Edge(c, targetBlock, targetIndex));
				}
				if (n == graph.source) {
					// make sure that the root node is always on position 0
					blockList.add(0, b);
				} else {
					blockList.add(b);
				}
				nodesInBlock++;
			}
		}

		// write last block
		backend.writeBlockArray(blockList, block);

		// write source node to block 0
		Block b = new Block(0);
		for (char c : graph.source.getEdges()) {
			b.addEdge(new Edge(c, graph.source.getEdge(c).getBlock(),
					graph.source.getEdge(c).getIndex()));
		}
		backend.writeBlock(b, 0);
	}
}
