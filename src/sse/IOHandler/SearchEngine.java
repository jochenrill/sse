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
package sse.IOHandler;

import java.io.File;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Constants;
import sse.Backend.Backend;

public class SearchEngine {
	private RandomAccessFile stream;
	private long currentBlock;
	private SecurityEngine sEn;

	private long numOccurs;
	private int files = 0;
	private boolean stop = false;
	private Backend backend;
	private String startFile;
	private int searchLength;

	public SearchEngine(Backend backend, String startFile, char password[]) {
		this.sEn = new SecurityEngine(password);
		this.sEn.readKey(backend.loadStartBlock());

		this.currentBlock = 0;
		this.backend = backend;
		this.startFile = startFile;
	}

	public int getTransferedFilesCount() {
		return files;
	}

	public long find(String word) {
		@SuppressWarnings("unused")
		long blockSize = 0;
		long numberOfBlocks = 0;

		searchLength = word.length();
		DataInputStream firstStream = new DataInputStream(
				backend.loadStartBlock());

		try {
			blockSize = firstStream.readLong();
			numberOfBlocks = firstStream.readLong();
			firstStream.close();
		} catch (IOException e1) {
			System.out.println("Startblock not found! Exiting.");
			System.exit(0);
		}

		try {
			long toDelete = currentBlock;
			// open the first block
			files++;
			backend.searchNext(currentBlock, startFile, 0, toDelete, stream,
					sEn);
			stream = backend.getStream();
			long currentBlock = 0;

			// parse a node
			while (!stop) {

				stop = true;
				// read numOccurs
				switch (Constants.NUMOCCURS_BYTES) {
				case 8:
					numOccurs = stream.readLong();
					break;
				case 4:
					numOccurs = (long) stream.readInt();
					break;
				case 2:
					numOccurs = (long) stream.readShort();
					break;
				case 1:
					numOccurs = (long) stream.readChar();
					break;
				default:
					throw new UnsupportedOperationException(
							Constants.NUMOCCURS_BYTES
									+ " is not a valid number for number of occurences");
				}
				if (word.length() == 0) {
					break;
				}

				// now the edges follow ..

				char c = (char) stream.readByte();
				while (c != Constants.VECTOR_MARKER) {

					// read block reference and edge reference
					long block, edge;

					switch (Constants.EDGE_REFERENCE_BYTES) {
					case 8:
						edge = stream.readLong();
						break;
					case 4:
						edge = (long) stream.readInt();
						break;
					case 2:
						edge = (long) stream.readShort();
						break;
					case 1:
						edge = (long) stream.readChar();
						break;
					default:
						throw new UnsupportedOperationException(
								Constants.EDGE_REFERENCE_BYTES
										+ " is not a valid number for edge reference");
					}
					switch (Constants.BLOCK_REFERENCE_BYTES) {
					case 8:
						block = stream.readLong();
						break;
					case 4:
						block = (long) stream.readInt();
						break;
					case 2:
						block = (long) stream.readShort();
						break;
					case 1:
						block = (long) stream.readChar();
						break;
					default:
						throw new UnsupportedOperationException(
								Constants.BLOCK_REFERENCE_BYTES
										+ " is not a valid number for block reference");
					}
					if (c == word.charAt(0)) {
						word = word.substring(1);

						// follow that edge
						backend.searchNext(block, startFile, edge,
								currentBlock, stream, sEn);
						stream = backend.getStream();
						stop = false;
						break;

					} else {
						c = (char) stream.readByte();
					}
				}

			}
		} catch (IOException e) {
			System.out.println("Error while parsing block " + currentBlock);
		}
		// Make sure to delete the last opened block
		File delete = new File(startFile + currentBlock + ".dec");
		if (delete.exists()) {
			delete.delete();
		}

		// request random blocks to make sure the amount of transfered blocks is
		// always word.length+1
		while (files < searchLength + 1) {
			backend.loadRandomBlock((int) numberOfBlocks, sEn);
			files++;
		}

		if (word.length() == 0) {
			return numOccurs;

		} else {
			return 0;
		}
	}

}
