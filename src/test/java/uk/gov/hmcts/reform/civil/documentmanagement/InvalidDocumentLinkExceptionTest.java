package uk.gov.hmcts.reform.civil.documentmanagement;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class InvalidDocumentLinkExceptionTest {

    private static final String CASE_ID = "1767636822302602";

    @Test
    void withCaseId_appendsCaseIdToShortHrefMessage() {
        InvalidDocumentLinkException original = new InvalidDocumentLinkException("documents/undefined");

        InvalidDocumentLinkException withCase = original.withCaseId(CASE_ID);

        assertThat(withCase.getMessage()).isEqualTo(
            "Invalid document link 'documents/undefined' for case " + CASE_ID
                + ": expected a path of at least "
                + SecuredDocumentManagementService.DOC_UUID_LENGTH
                + " characters ending in a document UUID."
        );
        assertThat(withCase.getCause()).isSameAs(original);
    }

    @Test
    void withCaseId_appendsCaseIdToMalformedMessage() {
        InvalidDocumentLinkException original =
            new InvalidDocumentLinkException("documents/zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz", new IllegalArgumentException());

        assertThat(original.withCaseId(CASE_ID).getMessage()).isEqualTo(
            "Invalid document link 'documents/zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz' for case "
                + CASE_ID + ": the trailing document id is not a valid UUID."
        );
    }

    @Test
    void withCaseId_appendsCaseIdToClientErrorMessage() {
        InvalidDocumentLinkException original =
            new InvalidDocumentLinkException("documents/abc", 400, new RuntimeException("cdam 400"));

        assertThat(original.withCaseId(CASE_ID).getMessage()).isEqualTo(
            "Document management rejected the request for 'documents/abc' (HTTP 400). for case " + CASE_ID
        );
    }

    @Test
    void withCaseId_returnsSameInstanceWhenCaseIdMissing() {
        InvalidDocumentLinkException original = new InvalidDocumentLinkException("documents/null");

        assertSame(original, original.withCaseId(null));
        assertSame(original, original.withCaseId(""));
        assertSame(original, original.withCaseId("   "));
        assertThat(original.getMessage()).doesNotContain(" for case ");
    }

    @Test
    void withCaseId_doesNotDuplicateSuffix() {
        InvalidDocumentLinkException original = new InvalidDocumentLinkException("documents/undefined")
            .withCaseId(CASE_ID);

        assertSame(original, original.withCaseId(CASE_ID));
    }
}
