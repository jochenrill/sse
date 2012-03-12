package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

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
	private DataOutputStream w;

	public SmartdriveBackend(String url, String user, String password,
			String fileName) {
		this.sardine = SardineFactory.begin(user, password);
		this.fileName = fileName;
		this.url = url;
	}

	@Override
	public void finalizeSearch() {
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

	@Override
	public void setSecurityEngine(SecurityEngine secEngine) {
		this.secEngine = secEngine;

	}

	@Override
	public DataOutputStream openBlock(int block) {
		if (w != null) {
			try {
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Encrypt the last block if needed

			secEngine.encrypt(fileName + (currentBlock));
			// remove the unencryted file
			new File(fileName + (currentBlock)).delete();

			try {

				byte[] data = FileUtils.readFileToByteArray(new File(fileName
						+ currentBlock + ".sec"));
				sardine.put(url, data);
				new File(fileName + (currentBlock) + ".sec").delete();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {

			w = new DataOutputStream(new FileOutputStream(new File(fileName
					+ block)));
			currentBlock = block;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return w;

	}

	@Override
	public void finalizeWriting() {
		try {
			w.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// open writer for meta information file
		try {
			w = new DataOutputStream(new FileOutputStream(new File(fileName)));
			w.writeLong(currentBlock);
			w.close();
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();

		// upload encrypted block

		try {
			InputStream fis = new FileInputStream(new File(fileName
					+ currentBlock + ".sec"));
			sardine.put(url, fis);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print Key writes the IV and salt to the meta information file
		secEngine.printKey(fileName);
		// upload meta block
		// upload encrypted block
		try {
			// upload the encrypted file
			InputStream fis = new FileInputStream(new File(fileName));
			sardine.put(url, fis);
			// delete the generated file
			new File(fileName).delete();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			s = sardine.get(url + fileName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (DataInputStream) s;
	}

	@Override
	public void loadRandomBlock(int numberOfBlocks) {
		Random rnd = new Random();
		try {
			int rand = rnd.nextInt(numberOfBlocks);
			sardine.get(url + fileName + rand + ".sec");
			secEngine.decrypt(fileName + rand,
					sardine.get(url + fileName + rand + ".sec"));
			new File(fileName + rand + ".dec").delete();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
