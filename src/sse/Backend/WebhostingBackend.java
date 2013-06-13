package sse.Backend;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

import sse.IOHandler.SecurityEngine;

public class WebhostingBackend implements Backend {

	private Sardine sardine;
	private String url;
	private SecurityEngine secEngine;
	private RandomAccessFile stream;
	private String fileName;
	private long currentBlock;
	private DataOutputStream w;

	public WebhostingBackend(String url, String user, String password,
			String fileName) {
		this.sardine = SardineFactory.begin(user, password);

		this.fileName = fileName;
		this.url = url;
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

	@Override
	public DataOutputStream openBlock(long block) {

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
			w = new DataOutputStream(new FileOutputStream(new File(fileName
					+ block)));
			currentBlock = block;
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName + block);
		}
		return w;

	}

	@Override
	public void finalizeWriting() {
		// open writer for meta information file
		try {
			w.close();
			w = new DataOutputStream(new FileOutputStream(new File(fileName)));
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName);
		}
		// write number of blocks

		// encrypt the last block

		secEngine.encrypt(fileName + (currentBlock));
		// remove the unencryted file
		new File(fileName + (currentBlock)).delete();
		try {
			w.writeLong(currentBlock);
			w.close();
			secEngine.printKey(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// zip-and-unzip
		try {
			ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(new File("archive.zip")));
			for(String str : new File("files/").list()){
				zipStream.putNextEntry(new ZipEntry(str));
				zipStream.write(FileUtils.readFileToByteArray(new File("files/"+str)));
				zipStream.closeEntry();
			}
			zipStream.close();
			
			sardine.put(url + "archive.zip", FileUtils.readFileToByteArray(new File("archive.zip")));
			URL unzip = new URL("http://s406809229.online.de/KIT/unzip.php");
			BufferedReader rd = new BufferedReader(new InputStreamReader(unzip.openStream()));
			String inputLine;
			while((inputLine = rd.readLine()) != null){
				
			}
			rd.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


	}

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
			InputStream s = sardine.get(url + fileName + block + ".sec");

			secEngine.decrypt(fileName + block, s);
			stream = new RandomAccessFile(new File(fileName + block + ".dec"),
					"r");

			currentBlock = block;
			stream.seek(position);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stream;

	}

	@Override
	public DataInputStream loadStartBlock() {
		InputStream s = null;
		try {
			s = sardine.get(url + fileName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DataInputStream(s);
	}

	@Override
	public void loadRandomBlock(int numberOfBlocks) {
		Random rnd = new Random();
		try {
			int rand = rnd.nextInt(numberOfBlocks);

			secEngine.decrypt(fileName + rand,
					sardine.get(url + fileName + rand + ".sec"));
			new File(fileName + rand + ".dec").delete();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
