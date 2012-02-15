package sse.Backend;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import sse.Constants;
import sse.IOHandler.SecurityEngine;

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
	public DataOutputStream openNextFile(int currentBlock, int nextBlock,
			DataOutputStream w, SecurityEngine secEngine) {
		try {
			w.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Encrypt the last block if needed

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();

		try {
			w = new DataOutputStream(new FileOutputStream(new File(fileName
					+ nextBlock)));

		} catch (IOException e) {
			System.out.println("Could not create file " + fileName + nextBlock);
		}
		return w;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public void finalize(long currentBlock, DataOutputStream w,
			SecurityEngine secEngine) throws IOException {
		w.close();
		// open writer for meta information file
		try {
			w = new DataOutputStream(new FileOutputStream(new File(fileName)));
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();
		w.writeLong(currentBlock);
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
		// TODO: not needed anymore?
		if (stream.readByte() == Constants.PADDING_BYTE) {
			reachedEnd = true;
		}
		stream.seek(position);
		searchStream = stream;
		return reachedEnd;

	}

	/**
	 * {@inheritDoc}
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
	public void loadRandomBlock(int numberOfBlocks, SecurityEngine sEn) {
		// nothing to do if using file system backend

	}

}
