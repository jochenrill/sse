package sse.Vectors;

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
	 * Defines whether exact position matching for vectors should be used. Running time for exact matching is at least O(n^2).
	 * If this is false, the positions will be distributed randomly. This will lead to multiple vectors occupying the same location.
	 * Might be less secure (or not ..). 
	 */
	public static boolean EXACT_MATCHING = true;
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
	 * Represents the character used to distinct SuffixVectors from text.
	 */
	public static char VECTOR_MARKER = '#';
	/**
	 * Represents the number of bytes used to represent the depth of a vector.
	 * Must be either 8, 4, 2 or 1.
	 */
	public static short VECTOR_DEPTH_BYTES = 4;
	/**
	 * Represents the number of bytes used to represent an edge in a vector.
	 * Must be either 8, 4, 2 or 1.
	 */

	public static short EDGE_REFERENCE_BYTES = 4;
	/**
	 * Represents the number of bytes used to describe the original position of
	 * the vector in the text. This is needed to calculate which vectors we have
	 * to jump over
	 */
	public static short ORIGINAL_VECTOR_POSITION_BYTES = 4;
	/**
	 * Represents the number of bytes used to describe the original position of
	 * the edge in the text. This is needed to calculate which vectors we have
	 * to jump over
	 */
	public static short ORIGINAL_EDGE_POSITION_BYTES = 4;
	/**
	 * Represents the number of times the maximum vector size will be taken to
	 * get the block size
	 */
	public static short VECTOR_SIZE_MULTI = 5;

	/**
	 * Represents the byte which is used to fill the datablocks
	 */
	public static char PADDING_BYTE = (byte) 0;

	public static short NUMOCCURS_BYTE = 4;
}
