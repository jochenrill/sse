package sse.IOHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Vectors.Constants;

public class BinaryParser {
	// private DataInputStream stream;
	private RandomAccessFile stream;
	private String fileName;

	public BinaryParser(String fileName) {
		this.fileName = fileName;
	}

	public String getTextWithBlocks() {

		// open root block

		long blockSize = 0;
		long numberOfBlocks = 0;
		long currentBlock = 0;
		StringBuilder b = new StringBuilder();

		try {

			stream = new RandomAccessFile(new File(fileName), "r");
			blockSize = stream.readLong();
			numberOfBlocks = stream.readLong();

			stream.close();
		} catch (IOException e) {
			System.out.println("Root block not found");
		}

		try {

			while (currentBlock < numberOfBlocks) {
				openNextFile(++currentBlock);
				while (stream.getFilePointer() < stream.length()) {
					int value = stream.readByte();
					char foo = (char) value;
					// parse suffix vector
					if (foo == Constants.VECTOR_MARKER) {
						b.append(foo);
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
						b.append(depthValue);
						// read char representing edge
						foo = (char) stream.readByte();
						b.append(foo);
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
							// TODO: Implement seeking of characters
							b.append(edgeValue);
							// we do use block, but we dont use block
							// references, so ignore them anyway
							switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
							case 8:
								stream.readLong();
								break;
							case 4:
								stream.readInt();
								break;
							case 2:
								stream.readShort();
								break;
							case 1:
								stream.readChar();
								break;
							default:
								throw new UnsupportedOperationException(
										Constants.EDGE_REFERENCE_BYTES
												+ " is not a valid number for edge reference");
							}
							// read next char representing edge
							foo = (char) stream.readByte();
							b.append(foo);
						}
					} else {

						// Ignore padding bytes
						if (value != Constants.PADDING_BYTE) {
							b.append((char) value);
						}
					}

				}
			}

		} catch (IOException e) {
			System.out.println("Error while parsing block " + currentBlock);
		}

		// start parsing the text

		return b.toString();
	}

	private void openNextFile(long block) throws IOException {
		stream.close();
		stream = new RandomAccessFile(new File(fileName + ".block" + block),
				"r");
	}

	public String getText() {
		try {
			/*
			 * stream = new DataInputStream( new FileInputStream(new
			 * File(fileName)));
			 */
			// BufferedInputStream b = new BufferedInputStream( new
			// DataInputStream(new FileInputStream(fileName)));
			stream = new RandomAccessFile(new File(fileName), "r");
		} catch (IOException e) {
			System.out.println("File not found");
		}
		StringBuilder b = new StringBuilder();
		try {
			while (stream.getFilePointer() < stream.length()) {
				int value = stream.readByte();
				char foo = (char) value;
				// parse suffix vector
				if (foo == Constants.VECTOR_MARKER) {
					b.append(foo);
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
					b.append(depthValue);
					// read char representing edge
					foo = (char) stream.readByte();
					b.append(foo);
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
						stream.seek(edgeValue);
						System.out
								.println("Char at Pos is "
										+ (char) stream.readByte()
										+ " expected " + foo);
						stream.seek(curPos);
						b.append(edgeValue);
						// we dont use blocks so skip over the block reference
						switch (Constants.ORIGINAL_EDGE_POSITION_BYTES) {
						case 8:
							stream.readLong();
							break;
						case 4:
							stream.readInt();
							break;
						case 2:
							stream.readShort();
							break;
						case 1:
							stream.readChar();
							break;
						default:
							throw new UnsupportedOperationException(
									Constants.EDGE_REFERENCE_BYTES
											+ " is not a valid number for edge reference");
						}
						// read next char representing edge
						foo = (char) stream.readByte();
						b.append(foo);
					}
				} else {
					b.append((char) value);
				}
			}
		} catch (IOException e) {
			System.out.println("Something bad happened");
		}
		return b.toString();
	}
}
