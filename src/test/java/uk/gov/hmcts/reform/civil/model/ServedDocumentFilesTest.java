package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import static org.assertj.core.api.Assertions.assertThat;

class ServedDocumentFilesTest {

    @Nested
    class GetErrors {

        @Test
        void shouldReturnEmptyList_WhenOnlyDocument() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimDocument(Document.builder().build())
                .build();

            assertThat(servedDocumentFiles.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_WhenOnlyText() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimText("Some string")
                .build();

            assertThat(servedDocumentFiles.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnRequiredError_WhenBothParticularsOfClaimFieldsAreNull() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder().build();

            assertThat(servedDocumentFiles.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnMoreThanOneError_WhenBothParticularsOfClaimFieldsAreNotNull() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimDocument(Document.builder().build())
                .particularsOfClaimText("Some string")
                .build();

            assertThat(servedDocumentFiles.getErrors())
                .containsOnly("More than one Particulars of claim details added");
        }
    }
}
