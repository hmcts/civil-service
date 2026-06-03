package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;

@Component
public class UploadDocumentWithDescriptionRetriever implements UploadDocumentRetriever<DocumentWithDescription> {

    @Override
    public Document getDocument(Element<DocumentWithDescription> element) {
        return element.getValue().getDocument();
    }

    @Override
    public LocalDateTime getDocumentDateTime(Element<DocumentWithDescription> element) {
        return element.getValue().getCreatedDatetime();
    }
}

