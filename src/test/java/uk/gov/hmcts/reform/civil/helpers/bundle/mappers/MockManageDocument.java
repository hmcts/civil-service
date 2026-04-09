package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.UUID;

public class MockManageDocument {

    protected MockManageDocument() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    public static @NotNull Element<ManageDocument> getManageDocumentElement(ManageDocumentType manageDocumentType,
                                                                            DocumentCategory docCategory) {
        return new Element<>(
            UUID.randomUUID(),
            new ManageDocument()
                .setDocumentType(manageDocumentType)
                .setDocumentName("test_file")
                .setDocumentLink(new Document().setDocumentFileName("test_file.pdf")
                                  .setDocumentUrl("http://localhost:9090/documents")
                                  .setDocumentBinaryUrl("http://localhost:9090/documents/binary")
                                  .setCategoryID(docCategory.getCategoryId())
                                  .setUploadTimestamp("2025-12-10T12:39:50.823836740"))
        );
    }

    public static @NotNull Element<ManageDocument> getManageDocumentElement(ManageDocumentType manageDocumentType,
                                                                            DocCategory docCategory) {
        return new Element<>(
            UUID.randomUUID(),
            new ManageDocument()
                .setDocumentType(manageDocumentType)
                .setDocumentName("test_file")
                .setDocumentLink(new Document().setDocumentFileName("test_file.pdf")
                                  .setDocumentUrl("http://localhost:9090/documents")
                                  .setDocumentBinaryUrl("http://localhost:9090/documents/binary")
                                  .setCategoryID(docCategory.getValue())
                                  .setUploadTimestamp("2025-12-10T12:39:50.823836740"))
        );
    }
}
