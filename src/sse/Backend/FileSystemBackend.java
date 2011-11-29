package sse.Backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;
import sse.Vectors.Constants;

public class FileSystemBackend implements Backend {

	private String fileName;
	private RandomAccessFile searchStream;

	public FileSystemBackend(String fileName) {
		this.fileName = fileName;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public BinaryOut openNextFile(long currentBlock, BinaryOut w,
			SecurityEngine secEngine) {
		w.close();
		// Encrypt the last block if needed

		secEngine.encrypt(fileName + (currentBlock - 1));
		// remove the unencryted file
		new File(fileName + (currentBlock - 1)).delete();

		try {
			w = new BinaryOut(fileName + currentBlock);

		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
		return w;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public void finalize(long currentBlock, BinaryOut w,
			SecurityEngine secEngine) {
		w.close();
		// open writer for meta information file
		try {
			w = new BinaryOut(fileName, true);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();

		

		w.write(currentBlock);
		w.close();
		secEngine.printKey(fileName);
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public boolean searchNext(long block, String fileName, long position,
			long oldBlock, RandomAccessFile stream, SecurityEngine sEn)
			throws IOException {
		boolean reachedEnd = false;
		if (stream != null) {
			stream.close();
		}
		File delete = new File(fileName + oldBlock + ".dec");
		if (delete.exists()) {
			delete.delete();
		}
		sEn.decrypt(fileName + block);
		stream = new RandomAccessFile(new File(fileName + block + ".dec"), "r");
		// if a block starts with a padding byte, it is a padding block =)
		if (stream.readByte() == Constants.PADDING_BYTE) {
			reachedEnd = true;
		}
		stream.seek(position);
		searchStream = stream;
		return reachedEnd;

	}

	/**
	 * 	{@inheritDoc}
	 */
	@Override
	public RandomAccessFile getStream() {
		return searchStream;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public InputStream loadStartBlock() {
		try {
			return new FileInputStream(new File(fileName));

		} catch (IOException e) {
			System.out.println("Root block not found");
		}
		return null;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void loadRandomBlock(int numberOfBlocks) {
		// nothing to do if using file system backend
		
	}

}
