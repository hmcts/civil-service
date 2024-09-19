package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;

public interface DocumentTypeBuilder<T> {
    T buildElementTypeWithDocumentCopy(T fromValue, String categoryId);
}
