package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.GSCredentials;

import sse.IOHandler.SecurityEngine;

public class GoogleBackend implements Backend {

	private GoogleStorageService service;
	private String fileName;
	private String bucket;
	private RandomAccessFile stream;
	private long currentBlock;
	private DataOutputStream w;
	private SecurityEngine secEngine;

	public GoogleBackend(String key, String secret, String fileName,
			String bucket) {
		GSCredentials login = new GSCredentials(key, secret);
		this.fileName = fileName;

		try {
			service = new GoogleStorageService(login);
			this.bucket = bucket;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public DataOutputStream openBlock(int block) {
		try {
			w.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Encrypt the last block if needed

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();
		try {
			// upload the encrypted file
			GSObject obj = new GSObject(new File(fileName + (currentBlock)
					+ ".sec"));
			service.putObject(bucket, obj);
			// delete the generated file
			new File(fileName + (currentBlock) + ".sec").delete();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			w = new DataOutputStream(new FileOutputStream(new File(fileName
					+ block)));
			currentBlock = block;
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
		return w;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
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
			w = new DataOutputStream(new FileOutputStream(new File(fileName),
					true));
			w.writeLong(currentBlock);
			w.close();
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();

		// upload encrypted block
		try {
			// upload the encrypted file
			GSObject obj = new GSObject(new File(fileName + (currentBlock)
					+ ".sec"));
			service.putObject(bucket, obj);
			// delete the generated file
			new File(fileName + (currentBlock) + ".sec").delete();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print Key writes the IV and salt to the meta information file
		secEngine.printKey(fileName);
		// upload meta block
		// upload encrypted block
		try {
			// upload the encrypted file
			S3Object obj = new S3Object(new File(fileName));
			service.putObject(bucket, obj);
			// delete the generated file
			new File(fileName).delete();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
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
			GSObject obj = service.getObject(bucket, fileName + block + ".sec");

			secEngine.decrypt(fileName + block, obj.getDataInputStream());
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");

			stream.seek(position);
			currentBlock = block;

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stream;

	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public DataInputStream loadStartBlock() {

		try {
			GSObject obj = service.getObject(bucket, fileName);

			return new DataInputStream(obj.getDataInputStream());
			// if a block starts with a padding byte, it is a padding block =)

		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void loadRandomBlock(int numberOfBlocks) {
		Random rnd = new Random();
		try {
			int rand = rnd.nextInt(numberOfBlocks);
			GSObject obj = service.getObject(bucket,
					fileName + rnd.nextInt(numberOfBlocks) + ".sec");
			secEngine.decrypt(fileName + rand, obj.getDataInputStream());
			new File(fileName + rand + ".dec").delete();
			// if a block starts with a padding byte, it is a padding block =)

		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
