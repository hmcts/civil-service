package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentWithDescriptionTypeBuilderTest {

    private DocumentWithDescriptionTypeBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DocumentWithDescriptionTypeBuilder();
    }

    @Test
    void buildElementTypeWithDocumentCopyShouldReturnNullWhenFromValueNull() {
        assertThat(builder.buildElementTypeWithDocumentCopy(null, "cat")).isNull();
    }

    @Test
    void buildElementTypeWithDocumentCopyShouldCopyMetadataWhenDocumentNull() {
        LocalDateTime created = LocalDateTime.of(2023, 1, 2, 3, 4);
        DocumentWithDescription from = new DocumentWithDescription(null, "notes", created, "creator");

        DocumentWithDescription copy = builder.buildElementTypeWithDocumentCopy(from, "newCat");

        assertThat(copy.getDocument()).isNull();
        assertThat(copy.getDocumentDescription()).isEqualTo("notes");
        assertThat(copy.getCreatedDateTime()).isEqualTo(created);
        assertThat(copy.getCreatedBy()).isEqualTo("creator");
    }

    @Test
    void buildElementTypeWithDocumentCopyShouldCopyDocumentWithCategory() {
        Document doc = new Document()
            .setDocumentUrl("http://a")
            .setDocumentBinaryUrl("http://a/bin")
            .setDocumentFileName("f.pdf")
            .setDocumentHash("h");
        LocalDateTime created = LocalDateTime.of(2022, 5, 6, 7, 8);
        DocumentWithDescription from = new DocumentWithDescription(doc, "d", created, "by");

        DocumentWithDescription copy = builder.buildElementTypeWithDocumentCopy(from, "targetCategory");

        assertThat(copy.getDocument()).isNotSameAs(doc);
        assertThat(copy.getDocument().getCategoryID()).isEqualTo("targetCategory");
        assertThat(copy.getDocument().getDocumentUrl()).isEqualTo("http://a");
        assertThat(copy.getDocument().getDocumentFileName()).isEqualTo("f.pdf");
        assertThat(copy.getCreatedDateTime()).isEqualTo(created);
    }

    @Test
    void buildElementTypeWithDocumentCopyShouldUseNowWhenCreatedDateTimeNull() {
        Document doc = new Document().setDocumentFileName("x.pdf");
        DocumentWithDescription from = new DocumentWithDescription(doc, null, null, null);

        DocumentWithDescription copy = builder.buildElementTypeWithDocumentCopy(from, "c");

        assertThat(copy.getCreatedDateTime()).isNotNull();
    }
}
