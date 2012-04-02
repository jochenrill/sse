package sse.IOHandler;

import java.io.DataInputStream;	
import java.io.IOException;
import java.io.RandomAccessFile;

import sse.Constants;
import sse.Backend.Backend;

/**
 * This class implements a search engine on the encrypted data. For correct
 * work, it needs a correctly implemented backend.
 * 
 * @author Jochen Rill
 * 
 */
public class SearchEngine {
	private RandomAccessFile stream;

	private long numOccurs;
	private int files = 0;
	private boolean stop = false;
	private Backend backend;
	private int searchLength;

	public SearchEngine(Backend backend, char password[]) {
		SecurityEngine sEn = new SecurityEngine(password);
		sEn.readKey(backend.loadStartBlock());
		backend.setSecurityEngine(sEn);

		this.backend = backend;

	}

	public int getTransferedFilesCount() {
		return files;
	}

	public long find(String word) {
		long numberOfBlocks = 0;
		int currentBlock = 0;
		searchLength = word.length();
		DataInputStream firstStream = backend.loadStartBlock();

		try {
			numberOfBlocks = firstStream.readLong();
			firstStream.close();
		} catch (IOException e1) {
			System.out.println("Startblock not found! Exiting.");
			System.exit(0);
		}

		try {

			files++;
			stream = backend.searchNext(currentBlock, 0);

			// parse a node
			while (!stop) {

				stop = true;
				// read numOccurs
				byte[] buffer = new byte[Constants.NUMOCCURS_BYTES];
				stream.read(buffer);
				numOccurs = fromByteArray(buffer, Constants.NUMOCCURS_BYTES);

				if (word.length() == 0) {
					break;
				}

				// now the edges follow ..

				char c = (char) stream.readByte();
				while (c != Constants.VECTOR_MARKER) {

					// read block reference and edge reference
					long block, edge;
					buffer = new byte[Constants.EDGE_REFERENCE_BYTES];
					stream.read(buffer);
					edge = fromByteArray(buffer, Constants.EDGE_REFERENCE_BYTES);

					buffer = new byte[Constants.BLOCK_REFERENCE_BYTES];
					stream.read(buffer);
					block = fromByteArray(buffer,
							Constants.BLOCK_REFERENCE_BYTES);

					if (c == word.charAt(0)) {
						word = word.substring(1);

						// follow that edge
						stream = backend.searchNext(block, edge);

						stop = false;
						break;

					} else {
						c = (char) stream.readByte();
					}
				}

			}
		} catch (IOException e) {
			System.out.println("Error while parsing block " + currentBlock
					+ "\n" + e.getMessage());
		}
		// Make sure to delete the last opened block
		backend.finalizeSearch();

		// request random blocks to make sure the amount of transfered blocks is
		// always word.length+1
		while (files < searchLength + 1) {
			backend.loadRandomBlock((int) numberOfBlocks);
			files++;
		}

		if (word.length() == 0) {
			return numOccurs;

		} else {
			return 0;
		}
	}

	private long fromByteArray(byte[] b, int bytes) {

		long result = 0;

		for (int i = 0; i < bytes; i++) {

			result += (b[(bytes - 1) - i] & 0xFF) << (i * 8);

		}

		return result;
	}

}
