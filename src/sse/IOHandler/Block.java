package sse.IOHandler;

public class Block {

	private int upperBound;
	private int lowerBound;
	private int id;

	public Block(int upperBound, int lowerBound, int id) {
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.id = id;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
