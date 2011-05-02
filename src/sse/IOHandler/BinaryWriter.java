package sse.IOHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import sse.Vectors.Constants;
import sse.Vectors.EdgePosition;
import sse.Vectors.SuffixVector;

public class BinaryWriter {
	private BinaryOut w;
	private String input;
	private String fileName;
	private SecurityEngine secEngine;

	public BinaryWriter(String fileName, String input) {
		this.input = input;
		this.fileName = fileName;
	}

	public void writeAll(ArrayList<SuffixVector> list,
			ArrayList<EdgePosition> ep) {
		try {
			w = new BinaryOut(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int pos = 0;
		for (SuffixVector v : list) {
			// write sequence before the vector
			if (v.getLocation() != 0) {
				for (; pos < v.getLocation(); pos++) {
					w.write(input.charAt(pos));
				}
			}
			// write vector itself
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
			switch (Constants.NUMOCCURS_BYTE) {
			case 8:
				w.write((long) v.getNumOccurs());
				break;
			case 4:
				w.write((int) v.getNumOccurs());
				break;
			case 2:
				w.write((short) v.getNumOccurs());
				break;
			case 1:
				w.write((char) v.getNumOccurs());
				break;
			default:
				throw new UnsupportedOperationException(
						Constants.NUMOCCURS_BYTE
								+ " is not a valid number for number of occurences");
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
						Constants.VECTOR_DEPTH_BYTES
								+ " is not a valid number for vector depth");
			}
			for (Character c : v.getMap().keySet()) {
				// write first char of edge
				w.write(c);
				// write bytesequence for representing the edge
				switch (Constants.EDGE_REFERENCE_BYTES) {
				case 8:
					w.write((long) v.getMap().get(c).getMovedPosition());
					break;
				case 4:
					w.write((int) v.getMap().get(c).getMovedPosition());
					break;
				case 2:
					w.write((short) v.getMap().get(c).getMovedPosition());
					break;
				case 1:
					w.write((char) v.getMap().get(c).getMovedPosition());
					break;
				default:
					throw new UnsupportedOperationException(
							Constants.EDGE_REFERENCE_BYTES
									+ " is not a valid number for edge reference");
				}
				// write bytesequence for representing the block the edge is
				// leading to. We dont use blocks, so just write zero bytes
				switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
				case 8:
					w.write((long) 0);
					break;
				case 4:
					w.write((int) 0);
					break;
				case 2:
					w.write((short) 0);
					break;
				case 1:
					w.write((char) 0);
					break;
				default:
					throw new UnsupportedOperationException(
							Constants.ORIGINAL_EDGE_POSITION_BYTES
									+ " is not a valid number for edge reference");
				}
			}
			w.write(Constants.VECTOR_MARKER);
		}
		// write the rest
		for (; pos < input.length(); pos++) {
			w.write(input.charAt(pos));
		}
		w.close();
	}

	/*
	 * this method updates the position of an edge in the actual file aswell as
	 * the information in which block the edge can be found.
	 */

	private void updateBlockPosition(ArrayList<SuffixVector> list,
			ArrayList<EdgePosition> ep, long blockSize) {
		// Start printing the blocks
		int pos = 0;
		long bytesInCurrentBlock = 0;
		long currentBlock = 1;
		long padding = 0;
		// 8 byte for header
		// bytesInCurrentBlock += 8;
		Iterator<EdgePosition> iterator = ep.iterator();
		EdgePosition e = iterator.next();
		for (SuffixVector v : list) {
			// write sequence before the vector
			if (v.getLocation() != 0) {
				for (; pos < v.getLocation(); pos++) {

					if ((bytesInCurrentBlock + 1) > blockSize) {

						while (e != null && e.getPosition() < pos) {
							e.setMovedPosition(e.getMovedPosition() + padding);
							if (iterator.hasNext()) {
								e = iterator.next();
							} else {
								e = null;
							}
						}

						padding += blockSize - bytesInCurrentBlock;
						currentBlock++;
						bytesInCurrentBlock = 0;
					}
					bytesInCurrentBlock++;
				}
			}
			// make sure that the vector fits in the blocksize
			if ((bytesInCurrentBlock + v.getSize()) > blockSize) {
				while (e != null && e.getPosition() < pos) {
					e.setMovedPosition(e.getMovedPosition() + padding);
					if (iterator.hasNext()) {
						e = iterator.next();
					} else {
						e = null;
					}
				}
				padding += blockSize - bytesInCurrentBlock;

				currentBlock++;
				bytesInCurrentBlock = 0;
			}
			// write vector itself
			bytesInCurrentBlock += v.getSize();
		}
		// write the rest (from end of last suffix vector to end of string)
		for (; pos < input.length(); pos++) {
			if ((bytesInCurrentBlock + 1) > blockSize) {
				while (e != null && e.getPosition() < pos) {
					e.setMovedPosition(e.getMovedPosition() + padding);
					if (iterator.hasNext()) {
						e = iterator.next();
					} else {
						e = null;
					}
				}
				padding += blockSize - bytesInCurrentBlock;

				currentBlock++;
				bytesInCurrentBlock = 0;
			}
			bytesInCurrentBlock++;
		}

		// update the rest
		while (e != null && e.getPosition() <= pos) {
			e.setMovedPosition(e.getMovedPosition() + padding);
			if (iterator.hasNext()) {
				e = iterator.next();
			} else {
				e = null;
			}
		}
	}

	public void writeBlocks(ArrayList<SuffixVector> list,
			ArrayList<EdgePosition> ep, boolean encrypt, long textLength,
			boolean indcpa) {
		if (encrypt) {
			secEngine = new SecurityEngine();
		}
		// Calculate the size of the alphabet. This is needed to determine the
		// maximum size for a vector.
		short alphabetSize = 0;
		boolean[] included = new boolean[255];
		for (int i = 0; i < input.length(); i++) {
			if (!included[(char) input.charAt(i)]) {
				included[(char) input.charAt(i)] = true;
				alphabetSize++;
			}
		}

		// maximum vector size in bytes
		long maximumVectorSize = 2 + alphabetSize + alphabetSize
				* Constants.EDGE_REFERENCE_BYTES + alphabetSize
				* Constants.ORIGINAL_EDGE_POSITION_BYTES
				+ Constants.VECTOR_DEPTH_BYTES
				+ Constants.ORIGINAL_VECTOR_POSITION_BYTES;

		// Calculate the maximum data size for IND-CPA-Security
		long maximumDataSize = 2 + 2 + 2 * Constants.EDGE_REFERENCE_BYTES + 2
				* Constants.ORIGINAL_EDGE_POSITION_BYTES
				+ Constants.VECTOR_DEPTH_BYTES
				+ Constants.ORIGINAL_VECTOR_POSITION_BYTES;
		maximumDataSize *= textLength;
		long blockSize = maximumVectorSize * Constants.VECTOR_SIZE_MULTI;

		long numOfBlocks = 2 * (maximumDataSize / blockSize) + 1;

		updateBlockPosition(list, ep, blockSize);
		// Start printing the blocks
		int pos = 0;
		long bytesInCurrentBlock = 0;
		long currentBlock = 1;
		// open writer for meta information file
		try {
			w = new BinaryOut(fileName);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write blocksize
		w.write(blockSize);
		w.close();
		// open writer for currentBlock
		try {
			w = new BinaryOut(fileName + currentBlock);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}

		for (SuffixVector v : list) {
			// write sequence before the vector
			if (v.getLocation() != 0) {
				for (; pos < v.getLocation(); pos++) {
					if ((bytesInCurrentBlock + 1) > blockSize) {
						fillWithData(bytesInCurrentBlock, blockSize);
						openNextFile(++currentBlock, encrypt);
						bytesInCurrentBlock = 0;
					}
					w.write(input.charAt(pos));
					bytesInCurrentBlock++;
				}
			}
			// make sure that the vector fits in the blocksize
			if ((bytesInCurrentBlock + v.getSize()) > blockSize) {
				fillWithData(bytesInCurrentBlock, blockSize);
				openNextFile(++currentBlock, encrypt);
				bytesInCurrentBlock = 0;
			}
			// we know the size of the vector and that it fits in the current
			// block, so lets just add its size to the bytes in the block
			bytesInCurrentBlock += v.getSize();
			// write vector itself
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
			switch (Constants.NUMOCCURS_BYTE) {
			case 8:
				w.write((long) v.getNumOccurs());
				break;
			case 4:
				w.write((int) v.getNumOccurs());
				break;
			case 2:
				w.write((short) v.getNumOccurs());
				break;
			case 1:
				w.write((char) v.getNumOccurs());
				break;
			default:
				throw new UnsupportedOperationException(
						Constants.NUMOCCURS_BYTE
								+ " is not a valid number for number of occurences");
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
						Constants.VECTOR_DEPTH_BYTES
								+ " is not a valid number for vector depth");
			}
			for (Character c : v.getMap().keySet()) {
				// write first char of edge
				w.write(c);
				// write bytesequence for representing the edge
				switch (Constants.EDGE_REFERENCE_BYTES) {
				case 8:
					w.write((long) v.getMap().get(c).getMovedPosition());
					break;
				case 4:
					w.write((int) v.getMap().get(c).getMovedPosition());
					break;
				case 2:
					w.write((short) v.getMap().get(c).getMovedPosition());
					break;
				case 1:
					w.write((char) v.getMap().get(c).getMovedPosition());
					break;
				default:
					throw new UnsupportedOperationException(
							Constants.EDGE_REFERENCE_BYTES
									+ " is not a valid number for edge reference");
				}
				// write bytesequence for representing the block the edge is
				// leading to
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
			}
			w.write(Constants.VECTOR_MARKER);
		}
		// write the rest (from end of last suffix vector to end of string)
		for (; pos < input.length(); pos++) {
			if ((bytesInCurrentBlock + 1) > blockSize) {
				fillWithData(bytesInCurrentBlock, blockSize);
				openNextFile(++currentBlock, encrypt);
				bytesInCurrentBlock = 0;
			}
			w.write(input.charAt(pos));
			bytesInCurrentBlock++;
		}
		fillWithData(bytesInCurrentBlock, blockSize);
		// Create a lot of empty blocks for IND-CPA-Security
		if (indcpa) {
			while (currentBlock * blockSize < maximumDataSize) {
				openNextFile(++currentBlock, encrypt);
				fillWithData(0, blockSize);
			}
		}

		w.close();
		// open writer for meta information file
		try {
			w = new BinaryOut(fileName, true);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock));
			// remove the unencryted file
			new File(fileName + (currentBlock)).delete();
		}
		secEngine.printKey("key");

		w.write(currentBlock);
		w.close();
		/*
		 * System.out.println(currentBlock*blockSize);
		 * System.out.println(maximumDataSize); System.out.println(numOfBlocks);
		 * System.out.println(currentBlock);
		 * System.out.println((maximumDataSize-
		 * currentBlock*blockSize)/blockSize); System.out.println(blockSize);
		 */
	}

	private void openNextFile(long currentBlock, boolean encrypt) {
		w.close();
		// Encrypt the last block if needed
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock - 1));
			// remove the unencryted file
			new File(fileName + (currentBlock - 1)).delete();

		}
		try {
			w = new BinaryOut(fileName + currentBlock);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
	}

	private void fillWithData(long bytesInBlock, long blockSize) {
		while (bytesInBlock < blockSize) {
			w.write((byte) Constants.PADDING_BYTE);
			bytesInBlock++;
		}
	}
}
