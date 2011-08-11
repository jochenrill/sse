package sse.Backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;

public interface Backend {

	public BinaryOut openNextFile(long currentBlock, boolean encrypt,
			BinaryOut w, SecurityEngine secEngine);

	public void finalize(long currentBlock, boolean encrypt, BinaryOut w,
			SecurityEngine secEngine);

	public boolean searchNext(long block, String fileName,
			long position, long oldBlock, RandomAccessFile stream, SecurityEngine sEn) throws IOException;
	
	public RandomAccessFile getStream();
	
	public InputStream loadStartBlock();
	

}
