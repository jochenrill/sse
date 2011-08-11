package sse.Backend;

import java.io.File;
import java.io.IOException;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;

public class FileSystemBackend implements Backend {

	private String fileName;

	public FileSystemBackend(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public BinaryOut openNextFile(long currentBlock, boolean encrypt,
			BinaryOut w, SecurityEngine secEngine) {
		w.close();
		// Encrypt the last block if needed
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock - 1));
			// remove the unencryted file
			new File(fileName + (currentBlock - 1)).delete();

		}
		try {
			w = new BinaryOut(fileName + currentBlock);

		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
		return w;
	}

	@Override
	public void finalize(long currentBlock, boolean encrypt, BinaryOut w,
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
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock));
			// remove the unencryted file
			new File(fileName + (currentBlock)).delete();
		}
		secEngine.printKey("key");

		w.write(currentBlock);
		w.close();

	}

}
