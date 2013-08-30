/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/
package sse.Backend;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multi.SimpleThreadedStorageService;
import org.jets3t.service.security.AWSCredentials;

import sse.IOHandler.SecurityEngine;
import sse.IOHandler.OutputFormat.Block;

/**
 * This is a implementation of a backend for Amazon S3. It uses threaded uploads
 * provided by the jets3t-library.
 * 
 * @author Jochen Rill
 * 
 */
public class AmazonBackend implements Backend {

	private S3Service service;
	private String fileName;
	private S3Bucket bucket;
	private SecurityEngine secEngine;
	private SimpleThreadedStorageService multi;

	public AmazonBackend(String key, String secret, String fileName,
			String bucket, char[] password) {
		AWSCredentials login = new AWSCredentials(key, secret);
		this.fileName = fileName;
		secEngine = new SecurityEngine(password);
		try {
			service = new RestS3Service(login);
			this.bucket = service.getBucket(bucket);
			multi = new SimpleThreadedStorageService(service);

		} catch (S3ServiceException e) {
			System.out.println("S3 Service Exception: " + e.getMessage());
		}
	}

	public AmazonBackend(String key, String secret, String fileName,
			String bucket, char[] password, String ivFile) {
		AWSCredentials login = new AWSCredentials(key, secret);
		this.fileName = fileName;

		try {
			service = new RestS3Service(login);
			this.bucket = service.getBucket(bucket);
			multi = new SimpleThreadedStorageService(service);
			S3Object obj = service.getObject(bucket, ivFile);

			DataInputStream s = new DataInputStream(obj.getDataInputStream());
			byte[] salt = new byte[8];
			byte[] iv = new byte[16];

			for (int i = 0; i < 8; i++) {
				salt[i] = s.readByte();
			}

			for (int i = 8; i < 16; i++) {
				iv[i] = s.readByte();
			}
			s.close();
			secEngine = new SecurityEngine(password, salt, iv);

		} catch (S3ServiceException e) {
			System.out.println("S3 Service Exception: " + e.getMessage());
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Block openBlock(int blockID, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	private void writeIV(String ivFile) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBlock(Block b, int blockId) {
		ArrayList<Block> list = new ArrayList<Block>();
		list.add(b);
		writeBlockArray(list, blockId);
	}

	@Override
	public void writeBlockArray(ArrayList<Block> b, int blockId) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(
					new GZIPOutputStream(
							secEngine.encrypt(new FileOutputStream(new File(
									fileName + blockId)))));
			os.writeObject(b);
			os.close();
			S3Object s3o = new S3Object(bucket, new File(fileName + blockId));

			multi.putObjects(s3o.getBucketName(), new S3Object[] { s3o });

			new File(fileName + blockId).delete();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
