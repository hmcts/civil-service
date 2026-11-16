package uk.gov.hmcts.reform.civil.documentmanagement.model;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

/**
 * A document body that is read from the upstream connection on demand rather than held in memory.
 * It can be read once; reading it to the end, or closing the stream, releases the connection.
 */
public class StreamingDocumentResource extends InputStreamResource {

    private final long contentLength;

    public StreamingDocumentResource(InputStream inputStream, long contentLength) {
        super(inputStream);
        this.contentLength = contentLength;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }
}
