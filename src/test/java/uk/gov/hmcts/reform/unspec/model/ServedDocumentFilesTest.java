package uk.gov.hmcts.reform.unspec.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

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

            assertThat(servedDocumentFiles.getErrors()).containsOnly("One particular of claim is required");
        }

        @Test
        void shouldReturnMoreThanOneError_WhenBothParticularsOfClaimFieldsAreNotNull() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimDocument(Document.builder().build())
                .particularsOfClaimText("Some string")
                .build();

            assertThat(servedDocumentFiles.getErrors()).containsOnly("More than one particular of claim added");
        }
    }
}
