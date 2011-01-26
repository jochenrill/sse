package sse.IOHandler;

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
    private Key secureKey;

    public SecurityEngine() {
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance("AES");
            kg.init(new SecureRandom());
            secureKey = kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void decrypt(String fileName) {
        try {
            FileOutputStream b = new FileOutputStream(fileName + ".dec");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, secureKey);
            CipherInputStream cis = new CipherInputStream(new FileInputStream(
                    fileName + ".sec"), c);
            byte[] block = new byte[8];
            int i;
            while ((i = cis.read(block)) != -1) {
                b.write(block, 0, i);
            }
            cis.close();
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

    public void encrypt(String fileName) {
        try {
            DataInputStream b = new DataInputStream(new FileInputStream(
                    fileName));
            Cipher c = Cipher.getInstance("AES");
            
            c.init(Cipher.ENCRYPT_MODE, secureKey);
            CipherOutputStream cos = new CipherOutputStream(
                    new FileOutputStream(fileName + ".sec"), c);
            int i;
            while ((i = b.read()) != -1) {
                cos.write(i);
            }
            cos.close();
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
}
