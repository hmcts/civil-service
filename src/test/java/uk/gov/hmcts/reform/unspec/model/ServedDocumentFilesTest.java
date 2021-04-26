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

            assertThat(servedDocumentFiles.getErrors("CREATE_CLAIM")).isEmpty();
            assertThat(servedDocumentFiles.getErrors("NOTIFY_DEFENDANT_OF_CLAIM_DETAILS")).isEmpty();
            assertThat(servedDocumentFiles.getErrors("ADD_OR_AMEND_CLAIM_DOCUMENTS")).isEmpty();

        }

        @Test
        void shouldReturnEmptyList_WhenOnlyText() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimText("Some string")
                .build();

            assertThat(servedDocumentFiles.getErrors("CREATE_CLAIM")).isEmpty();
            assertThat(servedDocumentFiles.getErrors("NOTIFY_DEFENDANT_OF_CLAIM_DETAILS")).isEmpty();
            assertThat(servedDocumentFiles.getErrors("ADD_OR_AMEND_CLAIM_DOCUMENTS")).isEmpty();
        }

        @Test
        void shouldReturnRequiredError_WhenBothParticularsOfClaimFieldsAreNull() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder().build();

            assertThat(servedDocumentFiles.getErrors("CREATE_CLAIM"))
                .containsOnly("You must add Particulars of claim details");
            assertThat(servedDocumentFiles.getErrors("NOTIFY_DEFENDANT_OF_CLAIM_DETAILS"))
                .containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnMoreThanOneError_WhenBothParticularsOfClaimFieldsAreNotNull() {
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimDocument(Document.builder().build())
                .particularsOfClaimText("Some string")
                .build();

            assertThat(servedDocumentFiles.getErrors("CREATE_CLAIM"))
                .containsOnly("More than one Particulars of claim details added");
            assertThat(servedDocumentFiles.getErrors("NOTIFY_DEFENDANT_OF_CLAIM_DETAILS"))
                .containsOnly("More than one Particulars of claim details added");
            assertThat(servedDocumentFiles.getErrors("ADD_OR_AMEND_CLAIM_DOCUMENTS"))
                .containsOnly("More than one Particulars of claim details added");
        }
    }
}
