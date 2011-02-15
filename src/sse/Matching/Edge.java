package sse.Matching;

public class Edge<T, K> {
	public boolean matched;
	private BipartiteNode<T> left;
	private BipartiteNode<K> right;

	public void setLeft(BipartiteNode<T> left) {
		this.left = left;
	}

	public BipartiteNode<T> getLeft() {
		return left;
	}

	public void setRight(BipartiteNode<K> right) {
		this.right = right;
	}

	public BipartiteNode<K> getRight() {
		return right;
	}
	@Override
	public String toString(){
		return left.toString() + " -> " + right.toString();
	}
	
}
