/**
 * 
 */
package org.apache.http.entity.mime.content;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.Args;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @author pkester
 *
 */
public class MLStringBody extends StringBody {
    private final byte[] content;
    private final String fileName;

	public MLStringBody(final String text, final ContentType contentType, final String fileName) {
        super(text, contentType);
        Args.notNull(text, "Text");
        final Charset charset = contentType.getCharset();
        final String csname = charset != null ? charset.name() : Consts.ASCII.name();
        this.fileName = fileName;
        try {
            this.content = text.getBytes(csname);
        } catch (final UnsupportedEncodingException ex) {
            // Should never happen
            throw new UnsupportedCharsetException(csname);
        }
	}

    public String getFilename() {
        return this.fileName;
    }

	public byte[] getContent() {
		return this.content;
	}

}
