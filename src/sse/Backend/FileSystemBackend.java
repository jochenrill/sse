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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import sse.IOHandler.BlockInputStream;
import sse.IOHandler.BlockOutputStream;
import sse.IOHandler.SecurityEngine;
import sse.IOHandler.OutputFormat.Block;

/**
 * This is an implementation of a simple backend for the local file system.
 * 
 * @author Jochen Rill
 * 
 */
public class FileSystemBackend implements Backend {

	private String fileName;
	private SecurityEngine secEngine;

	public FileSystemBackend(String fileName, char[] password, String ivFile) {
		this.fileName = fileName;
		try {

			if (!(new File(ivFile).exists())) {
				// generate and write new IV
				secEngine = new SecurityEngine(password);
				writeIV(ivFile);
			} else {
				DataInputStream s = new DataInputStream(new FileInputStream(
						new File(ivFile)));
				byte[] salt = new byte[8];
				byte[] iv = new byte[16];

				for (int i = 0; i < 8; i++) {
					salt[i] = s.readByte();
				}

				for (int i = 0; i < 16; i++) {
					iv[i] = s.readByte();
				}
				secEngine = new SecurityEngine(password, salt, iv);
				s.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeIV(String ivFile) {
		try {
			DataOutputStream s = new DataOutputStream(new FileOutputStream(
					new File(ivFile)));

			s.write(secEngine.getSalt());
			s.write(secEngine.getIV());
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Block openBlock(int blockId, int index) {

		try {
			BlockInputStream os = new BlockInputStream(new ObjectInputStream(
					new GZIPInputStream(secEngine.decrypt(new FileInputStream(
							new File(fileName + blockId))))));
			// Object o = os.readObject();
			// os.close();
			// if (o instanceof Block[]) {
			// Block[] b = (Block[]) o;
			// return b[index];
			// }
			Block[] result = os.readBlocks();
			os.close();
			return result[index];

		} catch (IOException e) {
			System.out
					.println("Error while reading file " + fileName + blockId);
			System.out.println(e.getMessage());
		}
		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	public void loadRandomBlock(int numberOfBlocks) {
		// nothing to do if using file system backend

	}

	@Override
	public void writeBlock(Block b, int blockId) {
		ArrayList<Block> list = new ArrayList<Block>();
		list.add(b);
		writeBlockArray(list, blockId);
	}

	@Override
	public void writeBlockArray(ArrayList<Block> b, int blockId) {
		try {

			BlockOutputStream os = new BlockOutputStream(
					new ObjectOutputStream(new GZIPOutputStream(
							secEngine.encrypt(new FileOutputStream(new File(
									fileName + blockId))))));

			// convert ArrayList into Array to reduce overhead

			os.writeBlocks(b.toArray(new Block[0]));
			os.close();
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName + blockId);
			System.out.println(e.getMessage());
		}

	}

}
