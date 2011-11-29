package sse.Backend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.GSCredentials;
import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;
import sse.Vectors.Constants;

public class GoogleBackend implements Backend {

	private GoogleStorageService service;
	private String fileName;
	private String bucket;
	private RandomAccessFile searchStream;

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
	 */
	@Override
	public BinaryOut openNextFile(long currentBlock, BinaryOut w,
			SecurityEngine secEngine) {
		w.close();
		// Encrypt the last block if needed

		secEngine.encrypt(fileName + (currentBlock - 1));
		// remove the unencryted file
		new File(fileName + (currentBlock - 1)).delete();
		try {
			// upload the encrypted file
			GSObject obj = new GSObject(new File(fileName + (currentBlock - 1)
					+ ".sec"));
			service.putObject(bucket, obj);
			// delete the generated file
			new File(fileName + (currentBlock - 1) + ".sec").delete();
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
			w = new BinaryOut(fileName + currentBlock);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
		return w;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finalize(long currentBlock, BinaryOut w,
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

		w.write(currentBlock);
		w.close();

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
	public boolean searchNext(long block, String fileName, long position,
			long oldBlock, RandomAccessFile stream, SecurityEngine sEn)
			throws IOException {
		boolean reachedEnd = false;
		if (stream != null) {
			stream.close();
		}
		File delete = new File(fileName + oldBlock + ".dec");
		if (delete.exists()) {
			delete.delete();
		}
		try {
			GSObject obj = service.getObject(bucket, fileName + block + ".sec");

			sEn.decrypt(fileName + block, obj.getDataInputStream());
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");
			// if a block starts with a padding byte, it is a padding block =)
			if (stream.readByte() == Constants.PADDING_BYTE) {
				reachedEnd = true;
			}
			stream.seek(position);
			searchStream = stream;

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return reachedEnd;

	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public RandomAccessFile getStream() {
		return searchStream;
	}

	@Override
	/**
	 * 	{@inheritDoc}
	 */
	public InputStream loadStartBlock() {

		try {
			GSObject obj = service.getObject(bucket, fileName);

			return obj.getDataInputStream();
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
			GSObject obj = service.getObject(bucket,
					fileName + rnd.nextInt(numberOfBlocks) + ".sec");

			// if a block starts with a padding byte, it is a padding block =)

		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
