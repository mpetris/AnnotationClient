package eu.interedition.annotationclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class AnnotationTargetLoader {

	private URI targetURI;

	public AnnotationTargetLoader(URI targetURI) {
		super();
		this.targetURI = targetURI;
	}
	
	public String getTargetText() throws Exception {
		URL targetURL = targetURI.toURL();
		URLConnection targetURLConnection = targetURL.openConnection();
		InputStream targetInputStream = targetURLConnection.getInputStream();

		String encoding = findEncoding(targetURLConnection);
		String mimeType = findMimeType(targetURLConnection);
		//TODO: check mimeType, needs to be plain text
		
		return streamToString(targetInputStream, encoding);
	}
	
	private String streamToString(InputStream is, String charset) throws IOException {
		StringBuilder contentBuffer = new StringBuilder();
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				new BOMFilterInputStream(is, Charset.forName(charset)), charset ) );
		
		char[] buf = new char[65536];
		int cCount = -1;
        while((cCount=reader.read(buf)) != -1) {
        	contentBuffer.append( buf, 0, cCount);
        }

        return contentBuffer.toString();
	}
	
	
	
	
	private String findMimeType(URLConnection targetURLConnection) {
		String contentType = targetURLConnection.getContentType();
		String mimeType = null;
		if (contentType != null) {
			String[] contentTypeAttributes = contentType.split(";");
			if (contentTypeAttributes.length > 0) {
				mimeType  = contentTypeAttributes[0];
			}
		}
		if (mimeType == null) {
			mimeType = "text/plain";
			// throw new Exception("no mimetype available");
		}
		return mimeType;
	}

	private String findEncoding(URLConnection targetURLConnection) throws Exception {
		String encoding = targetURLConnection.getContentEncoding();
		if (encoding==null) {
			String contentType = targetURLConnection.getContentType();
			if (contentType.contains("charset")) {
				String[] contentTypeAttributes = contentType.split(";");
				String charsetAttribute = null;
				for (String attribute : contentTypeAttributes) {
					if (attribute.trim().startsWith("charset")) {
						charsetAttribute = attribute;
					}
				}
				if (charsetAttribute != null) {
					encoding = charsetAttribute.trim().substring(
							charsetAttribute.indexOf("=")).toUpperCase();
				}
			}
			if (encoding == null) {
				encoding = "UTF-8";
				//throw new Exception("no encoding available");
			}
		}
		return encoding;
	}

}
