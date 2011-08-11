package sse.Backend;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;

public interface Backend {

	public void openNextFile(long currentBlock, boolean encrypt, BinaryOut w, SecurityEngine secEngine);
	
}
