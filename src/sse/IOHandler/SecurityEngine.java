package sse.IOHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class SecurityEngine {

	public static void decrypt(String fileName, String keyFile) {

		try {

			BinaryOut b = new BinaryOut(fileName + ".dec");
			Cipher c = Cipher.getInstance("AES");
			/*
			 * KeyGenerator kg = KeyGenerator.getInstance("AES"); kg.init(new
			 * SecureRandom());
			 */
			DataInputStream keyStream = new DataInputStream(
					new DataInputStream(new FileInputStream(new File(keyFile))));

			int pos = 0;
			byte[] key = new byte[keyStream.available()];
			while (pos < keyStream.available()) {
				key[pos] = (byte) keyStream.read();
				pos++;
			}

			keyStream.close();
			Key k = new SecretKeySpec(key, "AES");
			c.init(Cipher.DECRYPT_MODE, k);
			CipherInputStream o = new CipherInputStream(new FileInputStream(
					fileName + ".sec"), c);

			pos = 0;
			int in = 0;
			while ((in = o.read()) != -1) {
				b.write((byte) in);
				pos++;
			}
			o.close();
			b.close();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void encrypt(String fileName, String keyFile) {

		try {

			DataInputStream b = new DataInputStream(
					new FileInputStream(fileName));
			Cipher c = Cipher.getInstance("AES");
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(new SecureRandom());
			BinaryOut keyStream = new BinaryOut(keyFile);

			SecretKey k = kg.generateKey();

			byte[] key = k.getEncoded();
			for (int i = 0; i < key.length; i++) {
				keyStream.write(key[i]);
			}
			keyStream.close();

			c.init(Cipher.ENCRYPT_MODE, k);
			CipherOutputStream o = new CipherOutputStream(new FileOutputStream(
					fileName + ".sec"), c);

			int pos = 0;
			while (pos < b.available()) {
				o.write(b.read());
				pos++;
			}
			o.close();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
