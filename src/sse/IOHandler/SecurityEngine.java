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

import java.io.InputStream;
import java.io.OutputStream;
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

/**
 * This class manages en/decryption of blocks. The algorithm used is AES-CBC
 * with 128 bits key length.
 * 
 * @author Jochen Rill
 * 
 */
public class SecurityEngine {
	private Key secureKey;
	private byte[] iv;
	private Cipher c;
	private byte[] salt;

	private SecretKeyFactory factory;

	public SecurityEngine(char[] password) {
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			salt = new byte[8];
			new SecureRandom().nextBytes(salt);

			KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);

			SecretKey tmp = factory.generateSecret(spec);
			secureKey = new SecretKeySpec(tmp.getEncoded(), "AES");

			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, secureKey);
			iv = c.getIV();

		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SecurityEngine(char[] password, byte[] salt, byte[] iv) {
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			this.salt = salt;
			this.iv = iv;

			KeySpec spec = new PBEKeySpec(password, this.salt, 1024, 128);

			SecretKey tmp = factory.generateSecret(spec);
			secureKey = new SecretKeySpec(tmp.getEncoded(), "AES");

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

	public byte[] getSalt() {
		return this.salt;
	}

	public byte[] getIV() {
		return this.iv;
	}

	public CipherInputStream decrypt(InputStream s) {
		try {

			c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv));

			CipherInputStream cis = new CipherInputStream(s, c);
			return cis;

		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public CipherOutputStream encrypt(OutputStream s) {

		try {
			c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv));

			CipherOutputStream cos = new CipherOutputStream(s, c);

			return cos;

		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());

		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
