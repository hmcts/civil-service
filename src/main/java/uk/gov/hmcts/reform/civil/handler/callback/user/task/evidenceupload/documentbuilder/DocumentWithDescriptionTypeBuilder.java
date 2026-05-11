package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;

@Component
public class DocumentWithDescriptionTypeBuilder implements DocumentTypeBuilder<DocumentWithDescription> {

    @Override
    public DocumentWithDescription buildElementTypeWithDocumentCopy(DocumentWithDescription fromValue, String categoryId) {
        if (fromValue == null) {
            return null;
        }

        Document copiedDocument = null;
        if (fromValue.getDocument() != null) {
            copiedDocument = new Document();
            copiedDocument.setCategoryID(categoryId)
                .setDocumentBinaryUrl(fromValue.getDocument().getDocumentBinaryUrl())
                .setDocumentFileName(fromValue.getDocument().getDocumentFileName())
                .setDocumentHash(fromValue.getDocument().getDocumentHash())
                .setDocumentUrl(fromValue.getDocument().getDocumentUrl());
        }

        return new DocumentWithDescription(
            copiedDocument,
            fromValue.getDocumentDescription(),
            fromValue.getCreatedDateTime() != null ? fromValue.getCreatedDateTime() : LocalDateTime.now(),
            fromValue.getCreatedBy()
        );
    }
}

