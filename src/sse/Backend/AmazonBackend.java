package sse.Backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import sse.IOHandler.BinaryOut;
import sse.IOHandler.SecurityEngine;
import sse.Vectors.Constants;

public class AmazonBackend implements Backend {

	private S3Service service;
	private String fileName;
	private S3Bucket bucket;
	private RandomAccessFile searchStream;

	public AmazonBackend(String key, String secret, String fileName,
			String bucket) {
		AWSCredentials login = new AWSCredentials(key, secret);
		this.fileName = fileName;
		try {
			service = new RestS3Service(login);
			this.bucket = service.getBucket(bucket);

		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public BinaryOut openNextFile(long currentBlock, boolean encrypt,
			BinaryOut w, SecurityEngine secEngine) {
		w.close();
		// Encrypt the last block if needed
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock - 1));
			// remove the unencryted file
			new File(fileName + (currentBlock - 1)).delete();
			try {
				// upload the encrypted file
				S3Object obj = new S3Object(new File(fileName
						+ (currentBlock - 1) + ".sec"));
				service.putObject(bucket, obj);
				// delete the generated file
				new File(fileName + (currentBlock - 1) + ".sec").delete();
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
		try {
			w = new BinaryOut(fileName + currentBlock);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName
					+ currentBlock);
		}
		return w;

	}

	@Override
	public void finalize(long currentBlock, boolean encrypt, BinaryOut w,
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
		if (encrypt) {
			secEngine.encrypt(fileName + (currentBlock));
			// remove the unencryted file
			new File(fileName + (currentBlock)).delete();

			// upload encrypted block
			try {
				// upload the encrypted file
				S3Object obj = new S3Object(new File(fileName + (currentBlock)
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
			} catch (S3ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		secEngine.printKey("key");

		w.write(currentBlock);
		w.close();
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
			S3Object obj = service.getObject(bucket, fileName + block + ".sec");

			sEn.decrypt(fileName + block, obj.getDataInputStream());
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");
			// if a block starts with a padding byte, it is a padding block =)
			if (stream.readByte() == Constants.PADDING_BYTE) {
				reachedEnd = true;
			}
			stream.seek(position);
			searchStream = stream;
		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return reachedEnd;

	}

	@Override
	public RandomAccessFile getStream() {
		// TODO Auto-generated method stub
		return searchStream;
	}

	@Override
	public InputStream loadStartBlock() {

		try {
			S3Object obj = service.getObject(bucket, fileName);

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

}
