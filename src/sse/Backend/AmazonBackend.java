package sse.Backend;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

public class AmazonBackend {

	private S3Service service;

	public AmazonBackend(String key, String secret) {
		AWSCredentials login = new AWSCredentials(key, secret);
		try {
			service = new RestS3Service(login);
		} catch (S3ServiceException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
	}

}
