package sse.IOHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Vectors.Constants;

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
