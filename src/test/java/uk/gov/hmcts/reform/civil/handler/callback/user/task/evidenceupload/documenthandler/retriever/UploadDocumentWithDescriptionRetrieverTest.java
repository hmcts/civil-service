package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class UploadDocumentWithDescriptionRetrieverTest {

    private UploadDocumentWithDescriptionRetriever retriever;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        retriever = new UploadDocumentWithDescriptionRetriever();
        created = LocalDateTime.of(2024, 3, 4, 10, 30);
    }

    @Test
    void getDocumentShouldReturnNestedDocument() {
        Document document = new Document().setDocumentFileName("a.pdf");
        Element<DocumentWithDescription> el = element(new DocumentWithDescription(document, "x", created, "u"));

        assertThat(retriever.getDocument(el)).isSameAs(document);
    }

    @Test
    void getDocumentDateTimeShouldReturnCreatedDateTime() {
        Document document = new Document().setDocumentFileName("a.pdf");
        Element<DocumentWithDescription> el = element(new DocumentWithDescription(document, "x", created, "u"));

        assertThat(retriever.getDocumentDateTime(el)).isEqualTo(created);
    }
}
