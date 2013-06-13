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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import sse.Constants;
import sse.Backend.Backend;
import sse.Graph.Edge;
import sse.Graph.Node;

/**
 * This class provides the algorithms used for writing out the encrypted data.
 * It manages block allocation, as well as representation of the information in
 * each blocks. For it to work, it needs a correctly implemented backend.
 * 
 * @author Jochen Rill
 * 
 */
public class BinaryWriter {
	private DataOutputStream w;

	private Backend backend;

	public BinaryWriter(Backend backend, char[] password) {

		this.backend = backend;
		backend.setSecurityEngine(new SecurityEngine(password));

	}

	/*
	 * this method updates the position of an edge in the actual file aswell as
	 * the information in which block the edge can be found.
	 */

	private LinkedList<Block> distributeInBlocks(LinkedList<Node> list,
			long maximumDataSize, long maximumBlockSize, long actualDataSize) {
		long numberOfBlocks = (2 * maximumDataSize / maximumBlockSize) + 1;
		LinkedList<Block> blockList = new LinkedList<Block>();

		for (long i = 0; i <= numberOfBlocks; i++) {
			Block newBlock = new Block(maximumBlockSize, i);
			blockList.add(newBlock);
		}
		assert (!blockList.isEmpty()) : "blockList empty";
		LinkedList<Node> nodeList = list;

		Collections.shuffle(nodeList, new Random());
		Collections.shuffle(blockList, new Random());

		// find the root node and root block
		for (Node n : nodeList) {
			if (n.getId() == 0) {
				nodeList.remove(n);
				nodeList.addFirst(n);
				break;
			}
		}

		for (Block b : blockList) {
			if (b.getId() == 0) {
				blockList.remove(b);
				blockList.addFirst(b);
				break;
			}
		}

		/*
		 * int minimumFill = maximumBlockSize / 2 - ((maximumDataSize -
		 * actualDataSize) / numberOfBlocks);
		 */
		long minimumFill = maximumBlockSize / 2;

		for (Block newBlock : blockList) {
			int offset = 0;
			if (nodeList.isEmpty()) {
				break;
			}
			do {
				Node n = nodeList.getFirst();
				nodeList.remove();
				newBlock.addNode(n);
				n.setBlock(newBlock.getId());
				n.setLocation(offset);
				offset += n.getSize();
				// TODO: Eventuell muss man prüfen, dass man nicht > blockSize
				// Daten zuordnet
			} while (newBlock.getBytesIncluded() < minimumFill
					&& !list.isEmpty());
		}
		return blockList;

	}

	public void writeBlocks(LinkedList<Node> list, long textLength)
			throws IOException {
		assert(!list.isEmpty()):"nodeList empty";

		// maximum node size in bytes. A node can have a most |\Sigma| edges.
		long maximumVectorSize = 1
				+ Constants.ALPHABET_SIZE
				* (1 + Constants.BLOCK_REFERENCE_BYTES + Constants.EDGE_REFERENCE_BYTES)
				+ Constants.NUMOCCURS_BYTES;

		// the size of the actual data we have to store
		long actualDataSize = 0;
		for (Node n : list) {
			actualDataSize += n.getSize();
		}
		/*
		 * Calculate the maximum data size for IND-CPA-Security. This is 2|x|-1
		 * for the number of states and 3|x|-4 for the number of edges
		 */
		long maximumDataSize = (2 * textLength - 1)
				* (1 + Constants.NUMOCCURS_BYTES)
				+ (3 * textLength - 4)
				* (1 + Constants.BLOCK_REFERENCE_BYTES + Constants.EDGE_REFERENCE_BYTES);

		if (maximumDataSize > Math.pow(2, Constants.EDGE_REFERENCE_BYTES * 8)) {
			System.out.println("Warning: EDGE_REFERENCE_BYTES might be to low");
		}

		long blockSize = maximumVectorSize * Constants.VECTOR_SIZE_MULTI;

		System.out.println(maximumDataSize);
		System.out.println(blockSize);
		System.out.println(actualDataSize);
		LinkedList<Block> blockList = distributeInBlocks(list, maximumDataSize,
				blockSize, actualDataSize);

		// Start printing the blocks

		// open writer for root block

		w = backend.openBlock(0);

		Block currentBlock = blockList.getFirst();

		// print root block
		printBlock(w, currentBlock);
		fillWithData(w, currentBlock);
		blockList.remove(currentBlock);
		for (Block b : blockList) {
			currentBlock = b;
			w = backend.openBlock(currentBlock.getId());
			printBlock(w, b);
			fillWithData(w, b);
		}

		backend.finalizeWriting();
	}

	private void printBlock(DataOutputStream w, Block b) throws IOException {
		for (Node n : b.getNodes()) {

			w.write(toByteArray((long) n.getNumOccurs(),
					Constants.NUMOCCURS_BYTES));

			for (Edge e : n.getEdges()) {
				char c = e.getEdgeLabel();
				// write first char of edge
				w.write(c);

				/*
				 * write bytesequence for representing the edge
				 */

				w.write(toByteArray((long) (e.getEnd()).getLocation(),
						Constants.EDGE_REFERENCE_BYTES));

				// write bytesequence for representing the block the edge is
				// leading to
				w.write(toByteArray((long) e.getEnd().getBlock(),
						Constants.BLOCK_REFERENCE_BYTES));

			}

			w.write(Constants.VECTOR_MARKER);
		}
	}

	private void fillWithData(DataOutputStream w, Block b) throws IOException {
		long missing = b.getSize() - b.getBytesIncluded();
		while (missing > 0) {
			w.write((byte) Constants.PADDING_BYTE);
			missing--;
		}

	}

	private byte[] toByteArray(long number, int bytes) {

		// TODO: do a check whether the given number fits in the given bytes
		byte[] b = new byte[bytes];
		for (int i = 0; i < bytes; i++) {
			b[(bytes - 1) - i] = (byte) (number >>> (i * 8));
		}

		return b;
	}
}
