package sse.Backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;

public class GoogleBackend implements Backend {

	@Override
	public BinaryOut openNextFile(long currentBlock, BinaryOut w,
			SecurityEngine secEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finalize(long currentBlock, BinaryOut w,
			SecurityEngine secEngine) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean searchNext(long block, String fileName, long position,
			long oldBlock, RandomAccessFile stream, SecurityEngine sEn)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RandomAccessFile getStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream loadStartBlock() {
		// TODO Auto-generated method stub
		return null;
	}

}
