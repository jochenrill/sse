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
	 * Represents the character used to distinct SuffixVectors from text.
	 */
	public static char VECTOR_MARKER = '#';
	/**
	 * Represents the number of bytes used to represent the depth of a vector.
	 * Must be either 8, 4, 2 or 1.
	 */

	public static short EDGE_REFERENCE_BYTES = 4;
	/**
	 * Represents the number of bytes used to represent a block. Must be either
	 * 8, 4, 2 or 1.
	 */
	public static short BLOCK_REFERENCE_BYTES = 4;
	/**
	 * Represents the number of bytes used to describe the original position of
	 * the vector in the text. This is needed to calculate which vectors we have
	 * to jump over
	 */

	public static short VECTOR_SIZE_MULTI = 5;

	/**
	 * Represents the byte which is used to fill the datablocks
	 */
	public static char PADDING_BYTE = (byte) 0;

	public static short NUMOCCURS_BYTES = 4;
}
