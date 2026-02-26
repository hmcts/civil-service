package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class ServedDocumentFilesTest {

    @Nested
    class GetErrors {

        @Test
        void shouldReturnEmptyList_WhenOnlyDocument() {
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles()
                .setParticularsOfClaimDocument(wrapElements(new Document()))
                ;

            assertThat(servedDocumentFiles.getErrors()).isEmpty();
            assertThat(servedDocumentFiles.getErrorsAddOrAmendDocuments()).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_WhenOnlyText() {
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles()
                .setParticularsOfClaimText("Some string")
                ;

            assertThat(servedDocumentFiles.getErrors()).isEmpty();
            assertThat(servedDocumentFiles.getErrorsAddOrAmendDocuments()).isEmpty();
        }

        @Test
        void shouldReturnRequiredError_WhenBothParticularsOfClaimFieldsAreNull() {
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles();

            assertThat(servedDocumentFiles.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnMoreThanOneError_WhenBothParticularsOfClaimFieldsAreNotNull() {
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles()
                .setParticularsOfClaimDocument(wrapElements(new Document()))
                .setParticularsOfClaimText("Some string")
                ;

            assertThat(servedDocumentFiles.getErrors())
                .containsOnly("You need to either upload 1 Particulars of claim only or enter the Particulars "
                                  + "of claim text in the field provided. You cannot do both.");
            assertThat(servedDocumentFiles.getErrorsAddOrAmendDocuments())
                .containsOnly("You need to either upload 1 Particulars of claim only or enter the Particulars "
                                  + "of claim text in the field provided. You cannot do both.");
        }
    }
}
