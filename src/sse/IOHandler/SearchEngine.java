package sse.IOHandler;

import java.io.File;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Backend.Backend;
import sse.Vectors.Constants;

public class SearchEngine {
	private RandomAccessFile stream;
	private long currentBlock;
	private SecurityEngine sEn;
	private long lastEdgeValue = 0;
	private long lastDepthValue = 0;
	private long numOccurs;
	private boolean reachedSink = false;
	private int files = 0;
	private boolean reachedEnd = false;
	private Backend backend;
	private String startFile;

	public SearchEngine(String keyFile, Backend backend, String startFile) {
		this.sEn = new SecurityEngine();
		this.sEn.readKey(keyFile);

		this.currentBlock = 0;
		this.backend = backend;
		this.startFile = startFile;
	}

	public int getTransferedFilesCount() {
		return files;
	}

	public boolean find(String word) {
		long blockSize = 0;
		long numberOfBlocks = 0;

		DataInputStream firstStream = new DataInputStream(
				backend.loadStartBlock());

		try {
			blockSize = firstStream.readLong();
			numberOfBlocks = firstStream.readLong();
			firstStream.close();
		} catch (IOException e1) {
			System.out.println("Startblock not found! Exiting.");
			System.exit(0);
		}

		try {
			long toDelete = currentBlock;
			// open the first block
			files++;
			reachedEnd = backend.searchNext(++currentBlock, startFile, 0,
					toDelete, stream, sEn);
			stream = backend.getStream();
			while (stream.getFilePointer() < stream.length() && !reachedEnd) {
				// read first byte
				int value = stream.readByte();
				boolean jumpOver = false;
				char foo = (char) value;
				// we found a suffix vector - parse it
				if (foo == Constants.VECTOR_MARKER) {
					// read depth of the node
					long depthValue = 0;
					switch (Constants.VECTOR_DEPTH_BYTES) {
					case 8:
						depthValue = stream.readLong();
						break;
					case 4:
						depthValue = (long) stream.readInt();
						break;
					case 2:
						depthValue = (long) stream.readShort();
						break;
					case 1:
						depthValue = (long) stream.readChar();
						break;
					default:
						throw new UnsupportedOperationException(
								Constants.VECTOR_DEPTH_BYTES
										+ " is not a valid number for vector depth");
					}
					long lastNumOccurs = numOccurs;
					switch (Constants.NUMOCCURS_BYTE) {
					case 8:
						numOccurs = stream.readLong();
						break;
					case 4:
						numOccurs = (long) stream.readInt();
						break;
					case 2:
						numOccurs = (long) stream.readShort();
						break;
					case 1:
						numOccurs = (long) stream.readChar();
						break;
					default:
						throw new UnsupportedOperationException(
								Constants.NUMOCCURS_BYTE
										+ " is not a valid number for number of occurences");
					}

					long originalVectorPosition = 0;
					switch (Constants.ORIGINAL_VECTOR_POSITION_BYTES) {
					case 8:
						originalVectorPosition = stream.readLong();
						break;
					case 4:
						originalVectorPosition = (long) stream.readInt();
						break;
					case 2:
						originalVectorPosition = (long) stream.readShort();
						break;
					case 1:
						originalVectorPosition = (long) stream.readChar();
						break;
					default:
						throw new UnsupportedOperationException(
								Constants.ORIGINAL_VECTOR_POSITION_BYTES
										+ " is not a valid number for vector position");
					}
					// the suffix vector we are looking at might not be the one
					// the edge was leading to

					if (depthValue != 0) {
						if (lastEdgeValue - lastDepthValue < originalVectorPosition
								- depthValue) {
							jumpOver = true;
							numOccurs = lastNumOccurs;
						} else {
							jumpOver = false;
						}
					}
					/*
					 * we matched the word and we found the correct suffix
					 * vector the edge was leading to. stop the search and print
					 * numOccurs.
					 */
					if (word.length() == 0 && !jumpOver) {
						break;
					} else if (word.length() == 0 && reachedSink) {
						break;
					}
					// the suffix vector was not the correct one, change
					// depthValue back
					if (!jumpOver) {
						lastDepthValue = depthValue;
					}
					// read char representing edge
					foo = (char) stream.readByte();
					boolean sink;
					while (foo != Constants.VECTOR_MARKER) {
						/* read reference to position in text
						 * it is expected, that the least significant bit in the reference indicates whether the 
						 * edge leads to the sink or not, hence the bit operations.
						 */
						long edgeValue = 0;

						switch (Constants.EDGE_REFERENCE_BYTES) {
						case 8:

							edgeValue = stream.readLong();
							sink = (edgeValue & Long
									.parseLong("0x0000000000000001")) == 1;
							edgeValue = (edgeValue & Long
									.parseLong("0xFFFFFFFFFFFFFFFE")) >> 1;
							break;
						case 4:
							edgeValue = (long) stream.readInt();
							sink = (edgeValue & 0x00000001) == 1;
							edgeValue = (edgeValue & 0xFFFFFFFE) >> 1;
							break;
						case 2:
							edgeValue = (long) stream.readShort();
							sink = (edgeValue & 0x0001) == 1;
							edgeValue = (edgeValue & 0xFFFE) >> 1;
							break;
						case 1:
							edgeValue = (long) stream.readChar();
							sink = (edgeValue & 0x01) == 1;
							edgeValue = (edgeValue & 0xFE) >> 1;
							break;
						default:
							throw new UnsupportedOperationException(
									Constants.EDGE_REFERENCE_BYTES
											+ " is not a valid number for edge reference");
						}
						// Save the block we want to jump to
						long originalEdgePosition = 0;
						switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
						case 8:
							originalEdgePosition = stream.readLong();
							break;
						case 4:
							originalEdgePosition = (long) stream.readInt();
							break;
						case 2:
							originalEdgePosition = (long) stream.readShort();
							break;
						case 1:
							originalEdgePosition = (long) stream.readChar();
							break;
						default:
							throw new UnsupportedOperationException(
									Constants.EDGE_REFERENCE_BYTES
											+ " is not a valid number for edge reference");
						}
						if (!jumpOver && foo == word.charAt(0)) {
							// Jump to the block at the given position
							// if (blockValue - currentBlock == 0) {
							// we are staying in the current block
							reachedSink = sink;
							long blockToOpen = (edgeValue / blockSize) + 1;
							long position = 0;
							if (blockToOpen == 1) {
								position = edgeValue;
							} else {
								position = edgeValue
										% ((blockToOpen - 1) * blockSize);
							}
							lastEdgeValue = originalEdgePosition;
							files++;
							reachedEnd = backend.searchNext(blockToOpen,
									startFile, position, currentBlock, stream,
									sEn);
							stream = backend.getStream();
							currentBlock = blockToOpen;
							break;
						}
						// read next char representing edge
						foo = (char) stream.readByte();
					}
				} else {

					// Ignore padding bytes
					if (value == Constants.PADDING_BYTE
							&& currentBlock < numberOfBlocks) {
						// Block done, move to next block
						toDelete = currentBlock;
						files++;
						reachedEnd = backend.searchNext(++currentBlock,
								startFile, 0, toDelete, stream, sEn);
						stream = backend.getStream();
					} else if (word.length() != 0 && foo == word.charAt(0)) {
						word = word.substring(1, word.length());
					} else if (word.length() != 0) {
						return false;
					}
				}
				// Make sure that we open the next block if the last byte in a
				// block is indeed a character
				if (!(stream.getFilePointer() < stream.length())
						&& currentBlock < numberOfBlocks) {
					toDelete = currentBlock;
					files++;
					reachedEnd = backend.searchNext(++currentBlock, startFile,
							0, toDelete, stream, sEn);
					stream = backend.getStream();
				}
			}
		} catch (IOException e) {
			System.out.println("Error while parsing block " + currentBlock);
		}
		// Make sure to delete the last opened block
		File delete = new File(startFile + currentBlock + ".dec");
		if (delete.exists()) {
			delete.delete();
		}
		if (word.length() == 0) {
			// if we have reached the sink then numOccurs is 1
			if (reachedSink) {
				System.out.println("1");
			} else {
				System.out.println(numOccurs);
			}
			return true;
		} else {
			return false;
		}
	}
}
