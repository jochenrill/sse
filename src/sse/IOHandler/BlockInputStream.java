package sse.IOHandler;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import sse.IOHandler.OutputFormat.Block;
import sse.IOHandler.OutputFormat.Edge;

public class BlockInputStream extends FilterInputStream {

	private ObjectInputStream in;
	private final char DELIMITER = 0x1E;
	private final int END = Integer.MAX_VALUE;

	public BlockInputStream(ObjectInputStream in) {
		super(in);
		this.in = in;
	}

	public Block[] readBlocks() throws IOException {
		ArrayList<Block> result = new ArrayList<Block>();

		boolean end = false;

		while (!end) {
			int numOccurs = in.readInt();

			// shit ... kann natürlich auch einfach so 31 sein ..
			if (numOccurs == END) {
				break;
			}
			Block b = new Block(numOccurs);

			char edge = in.readChar();

			while (edge != DELIMITER) {
				int blockNumber = in.readInt();
				int indexNumber = in.readInt();
				b.addEdge(new Edge(edge, blockNumber, indexNumber));

				edge = in.readChar();
			}
			result.add(b);
		}
		return result.toArray(new Block[0]);
	}

}
