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
package sse.IOHandler;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityEngine {
	private Key secureKey;
	private byte[] iv;
	private Cipher c;
	private byte[] salt;
	private char[] password;
	private SecretKeyFactory factory;

	public SecurityEngine(char[] password) {
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			// Read a password from console

			this.password = password;

			salt = new byte[8];
			new SecureRandom().nextBytes(salt);

			KeySpec spec = new PBEKeySpec(this.password, salt, 1024, 128);

			SecretKey tmp = factory.generateSecret(spec);
			secureKey = new SecretKeySpec(tmp.getEncoded(), "AES");
			/*
			 * kg = KeyGenerator.getInstance("AES"); kg.init(new
			 * SecureRandom()); secureKey = kg.generateKey();
			 */

			c = Cipher.getInstance("AES/CBC/PKCS5Padding");

		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printKey(String fileName) {
		BinaryOut keyStream;
		try {
			keyStream = new BinaryOut(fileName, true);
			/*
			 * byte[] key = secureKey.getEncoded(); for (int i = 0; i <
			 * key.length; i++) { keyStream.write(key[i]); } keyStream.close();
			 */

			for (int i = 0; i < iv.length; i++) {
				keyStream.write(iv[i]);
			}

			for (int i = 0; i < salt.length; i++) {
				keyStream.write(salt[i]);
			}
			keyStream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void readKey(InputStream key) {
		DataInputStream keyStream;
		try {
			/*
			 * keyStream = new DataInputStream(new DataInputStream( new
			 * FileInputStream(new File(fileName)))); byte[] key = new byte[16];
			 * keyStream.read(key, 0, 16); keyStream.close();
			 */
			// secureKey = new SecretKeySpec(key, "AES");
			keyStream = new DataInputStream(new DataInputStream(key));
			// Jump over the irrelevant information
			keyStream.readLong();
			keyStream.readLong();

			// read IV and Key
			iv = new byte[16];
			keyStream.read(iv, 0, 16);
			salt = new byte[8];
			keyStream.read(salt, 0, 8);
			keyStream.close();
			KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
			SecretKey tmp = factory.generateSecret(spec);
			secureKey = new SecretKeySpec(tmp.getEncoded(), "AES");

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void decrypt(String fileName) {
		try {
			FileOutputStream b = new FileOutputStream(fileName + ".dec");

			c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv));

			CipherInputStream cis = new CipherInputStream(new FileInputStream(
					fileName + ".sec"), c);
			byte[] block = new byte[8];
			int i;
			while ((i = cis.read(block)) != -1) {
				b.write(block, 0, i);
			}
			cis.close();
			b.close();

		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e.getMessage());
		}
	}

	public void decrypt(String fileName, InputStream s) {
		try {
			FileOutputStream b = new FileOutputStream(fileName + ".dec");

			c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv));

			CipherInputStream cis = new CipherInputStream(s, c);
			byte[] block = new byte[8];
			int i;
			while ((i = cis.read(block)) != -1) {
				b.write(block, 0, i);
			}
			cis.close();
			b.close();

		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e.getMessage());
		}
	}

	public void encrypt(String fileName) {
		try {
			DataInputStream b = new DataInputStream(new FileInputStream(
					fileName));

			if (iv == null) {
				c.init(Cipher.ENCRYPT_MODE, secureKey);
				iv = c.getIV();
			}
			CipherOutputStream cos = new CipherOutputStream(
					new FileOutputStream(fileName + ".sec"), c);
			int i;
			while ((i = b.read()) != -1) {
				cos.write(i);
			}
			cos.close();
			b.close();

		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
