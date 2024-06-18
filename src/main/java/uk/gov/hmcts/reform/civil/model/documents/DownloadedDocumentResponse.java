package uk.gov.hmcts.reform.civil.model.documents;

import org.springframework.core.io.Resource;

public record DownloadedDocumentResponse(Resource file, String fileName, String mimeType) {
}
