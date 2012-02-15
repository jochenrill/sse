package sse;

/**
 * Utility class to manage the different representations of the vector and its
 * components.
 * 
 * @author Jochen Rill
 * 
 */
public class Constants {
	private Constants() {
	}

	/**
	 * The size of the maximal alphabet which can be used (95 = printable ascii
	 * characters)
	 */
	public static short ALPHABET_SIZE = 95;
	/**
	 * Indicates whether debug mode is on
	 */
	public static boolean DEBUG = false;
	/**
	 * Represents the character used to distinct Nodes from text.
	 */
	public static char VECTOR_MARKER = '#';
	/**
	 * Represents the number of bytes used to represent the edge of a node.
	 */
	public static short EDGE_REFERENCE_BYTES = 4;
	/**
	 * Represents the number of bytes used to represent the block an edge is
	 * leading to.
	 */
	public static short BLOCK_REFERENCE_BYTES = 4;
	/**
	 * Represents the multiplier for the block size. This can be used to reduce
	 * the number of blocks if needed.
	 */
	public static short VECTOR_SIZE_MULTI = 5;
	/**
	 * Represents the byte which is used to fill the blocks
	 */
	public static char PADDING_BYTE = (byte) 0;
	/**
	 * Represents the number of bytes used to represent the number of
	 * occurrences of a node
	 */
	public static short NUMOCCURS_BYTES = 4;
}
