package sse.Backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multi.SimpleThreadedStorageService;
import org.jets3t.service.security.AWSCredentials;

import sse.IOHandler.SecurityEngine;

public class AmazonBackend implements Backend {

	private S3Service service;
	private String fileName;
	private S3Bucket bucket;
	private LinkedList<String> fileNames;
	private LinkedList<S3Object> listOfObjects;
	private RandomAccessFile stream;
	private SecurityEngine secEngine;
	private SimpleThreadedStorageService multi;
	private DataOutputStream w;
	private long currentBlock;

	public AmazonBackend(String key, String secret, String fileName,
			String bucket) {
		AWSCredentials login = new AWSCredentials(key, secret);
		this.fileName = fileName;
		try {
			service = new RestS3Service(login);
			this.bucket = service.getBucket(bucket);
			fileNames = new LinkedList<String>();
			multi = new SimpleThreadedStorageService(service);
			listOfObjects = new LinkedList<S3Object>();

		} catch (S3ServiceException e) {
			System.out.println("S3 Service Exception: " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
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
		}
		try {

			S3Object obj = new S3Object(new File(fileName + (currentBlock)
					+ ".sec"));
			fileNames.add(fileName + (currentBlock) + ".sec");
			listOfObjects.add(obj);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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

			// this is the last block
			S3Object obj = new S3Object(new File(fileName + (currentBlock)
					+ ".sec"));
			listOfObjects.add(obj);
			fileNames.add(fileName + (currentBlock) + ".sec");
			multi.putObjects(bucket.getName(),
					listOfObjects.toArray(new S3Object[0]));
			for (String s : fileNames) {
				new File(s).delete();
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (S3ServiceException e) {
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
		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
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
			S3Object obj = service.getObject(bucket, fileName + block + ".sec");
			secEngine.decrypt(fileName + block, obj.getDataInputStream());
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");
			// if a block starts with a padding byte, it is a padding block =)
			currentBlock = block;
			stream.seek(position);

		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			@SuppressWarnings("deprecation")
			S3Object obj = service.getObject(bucket, fileName);

			return new DataInputStream(obj.getDataInputStream());

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
			@SuppressWarnings("deprecation")
			S3Object obj = service.getObject(bucket, fileName + rand + ".sec");
			secEngine.decrypt(fileName + rand, obj.getDataInputStream());
			new File(fileName + rand + ".dec").delete();

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
