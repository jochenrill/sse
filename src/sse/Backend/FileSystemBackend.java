/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/
package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.IOHandler.SecurityEngine;

/**
 * This is an implementation of a simple backend for the local file system.
 * 
 * @author Jochen Rill
 * 
 */
public class FileSystemBackend implements Backend {

	private String fileName;
	private RandomAccessFile stream;
	private DataOutputStream w;
	private SecurityEngine secEngine;
	private long currentBlock;

	public FileSystemBackend(String fileName) {
		this.fileName = fileName;

	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public DataOutputStream openBlock(long block) {

		if (w != null) {
			try {
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Encrypt the last block if needed
			secEngine.encrypt(fileName + (currentBlock));
			// remove the unencryted file
			new File(fileName + (currentBlock)).delete();
		}

		try {
			w = new DataOutputStream(new FileOutputStream(new File(fileName
					+ block)));
			currentBlock = block;
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName + block);
		}
		return w;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public void finalizeWriting() {

		// open writer for meta information file
		try {
			w.close();
			w = new DataOutputStream(new FileOutputStream(new File(fileName)));
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();
		try {
			w.writeLong(currentBlock);
			w.close();
			secEngine.printKey(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public RandomAccessFile searchNext(long block, long position) {

		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File delete = new File(fileName + currentBlock + ".dec");
			if (delete.exists()) {
				delete.delete();
			}
		}

		secEngine.decrypt(fileName + block);
		try {
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");
			currentBlock = block;
			stream.seek(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stream;

	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public DataInputStream loadStartBlock() {
		try {
			return new DataInputStream(new FileInputStream(new File(fileName)));

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

	@Override
	public void setSecurityEngine(SecurityEngine secEngine) {
		this.secEngine = secEngine;

	}

	@Override
	public void finalizeSearch() {
		try {
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File delete = new File(fileName + currentBlock + ".dec");
		if (delete.exists()) {
			delete.delete();
		}

	}

}
