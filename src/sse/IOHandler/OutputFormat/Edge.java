package sse.IOHandler.OutputFormat;

import java.io.Serializable;

public class Edge implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8207119369266009956L;
	private char label;
	private int blockNumber;
	private int indexNumber;

	public Edge(char label, int blockNumber, int indexNumber) {
		this.label = label;
		this.blockNumber = blockNumber;
		this.indexNumber = indexNumber;
	}

	public char getLabel() {
		return label;
	}

	public void setLabel(char label) {
		this.label = label;
	}

	public int getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}

	public int getIndexNumber() {
		return indexNumber;
	}

	public void setIndexNumber(int indexNumber) {
		this.indexNumber = indexNumber;
	}

}
