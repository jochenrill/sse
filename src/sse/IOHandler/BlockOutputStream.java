package sse.IOHandler;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import sse.IOHandler.OutputFormat.Block;
import sse.IOHandler.OutputFormat.Edge;

public class BlockOutputStream extends FilterOutputStream {

	private final char DELIMITER = 0x1E;
	private final int END = Integer.MAX_VALUE;

	private ObjectOutputStream out;

	public BlockOutputStream(ObjectOutputStream out) {
		super(out);
		this.out = out;
	}

	public void writeBlock(Block b) throws IOException {
		out.writeInt(b.getNumOccurs());
		for (Edge e : b.getEdges()) {
			out.writeChar(e.getLabel());
			out.writeInt(e.getBlockNumber());
			out.writeInt(e.getIndexNumber());
		}
		out.writeChar(DELIMITER);
	}

	public void writeBlocks(Block[] blockArray) throws IOException {
		for (int i = 0; i < blockArray.length; i++) {
			writeBlock(blockArray[i]);
		}
		out.writeInt(END);
	}

}
