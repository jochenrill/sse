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
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge tmp = (Edge) o;
			return (tmp.left == left && tmp.right == right);
		} else {
			return false;
		}
	}
}
