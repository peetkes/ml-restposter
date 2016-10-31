/**
 * 
 */
package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author pkester
 *
 */
public class HttpBrowserCompatibleMixedMultipart extends AbstractMixedMultipartForm {
    private final List<MixedBodyPart> parts;

	/**
	 * @param subType
	 * @param charset
	 * @param boundary
	 */
	public HttpBrowserCompatibleMixedMultipart(
			String subType, 
			Charset charset,
			String boundary,
			final List<MixedBodyPart> parts) {
		super(subType, charset, boundary);
		this.parts = parts;
	}

	public List<MixedBodyPart> getBodyParts() {
		return this.parts;
	}

	@Override
	protected void formatMultipartHeader(MixedBodyPart part, OutputStream out)
			throws IOException {
	       // For browser-compatible, only write Content-Disposition
	       // Use content charset
	       final Header header = part.getHeader();
	       final MinimalField cd = header.getField(MIME.CONTENT_DISPOSITION);
	       writeField(cd, this.charset, out);
	       final String filename = part.getBody().getFilename();
	       if (filename != null) {
	           final MinimalField ct = header.getField(MIME.CONTENT_TYPE);
	           writeField(ct, this.charset, out);
	       }
	}
}
