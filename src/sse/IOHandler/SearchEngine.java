package sse.IOHandler;

import java.io.File;

import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Vectors.Constants;

public class SearchEngine {
	private String word;
	private RandomAccessFile stream;
	private long currentBlock;
	private SecurityEngine sEn;
	private boolean decrypt;
	private long lastEdgeValue = 0;
	private long lastDepthValue = 0;
	private long numOccurs;
	private boolean notReachedSink = false;
	public int files = 0;

	public SearchEngine(String word) {
		this.word = word;
		this.currentBlock = 0;
		this.decrypt = false;
	}

	public SearchEngine(String word, String keyFile) {
		this.decrypt = true;
		this.sEn = new SecurityEngine();
		this.sEn.readKey(keyFile);
		this.word = word;
		this.currentBlock = 0;
	}

	private void openNextFile(long block, String fileName, long position,
			long oldBlock) throws IOException {
		files++;
		stream.close();
		if (decrypt) {
			File delete = new File(fileName + oldBlock + ".dec");
			if (delete.exists()) {
				delete.delete();
			}
			sEn.decrypt(fileName + block);
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");
			stream.seek(position);
		} else {
			stream = new RandomAccessFile(new File(fileName + block), "r");
			stream.seek(position);
		}
	}

	public boolean find(String startFile, boolean decrypt) {
		long blockSize = 0;
		long numberOfBlocks = 0;
		try {
			stream = new RandomAccessFile(new File(startFile), "r");
			blockSize = stream.readLong();
			numberOfBlocks = stream.readLong();
			stream.close();
		} catch (IOException e) {
			System.out.println("Root block not found");
		}
		try {
			long toDelete = currentBlock;
			// open the first block
			openNextFile(++currentBlock, startFile, 0, toDelete);
			while (stream.getFilePointer() < stream.length()) {
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
					switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
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
								Constants.EDGE_REFERENCE_BYTES
										+ " is not a valid number for edge reference");
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
					 * we matched to word and we found the correct suffix vector
					 * the edge was leading to stop the search an print
					 * numOccurs.
					 */
					if (word.length() == 0 && !jumpOver) {
						notReachedSink = true;
						break;
					}
					// the suffix vector was not the correct one, change
					// depthValue back
					if (!jumpOver) {
						lastDepthValue = depthValue;
					}
					// read char representing edge
					foo = (char) stream.readByte();
					while (foo != Constants.VECTOR_MARKER) {
						// read reference to position in text
						long edgeValue = 0;
						switch (Constants.EDGE_REFERENCE_BYTES) {
						case 8:
							edgeValue = stream.readLong();
							break;
						case 4:
							edgeValue = (long) stream.readInt();
							break;
						case 2:
							edgeValue = (long) stream.readShort();
							break;
						case 1:
							edgeValue = (long) stream.readChar();
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
							long blockToOpen = (edgeValue / blockSize) + 1;
							long position = 0;
							if (blockToOpen == 1) {
								position = edgeValue;
							} else {
								position = edgeValue
										% ((blockToOpen - 1) * blockSize);
							}
							lastEdgeValue = originalEdgePosition;
							openNextFile(blockToOpen, startFile, position,
									currentBlock);
							currentBlock = blockToOpen;
							break;
						}
						// read next char representing edge
						foo = (char) stream.readByte();
					}
				} else {

					// Ignore padding bytes
					if (value == Constants.PADDING_BYTE) {
						// Block done, move to next block
						toDelete = currentBlock;
						openNextFile(++currentBlock, startFile, 0, toDelete);
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
					openNextFile(++currentBlock, startFile, 0, toDelete);
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
			// if we are at the end of the files and have reached the sink then numOccurs is 1
			if (currentBlock == numberOfBlocks && !notReachedSink) {
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
