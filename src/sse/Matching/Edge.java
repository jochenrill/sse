package sse.Matching;

public class Edge<T, K> {
	public boolean matched;
	private BipartiteNode<T> left;
	private BipartiteNode<K> right;
	public int capacity = 10000;

	public void setLeft(BipartiteNode<T> left) {
		this.left = left;
	}

	public BipartiteNode<T> getLeft() {
		return left;
	}

	public void setRight(BipartiteNode<K> right) {
		this.right = right;
	}
	public Edge(BipartiteNode<T> left, BipartiteNode<K> right){
		this.left = left;
		this.right = right;
	}
	public Edge(){
		
	}
	public BipartiteNode<K> getRight() {
		return right;
	}
	@Override
	public String toString(){
		return left.toString() + " -> " + right.toString();
	}
	
}
