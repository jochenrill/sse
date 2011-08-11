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
	public void openNextFile(long currentBlock, boolean encrypt, BinaryOut w,
			SecurityEngine secEngine) {
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

	}

}
