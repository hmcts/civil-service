package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

public interface DocumentTypeBuilder<T> {

    T buildElementTypeWithDocumentCopy(T fromValue, String categoryId);
}
