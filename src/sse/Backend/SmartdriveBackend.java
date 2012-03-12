package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

import sse.IOHandler.SecurityEngine;

public class SmartdriveBackend implements Backend {

	private Sardine sardine;
	private String url;
	private SecurityEngine secEngine;
	private RandomAccessFile stream;
	private String fileName;
	private long currentBlock;

	public SmartdriveBackend(String url, String user, String password,
			String fileName) {
		this.sardine = SardineFactory.begin(user, password);
		this.fileName = fileName;
	}

	@Override
	public void finalizeSearch() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSecurityEngine(SecurityEngine secEngine) {
		this.secEngine = secEngine;

	}

	@Override
	public DataOutputStream openBlock(int block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finalizeWriting() {
		// TODO Auto-generated method stub

	}

	@Override
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
		try {
			InputStream s = sardine.get(url + fileName + block + ".sec");

			secEngine.decrypt(fileName + block, s);
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");

			// if a block starts with a padding byte, it is a padding block =)
			currentBlock = block;
			stream.seek(position);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stream;

	}

	@Override
	public DataInputStream loadStartBlock() {
		InputStream s = null;
		try {
			s = sardine.get(url + fileName + ".sec");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (DataInputStream) s;
	}

	@Override
	public void loadRandomBlock(int numberOfBlocks) {
		// TODO Auto-generated method stub

	}

}
