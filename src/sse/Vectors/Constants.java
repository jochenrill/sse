package sse.Vectors;


/**
 * Utility class to manage the different representations of the vector and its components.
 * @author Jochen Rill
 *
 */
public class Constants {
    private Constants() {
    }

    /**
     * Represents the character used to distinct SuffixVectors from text.
     */
    public static final char VECTOR_MARKER = '#';
    /**
     * Represents the number of bytes used to represent the depth of a vector.
     * Must be either 8, 4, 2 or 1.
     */
    public static final short VECTOR_DEPTH_BYTES = 4;
    /**
     * Represents the number of bytes used to represent an edge in a vector.
     * Must be either 8, 4, 2 or 1.
     */
    public static final short EDGE_REFERENCE_BYTES = 8;
    /**
     * Represents the number of bytes used to represent the block in which the
     * part of the text, the edge is leading to, is.
     * Must be either 8, 4, 2 or 1.
     */
    public static final short BLOCK_REFERENCE_BYTES = 8;
    /**
     * Represents the number of times the maximum vector size will be taken to get the block size
     */
    public static final short VECTOR_SIZE_MULTI = 2;
}
