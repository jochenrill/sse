package sse.Graph;

public class NodeList {

	private Node[] container;
	private int elementCount;

	public NodeList() {
		container = new Node[3];
	}

	public void add(int position, Node n) {
		ensureCapacity(position);

		if (get(position) != null) {

			// move elements to the right
			ensureCapacity(elementCount + 1);

			elementCount++;
			System.arraycopy(container, position, container, position + 1,
					elementCount - position);
			container[position] = n;
		} else {
			elementCount++;
			container[position] = n;
		}

	}

	public Node get(int position) {
		return container[position];
	}

	private void resize(int minimalSize) {
		int newSize = container.length * 2;
		if (minimalSize > newSize) {
			newSize = minimalSize + 3;
		}
		Node[] newArray = new Node[container.length * 2];
		System.arraycopy(container, 0, newArray, 0, container.length);
		container = newArray;
	}

	private void ensureCapacity(int index) {
		if (index >= container.length) {
			resize(index);
		}
	}

	public void remove(int position) {
		elementCount--;
		container[position] = null;
	}
}
