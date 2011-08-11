package sse.IOHandler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityEngine {
	private Key secureKey;
	private byte[] iv;
	private Cipher c;

	public SecurityEngine() {
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(new SecureRandom());
			secureKey = kg.generateKey();
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");

		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printKey(String fileName) {
		BinaryOut keyStream;
		try {
			keyStream = new BinaryOut(fileName);
			byte[] key = secureKey.getEncoded();
			for (int i = 0; i < key.length; i++) {
				keyStream.write(key[i]);
			}
			keyStream.close();
			keyStream = new BinaryOut(fileName + ".iv");

			for (int i = 0; i < iv.length; i++) {
				keyStream.write(iv[i]);
			}
			keyStream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void readKey(String fileName) {
		DataInputStream keyStream;
		try {
			keyStream = new DataInputStream(new DataInputStream(
					new FileInputStream(new File(fileName))));
			byte[] key = new byte[16];
			keyStream.read(key, 0, 16);
			keyStream.close();
			secureKey = new SecretKeySpec(key, "AES");
			keyStream = new DataInputStream(new DataInputStream(
					new FileInputStream(new File(fileName + ".iv"))));
			iv = new byte[16];
			keyStream.read(iv, 0, 16);
			keyStream.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
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
