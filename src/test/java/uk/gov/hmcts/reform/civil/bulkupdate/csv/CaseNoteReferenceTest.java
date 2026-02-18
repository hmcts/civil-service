package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseNoteReferenceTest {

    @Test
    void shouldCreateCaseNoteReference() {
        CaseNoteReference caseNoteReference = caseNoteReference("1234567890123456", "note-id-123");

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseNoteReference.getCaseNoteElementId()).isEqualTo("note-id-123");
    }

    @Test
    void shouldHandleNoArgsConstructor() {
        CaseNoteReference caseNoteReference = new CaseNoteReference();
        caseNoteReference.setCaseReference("123");
        caseNoteReference.setCaseNoteElementId("note-id");

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("123");
        assertThat(caseNoteReference.getCaseNoteElementId()).isEqualTo("note-id");
    }

    @Test
    void shouldHandleAllArgsConstructor() {
        CaseNoteReference caseNoteReference = new CaseNoteReference("note-id");
        caseNoteReference.setCaseReference("123");

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("123");
        assertThat(caseNoteReference.getCaseNoteElementId()).isEqualTo("note-id");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        CaseNoteReference ref1 = caseNoteReference("123", "note-1");

        CaseNoteReference ref2 = caseNoteReference("123", "note-1");

        CaseNoteReference ref3 = caseNoteReference("123", "note-2");

        assertThat(ref1).isEqualTo(ref2).hasSameHashCodeAs(ref2).isNotEqualTo(ref3);
    }

    private CaseNoteReference caseNoteReference(String caseReference, String noteId) {
        CaseNoteReference reference = new CaseNoteReference();
        reference.setCaseReference(caseReference);
        reference.setCaseNoteElementId(noteId);
        return reference;
    }
}
