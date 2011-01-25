package sse.IOHandler;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinaryParser {
	// private DataInputStream stream;
	RandomAccessFile stream;

	public BinaryParser(String fileName) {

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

	}
	


	public String getText() {

		StringBuilder b = new StringBuilder();

		try {
			while (stream.getFilePointer() < stream.length()) {

				int value = stream.readByte();
				char foo = (char) value;

				// parse suffix vector
				if (foo == '#') {
					b.append(foo);
					// read depth of the node
					long depthValue = stream.readLong();

					b.append(depthValue);
					// read char ','
					foo = (char) stream.readByte();
					b.append(foo);
					// read char representing edge
					foo = (char) stream.readByte();
					b.append(foo);
					while (foo != '#') {

						// read reference to position in text
						long edgeValue = stream.readLong();

						long curPos = stream.getFilePointer();

						stream.seek(edgeValue);
						System.out
								.println("Char at Pos is "
										+ (char) stream.readByte()
										+ " expected " + foo);
						stream.seek(curPos);
						b.append(edgeValue);
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
