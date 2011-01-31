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

	private void openNextFile(long block, String fileName, long position)
			throws IOException {
		stream.close();
		if (decrypt) {
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
			openNextFile(++currentBlock, startFile, 0);
			while (currentBlock < numberOfBlocks || word.length() != 0) {
				long lastDepth = 0;

				long bytesInBlock = 0;

				while (stream.getFilePointer() < stream.length()) {
					int value = stream.readByte();
					bytesInBlock++;
					char foo = (char) value;
					// parse suffix vector
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
						lastDepth = depthValue;

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
							long curPos = stream.getFilePointer();
							// Save the block we want to jump to
							long blockValue = 0;
							switch (Constants.BLOCK_REFERENCE_BYTES) {
							case 8:
								blockValue = stream.readLong();
								break;
							case 4:
								blockValue = (long) stream.readInt();
								break;
							case 2:
								blockValue = (long) stream.readShort();
								break;
							case 1:
								blockValue = (long) stream.readChar();
								break;
							default:
								throw new UnsupportedOperationException(
										Constants.EDGE_REFERENCE_BYTES
												+ " is not a valid number for edge reference");
							}
							if (foo == word.charAt(0)) {

								// Jump to the block at the given position
								currentBlock = blockValue;

								openNextFile(blockValue, startFile, edgeValue
										- (blockValue - currentBlock)
										* blockSize);

								break;
							}
							// read next char representing edge
							foo = (char) stream.readByte();

						}

					} else {

						// Ignore padding bytes
						if (value != Constants.PADDING_BYTE
								&& foo == word.charAt(0)) {
							word = word.substring(1, word.length() - 1);
						}
					}

				}
			}

		} catch (IOException e) {
			System.out.println("Error while parsing block " + currentBlock);
		}
		return false;
	}
}
