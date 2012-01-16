package sse.IOHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

import sse.Backend.Backend;
import sse.Vectors.Constants;
import sse.Vectors.EdgePosition;
import sse.Vectors.SuffixVector;

public class BinaryWriter {
	private BinaryOut w;
	private String input;
	private String fileName;
	private SecurityEngine secEngine;
	private Backend backend;
	private LinkedList<Integer> blocks;

	public BinaryWriter(String fileName, String input, Backend backend) {
		this.input = input;
		this.fileName = fileName;
		this.backend = backend;
	}

	/*
	 * this method updates the position of an edge in the actual file aswell as
	 * the information in which block the edge can be found.
	 */

	private Block[] distributeInBlocks(ArrayList<SuffixVector> list,
			ArrayList<EdgePosition> ep, int maximumDataSize,
			int maximumBlockSize, int actualDataSize) {
		int numberOfBlocks = (2 * maximumDataSize / maximumBlockSize) + 1;
		Block blockList[] = new Block[numberOfBlocks];
		// int usage[] = new int[numberOfBlocks + 1];
		blocks = new LinkedList<Integer>();
		for (int i = 1; i <= numberOfBlocks - 1; i++) {
			blocks.add(i);
		}
		Collections.shuffle(blocks, new Random());

		/*
		 * int minimumFill = maximumBlockSize / 2 - ((maximumDataSize -
		 * actualDataSize) / numberOfBlocks);
		 */
		int minimumFill = actualDataSize / numberOfBlocks;

		// the first block always has to contain the root vector to start the
		// search, so the first chunk of data is always in block 0

		int bytesInCurrentBlock = 0;
		int pos = 0;
		int lastPos = 0;

		Stack<SuffixVector> vectorsToUpdate = new Stack<SuffixVector>();
		Iterator<EdgePosition> i = ep.iterator();
		EdgePosition e = i.next();
		int lastLocation = 0;

		// always start with block 0
		blocks.push(0);
		for (SuffixVector v : list) {
			// if (v.getLocation() != 0) {

			int diff = v.getLocation() - lastLocation;
			pos += diff;
			lastLocation = v.getLocation();
			if (bytesInCurrentBlock + diff > minimumFill) {
				// fill the block with characters up to the vector and move
				// the rest to the next block
				bytesInCurrentBlock = bytesInCurrentBlock + diff - minimumFill;
				int block = blocks.pop();
				blockList[block] = new Block(pos - bytesInCurrentBlock,
						lastPos, block);

				while (e != null) {
					if (e.getMovedPosition() <= blockList[block]
							.getUpperBound()) {
						e.setBlock(block);
						e.setMovedPosition(e.getMovedPosition()
								- blockList[block].getLowerBound() + 1);
						if (i.hasNext()) {
							e = i.next();
						} else {
							e = null;
						}
					} else {
						break;
					}
				}
				// usage[block] = minimumFill;
				lastPos = pos - bytesInCurrentBlock + 1;

			} else {
				bytesInCurrentBlock += diff;
			}
			pos += v.getSize();

			if (bytesInCurrentBlock + v.getSize() > minimumFill) {
				if (bytesInCurrentBlock + v.getSize() < maximumBlockSize) {
					// put the vector in the block and start the next one
					vectorsToUpdate.add(v);
					bytesInCurrentBlock = 0;
					/*
					 * if (blocks.isEmpty()) { int foo = 0; for (Block b :
					 * blockList) { if (b != null) { foo += b.getUpperBound() -
					 * b.getLowerBound(); } } System.out.println(foo); }
					 */
					int block = blocks.pop();
					blockList[block] = new Block(pos, lastPos, block);
					while (!vectorsToUpdate.isEmpty()) {
						vectorsToUpdate.pop().setBlock(blockList[block]);
					}
					while (e != null) {
						if (e.getMovedPosition() <= blockList[block]
								.getUpperBound()) {
							e.setBlock(block);
							e.setMovedPosition(e.getMovedPosition()
									- blockList[block].getLowerBound() + 1);
							if (i.hasNext()) {
								e = i.next();
							} else {
								e = null;
							}
						} else {
							break;
						}
					}
					// usage[block] = bytesInCurrentBlock + v.getSize();
					lastPos = pos + 1;
				} else {
					// move vector to next block
					int block = blocks.pop();
					// usage[block] = bytesInCurrentBlock;
					bytesInCurrentBlock = v.getSize();

					blockList[block] = new Block(pos - bytesInCurrentBlock,
							lastPos, block);
					while (!vectorsToUpdate.isEmpty()) {
						vectorsToUpdate.pop().setBlock(blockList[block]);
					}
					lastPos = pos - bytesInCurrentBlock + 1;
					vectorsToUpdate.add(v);
					while (e != null) {
						if (e.getMovedPosition() <= blockList[block]
								.getUpperBound()) {
							e.setBlock(block);
							e.setMovedPosition(e.getMovedPosition()
									- blockList[block].getLowerBound() + 1);
							if (i.hasNext()) {
								e = i.next();
							} else {
								e = null;
							}
						} else {
							break;
						}
					}
				}
			} else {
				vectorsToUpdate.add(v);
				bytesInCurrentBlock += v.getSize();
			}

			// }
		}

		// finish until end of text
		int block = blocks.pop();
		blockList[block] = new Block(actualDataSize, lastPos, block);
		while (!vectorsToUpdate.isEmpty()) {
			vectorsToUpdate.pop().setBlock(blockList[block]);
		}
		while (e != null) {
			if (e.getMovedPosition() <= blockList[block].getUpperBound()) {
				e.setBlock(block);
				e.setMovedPosition(e.getMovedPosition()
						- blockList[block].getLowerBound() + 1);
				if (i.hasNext()) {
					e = i.next();
				} else {
					e = null;
				}
			} else {
				break;
			}
		}

		return blockList;

	}

	public void writeBlocks(ArrayList<SuffixVector> list,
			ArrayList<EdgePosition> ep, long textLength, char password[]) {

		secEngine = new SecurityEngine(password);

		// Calculate the size of the alphabet. This is needed to determine the
		// maximum size for a vector.
		short alphabetSize = Constants.ALPHABET_SIZE;

		// maximum vector size in bytes
		long maximumVectorSize = 2 + alphabetSize + alphabetSize
				* Constants.EDGE_REFERENCE_BYTES + alphabetSize
				* Constants.ORIGINAL_EDGE_POSITION_BYTES + alphabetSize
				* Constants.NUMOCCURS_BYTE + +alphabetSize
				* Constants.BLOCK_REFERENCE_BYTES
				+ Constants.VECTOR_DEPTH_BYTES
				+ Constants.ORIGINAL_VECTOR_POSITION_BYTES;

		// the size of the actual data we have to store
		long actualDataSize = textLength;
		for (SuffixVector v : list) {
			actualDataSize += v.getSize();
		}
		// Calculate the maximum data size for IND-CPA-Security
		long maximumDataSize = 2 + 2 + 2 * Constants.EDGE_REFERENCE_BYTES + 2
				* Constants.ORIGINAL_EDGE_POSITION_BYTES + 2
				* Constants.NUMOCCURS_BYTE + 2
				* Constants.BLOCK_REFERENCE_BYTES
				+ Constants.VECTOR_DEPTH_BYTES
				+ Constants.ORIGINAL_VECTOR_POSITION_BYTES;
		// = 28 in default configuration
		maximumDataSize *= textLength;
		if (maximumDataSize > Math.pow(2, Constants.EDGE_REFERENCE_BYTES * 8)) {
			System.out.println("Warning: EDGE_REFERENCE_BYTES might be to low");
		}

		// if indcpa security is not needed use the actual data size as maximum
		// size

		long blockSize = maximumVectorSize * Constants.VECTOR_SIZE_MULTI;

		Block[] blockList = distributeInBlocks(list, ep, (int) maximumDataSize,
				(int) blockSize, (int) actualDataSize);

		// Start printing the blocks
		int pos = 0;
		int bytesInCurrentBlock = 0;

		int bytesWritten = 0;
		// open writer for meta information file
		try {
			w = new BinaryOut(fileName);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write size of actual data contained in a block
		w.write(blockSize);
		w.close();

		HashMap<Integer, Block> blockAssignment = new HashMap<Integer, Block>();
		for (Block b : blockList) {

			if (b != null) {
				blockAssignment.put(b.getLowerBound(), b);
			}

		}
		// open writer for root block

		try {
			w = new BinaryOut(fileName + "0");
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}

		Block currentBlock;
		Block lastBlock;

		currentBlock = blockAssignment.get(0);
		// w = backend.openNextFile(0, currentBlock.getId(), w, secEngine);

		for (SuffixVector v : list) {
			// write sequence before the vector

			for (; pos < v.getLocation(); pos++, bytesWritten++) {
				if ((bytesWritten + 1) > currentBlock.getUpperBound()) {

					lastBlock = currentBlock;
					currentBlock = blockAssignment.get(lastBlock
							.getUpperBound() + 1);
					fillWithData(bytesInCurrentBlock, blockSize, currentBlock);
					w = backend.openNextFile(lastBlock.getId(),
							currentBlock.getId(), w, secEngine);
					bytesInCurrentBlock = 0;
				}
				w.write(input.charAt(pos));
				bytesInCurrentBlock++;
			}

			// make sure that the vector fits in the block
			if (v.getBlock() != currentBlock) {

				lastBlock = currentBlock;
				currentBlock = blockAssignment
						.get(lastBlock.getUpperBound() + 1);
				fillWithData(bytesInCurrentBlock, blockSize, currentBlock);
				w = backend.openNextFile(lastBlock.getId(),
						currentBlock.getId(), w, secEngine);
				bytesInCurrentBlock = 0;
			}
			// we know the size of the vector and that it fits in the
			// current
			// block, so lets just add its size to the bytes in the block
			bytesInCurrentBlock += v.getSize();
			bytesWritten += v.getSize();

			printVector(w, v);

		}

		// write data from last suffix vector to end
		for (; pos < input.length(); pos++, bytesWritten++) {
			if ((bytesWritten + 1) > currentBlock.getUpperBound()) {

				lastBlock = currentBlock;
				currentBlock = blockAssignment
						.get(lastBlock.getUpperBound() + 1);
				fillWithData(bytesInCurrentBlock, blockSize, currentBlock);
				w = backend.openNextFile(lastBlock.getId(),
						currentBlock.getId(), w, secEngine);
				bytesInCurrentBlock = 0;
			}
			w.write(input.charAt(pos));
			bytesInCurrentBlock++;
		}

		fillWithData(bytesInCurrentBlock, blockSize, currentBlock);

		if (!blocks.isEmpty()) {
			// there wasnt enough data to fill all blocks, so create random
			// blocks
			for (Integer b : blocks) {
				lastBlock = currentBlock;
				currentBlock = new Block(0, 0, b);
				w = backend.openNextFile(lastBlock.getId(),
						currentBlock.getId(), w, secEngine);
				fillWithData(0, blockSize, currentBlock);
			}
		}
		backend.finalize(currentBlock.getId(), w, secEngine);
	}

	private void printVector(BinaryOut w, SuffixVector v) {
		w.write(Constants.VECTOR_MARKER);
		switch (Constants.VECTOR_DEPTH_BYTES) {
		case 8:
			w.write((long) v.getDepth());
			break;
		case 4:
			w.write((int) v.getDepth());
			break;
		case 2:
			w.write((short) v.getDepth());
			break;
		case 1:
			w.write((char) v.getDepth());
			break;
		default:
			throw new UnsupportedOperationException(
					Constants.VECTOR_DEPTH_BYTES
							+ " is not a valid number for vector depth");
		}

		switch (Constants.ORIGINAL_VECTOR_POSITION_BYTES) {
		case 8:
			w.write((long) v.getLocation());
			break;
		case 4:
			w.write((int) v.getLocation());
			break;
		case 2:
			w.write((short) v.getLocation());
			break;
		case 1:
			w.write((char) v.getLocation());
			break;
		default:
			throw new UnsupportedOperationException(
					Constants.ORIGINAL_VECTOR_POSITION_BYTES
							+ " is not a valid number for original vector position");
		}
		for (Character c : v.getMap().keySet()) {
			// write first char of edge
			w.write(c);
			/*
			 * write bytesequence for representing the edge
			 */

			switch (Constants.EDGE_REFERENCE_BYTES) {
			case 8:

				w.write((long) ((v.getMap().get(c).getMovedPosition())));
				break;
			case 4:
				w.write((int) ((v.getMap().get(c).getMovedPosition())));

				break;
			case 2:
				w.write((short) ((v.getMap().get(c).getMovedPosition())));

				break;
			case 1:
				w.write((char) ((v.getMap().get(c).getMovedPosition())));

				break;
			default:
				throw new UnsupportedOperationException(
						Constants.EDGE_REFERENCE_BYTES
								+ " is not a valid number for edge reference");
			}
			// write bytesequence for representing the original edge position
			switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
			case 8:
				w.write((long) v.getMap().get(c).getPosition());
				break;
			case 4:
				w.write((int) v.getMap().get(c).getPosition());
				break;
			case 2:
				w.write((short) v.getMap().get(c).getPosition());
				break;
			case 1:
				w.write((char) v.getMap().get(c).getPosition());
				break;
			default:
				throw new UnsupportedOperationException(
						Constants.ORIGINAL_EDGE_POSITION_BYTES
								+ " is not a valid number for edge reference");
			}
			// write bytesequence for representing the block the edge is
			// leading to
			switch (Constants.BLOCK_REFERENCE_BYTES) {
			case 8:
				w.write((long) v.getMap().get(c).getBlock());
				break;
			case 4:
				w.write((int) v.getMap().get(c).getBlock());
				break;
			case 2:
				w.write((short) v.getMap().get(c).getBlock());
				break;
			case 1:
				w.write((char) v.getMap().get(c).getBlock());
				break;
			default:
				throw new UnsupportedOperationException(
						Constants.BLOCK_REFERENCE_BYTES
								+ " is not a valid number for edge reference");
			}
			// if vector == null then the edge is leading to the sink
			int tmpOccurs = 1;
			if (v.getMap().get(c).end.vector != null) {
				tmpOccurs = v.getMap().get(c).end.vector.getNumOccurs();
			}
			switch (Constants.NUMOCCURS_BYTE) {

			case 8:
				w.write((long) tmpOccurs);
				break;
			case 4:
				w.write((int) tmpOccurs);
				break;
			case 2:
				w.write((short) tmpOccurs);
				break;
			case 1:
				w.write((char) tmpOccurs);
				break;
			default:
				throw new UnsupportedOperationException(
						Constants.NUMOCCURS_BYTE
								+ " is not a valid number for number of occurences");
			}
		}

		w.write(Constants.VECTOR_MARKER);
	}

	private void fillWithData(long bytesInBlock, long blockSize, Block nextBlock) {
		while (bytesInBlock < blockSize) {
			w.write((byte) Constants.PADDING_BYTE);
			bytesInBlock++;
		}

		// append the id of the next block which contains the following data
		switch (Constants.BLOCK_REFERENCE_BYTES) {
		case 8:
			w.write((long) nextBlock.getId());
			break;
		case 4:
			w.write((int) nextBlock.getId());
			break;
		case 2:
			w.write((short) nextBlock.getId());
			break;
		case 1:
			w.write((char) nextBlock.getId());
			break;
		default:
			throw new UnsupportedOperationException(
					Constants.BLOCK_REFERENCE_BYTES
							+ " is not a valid number for edge reference");
		}
	}
}
