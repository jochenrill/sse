package sse.Vectors;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * This class is used to represent a suffix vector generated from the cdwag
 * 
 * @author Jochen Rill
 * 
 */
public class SuffixVector implements Comparable<SuffixVector>, Serializable {

	private static final long serialVersionUID = -9022293708037890917L;
	private int location;
	private int depth;
	private Hashtable<Character, EdgePosition> map;
	private int numOccurs = -1;

	public SuffixVector(int loc) {
		this.location = loc;
		map = new Hashtable<Character, EdgePosition>();
	}

	public int getLocation() {
		return location;
	}

	public int getSize() {
		// 8 bytes for depth keySet().size bytes for each character, each edge
		// has 8 bytes for the reference number
		// two bytes for each '#'
		return Constants.VECTOR_DEPTH_BYTES + map.keySet().size()
				+ map.keySet().size() * Constants.EDGE_REFERENCE_BYTES + 2
				+ map.keySet().size() * Constants.ORIGINAL_EDGE_POSITION_BYTES
				+ Constants.ORIGINAL_VECTOR_POSITION_BYTES
				+ Constants.NUMOCCURS_BYTE;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public void addEdge(char c, EdgePosition e) {
		map.put(c, e);
	}

	public void setMap(Hashtable<Character, EdgePosition> map) {
		this.map = map;
	}

	public Hashtable<Character, EdgePosition> getMap() {
		return map;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public int compareTo(SuffixVector o) {
		if (o == null) {
			throw new NullPointerException();
		}
		if (this.location == o.getLocation()) {
			
			// If the location is the same the vectors get sorted by depth. This is important for the random distribution to work.
			// If multiple vectors occupy the same location, the one with the lowest depth must be chosen.
			if (this.getDepth() < o.getDepth()) {
				return -1;
			} else if (this.getDepth() > o.getDepth()) {
				return 1;
			} else {
				return 0;
			}
		} else if (this.location < o.getLocation()) {
			return -1;
		} else {
			return 1;
		}
	}

	public void setNumOccurs(int numOccurs) {
		this.numOccurs = numOccurs;
	}

	public int getNumOccurs() {
		return numOccurs;
	}
}
