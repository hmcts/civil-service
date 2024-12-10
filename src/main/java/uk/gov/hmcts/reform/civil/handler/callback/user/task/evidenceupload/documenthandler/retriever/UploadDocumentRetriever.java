package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

public interface UploadDocumentRetriever<T> {

    Document getDocument(Element<T> element);

    LocalDateTime getDocumentDateTime(Element<T> element);

}
