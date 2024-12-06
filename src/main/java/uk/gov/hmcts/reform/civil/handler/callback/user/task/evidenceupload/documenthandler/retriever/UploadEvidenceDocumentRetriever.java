package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

@Component
public class UploadEvidenceDocumentRetriever implements UploadDocumentRetriever<UploadEvidenceDocumentType> {

    public Document getDocument(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getDocumentUpload();
    }

    public LocalDateTime getDocumentDateTime(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getCreatedDatetime();
    }
}
