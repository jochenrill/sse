package sse.Backend;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;

public interface Backend {

	public BinaryOut openNextFile(long currentBlock, boolean encrypt, BinaryOut w, SecurityEngine secEngine);
	public void finalize (long currentBlock, boolean encrypt,BinaryOut w, SecurityEngine secEngine);
	
}
