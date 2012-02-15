package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import sse.IOHandler.SecurityEngine;

public interface Backend {

	/**
	 * This method closes all streams and deletes unneeded files created during
	 * search. The SearchEngine makes sure that this method is called.
	 */
	public void finalizeSearch();

	/**
	 * This method sets the security engine needed to en/decrypt all block.
	 * BinaryWriter, as well as SearchEngine make sure that it is set up
	 * correctly.
	 * 
	 * @param secEngine
	 *            the security engine
	 */
	public void setSecurityEngine(SecurityEngine secEngine);

	/**
	 * This method opens a new file and returns the new output stream. It has to
	 * close the previous stream and make sure all unneeded files are deleted.
	 * 
	 * @param block
	 *            the block we want to open
	 * @return the new output stream
	 * 
	 */
	public DataOutputStream openBlock(int block);

	/**
	 * This method deletes any unneeded files and closes all streams created
	 * during writing. The BinaryWriter class makes sure that this method is
	 * called.
	 */
	public void finalizeWriting();

	/**
	 * This method opens the next file for searching and returns the new stream.
	 * 
	 * @param block
	 *            the block to open
	 * 
	 * @param position
	 *            the position to jump to
	 * 
	 * @return the new stream at the given position
	 */
	public RandomAccessFile searchNext(long block, long position);

	/**
	 * This method returns a stream to the block containing the meta
	 * information.
	 * 
	 * @return a stream to the first block.
	 */
	public DataInputStream loadStartBlock();

	/**
	 * This method loads a random block file and deletes it again.
	 * 
	 * @param numberOfBlocks
	 *            the total number of blocks available
	 * 
	 */
	public void loadRandomBlock(int numberOfBlocks);

}
