/**
 * 
 */
package org.apache.http.entity.mime;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.util.Args;

/**
 * @author pkester
 *
 */
public class MixedBodyPart {

    private final String name;
    private final Header header;

    private final ContentBody body;

    public MixedBodyPart(final String name, final ContentBody body) {
        super();
        
        Args.notNull(name, "Name");
        Args.notNull(body, "Body");
        this.name = name;
        this.body = body;
        this.header = new Header();

        generateContentDisp(name, body);
        generateContentType(body);
        generateTransferEncoding(body);
        generateContentLength(body);
    }

    public ContentBody getBody() {
        return this.body;
    }

    public Header getHeader() {
        return this.header;
    }

    public void addField(final String name, final String value) {
        Args.notNull(name, "Field name");
        this.header.addField(new MinimalField(name, value));
    }

    protected void generateContentDisp(final String name,final ContentBody body) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        if (body.getFilename() != null) {
            buffer.append("; filename=\"");
            buffer.append(body.getFilename());
            buffer.append("\"");
        }
        addField(MIME.CONTENT_DISPOSITION, buffer.toString());
    }

    protected void generateContentType(final ContentBody body) {
        final ContentType contentType;
        if (body instanceof AbstractContentBody) {
            contentType = ((AbstractContentBody) body).getContentType();
        } else {
            contentType = null;
        }
        if (contentType != null) {
            addField(MIME.CONTENT_TYPE, contentType.toString());
        } else {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(body.getMimeType()); // MimeType cannot be null
            if (body.getCharset() != null) { // charset may legitimately be null
                buffer.append("; charset=");
                buffer.append(body.getCharset());
            }
            addField(MIME.CONTENT_TYPE, buffer.toString());
        }
    }

    protected void generateTransferEncoding(final ContentBody body) {
//        addField(MIME.CONTENT_TRANSFER_ENC, body.getTransferEncoding()); // TE cannot be null
    }

    protected void generateContentLength(final ContentBody body) {
      addField("Content-Length", String.valueOf(body.getContentLength())); // TE cannot be null
  }
}
