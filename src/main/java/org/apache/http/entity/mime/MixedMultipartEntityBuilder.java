/**
 * 
 */
package org.apache.http.entity.mime;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.*;
import org.apache.http.util.Args;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author pkester
 *
 */
public class MixedMultipartEntityBuilder {
	   /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    private final static String DEFAULT_SUBTYPE = "mixed";
    private String subType = DEFAULT_SUBTYPE;
    private HttpMultipartMode mode = HttpMultipartMode.STRICT;
    private String boundary = null;
    private Charset charset = null;
    private List<MixedBodyPart> bodyParts = null;

	MixedMultipartEntityBuilder() {
		super();
	}

	public static MixedMultipartEntityBuilder create() {
        return new MixedMultipartEntityBuilder();
		
	}
    public MixedMultipartEntityBuilder setMode(final HttpMultipartMode mode) {
        this.mode = mode;
        return this;
    }

    public MixedMultipartEntityBuilder setLaxMode() {
        this.mode = HttpMultipartMode.BROWSER_COMPATIBLE;
        return this;
    }

    public MixedMultipartEntityBuilder setStrictMode() {
        this.mode = HttpMultipartMode.STRICT;
        return this;
    }

    public MixedMultipartEntityBuilder setBoundary(final String boundary) {
        this.boundary = boundary;
        return this;
    }

    public MixedMultipartEntityBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    MixedMultipartEntityBuilder addPart(final MixedBodyPart bodyPart) {
        if (bodyPart == null) {
            return this;
        }
        if (this.bodyParts == null) {
            this.bodyParts = new ArrayList<MixedBodyPart>();
        }
        this.bodyParts.add(bodyPart);
        return this;
    }

    MixedMultipartEntityBuilder addParts(final List<MixedBodyPart> bodyParts) {
        if (bodyParts == null) {
            return this;
        }
        if (this.bodyParts == null) {
            this.bodyParts = new ArrayList<MixedBodyPart>();
        }
        for (MixedBodyPart bodyPart: bodyParts) {
        	this.bodyParts.add(bodyPart);
        }
        return this;
    }
    
    public MixedMultipartEntityBuilder addPart(final String name, final ContentBody contentBody) {
        Args.notNull(name, "Name");
        Args.notNull(contentBody, "Content body");
        return addPart(new MixedBodyPart(name, contentBody));
    }

    public MixedMultipartEntityBuilder addParts(final String name, final List<ContentBody> contentBodies) {
        Args.notNull(name, "Name");
        Args.notNull(contentBodies, "Content body");
        List<MixedBodyPart> mixedBodyParts = new ArrayList<MixedBodyPart>();
        for (ContentBody contentBody : contentBodies) {
        	mixedBodyParts.add(new MixedBodyPart(name, contentBody));
        }
        return addParts(mixedBodyParts);
    }

    public MixedMultipartEntityBuilder addTextBody(final String name, 
            final String text, final ContentType contentType) {
        return addPart(name, new StringBody(text, contentType));
    }

    public MixedMultipartEntityBuilder addTextBody(final String name,
            final String text) {
        return addTextBody(name, text, ContentType.DEFAULT_TEXT);
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,
            final byte[] b, final ContentType contentType, final String filename) {
        return addPart(name, new ByteArrayBody(b, contentType, filename));
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,
            final byte[] b) {
        return addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,
            final File file, final ContentType contentType, final String filename) {
        return addPart(name, new FileBody(file, contentType, filename));
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,
            final File file) {
        return addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,
            final InputStream stream, final ContentType contentType,
            final String filename) {
        return addPart(name, new InputStreamBody(stream, contentType, filename));
    }

    public MixedMultipartEntityBuilder addBinaryBody(final String name,final InputStream stream) {
        return addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
    }

    private String generateContentType(
            final String boundary,
            final Charset charset) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("multipart/mixed; boundary=");
        buffer.append(boundary);
        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset.name());
        }
        return buffer.toString();
    }

    private String generateBoundary() {
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random();
        final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    MultipartMixedEntity buildEntity() {
        final String st = subType != null ? subType : DEFAULT_SUBTYPE;
        final Charset cs = charset;
        final String b = boundary != null ? boundary : generateBoundary();
        final List<MixedBodyPart> bps = bodyParts != null ? new ArrayList<MixedBodyPart>(bodyParts) :
                Collections.<MixedBodyPart>emptyList();
        final HttpMultipartMode m = mode != null ? mode : HttpMultipartMode.STRICT;
        final AbstractMixedMultipartForm form;
        switch (m) {
            case BROWSER_COMPATIBLE:
                form = new HttpBrowserCompatibleMixedMultipart(st, cs, b, bps);
                break;
            case RFC6532:
                form = new HttpRFC6532MixedMultipart(st, cs, b, bps);
                break;
            default:
                form = new HttpStrictMixedMultipart(st, cs, b, bps);
        }
        return new MultipartMixedEntity(form, generateContentType(b, cs), form.getTotalLength());
    }

    public HttpEntity build() {
        return buildEntity();
    }

}
