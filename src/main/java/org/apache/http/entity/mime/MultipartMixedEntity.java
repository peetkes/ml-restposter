/**
 * 
 */
package org.apache.http.entity.mime;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author pkester
 *
 */
public class MultipartMixedEntity implements HttpEntity {
    private final AbstractMixedMultipartForm multipart;
    private final Header contentType;
    private final long contentLength;


	/**
	 * 
	 */
	public MultipartMixedEntity(
            final AbstractMixedMultipartForm multipart,
            final String contentType,
            final long contentLength) {
        super();
        this.multipart = multipart;
        this.contentType = new BasicHeader(HTTP.CONTENT_TYPE, contentType);
        this.contentLength = contentLength;
	}

    AbstractMixedMultipartForm getMultipart() {
        return this.multipart;
    }

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#isRepeatable()
	 */
	@Override
	public boolean isRepeatable() {
        return this.contentLength != -1;
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpEntity#isChunked()
	 */
	@Override
	public boolean isChunked() {
        return !isRepeatable();
	}

    public boolean isStreaming() {
        return !isRepeatable();
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return null;
    }

    public void consumeContent()
        throws IOException, UnsupportedOperationException{
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException(
                    "Multipart form entity does not implement #getContent()");
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        this.multipart.writeTo(outstream);
    }
}
