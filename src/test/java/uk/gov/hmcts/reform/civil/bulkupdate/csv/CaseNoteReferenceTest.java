package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseNoteReferenceTest {

    @Test
    void shouldCreateCaseNoteReference() {
        CaseNoteReference caseNoteReference = CaseNoteReference.builder()
            .caseReference("1234567890123456")
            .caseNoteItemId("note-id-123")
            .build();

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseNoteReference.getCaseNoteItemId()).isEqualTo("note-id-123");
    }

    @Test
    void shouldHandleNoArgsConstructor() {
        CaseNoteReference caseNoteReference = new CaseNoteReference();
        caseNoteReference.setCaseReference("123");
        caseNoteReference.setCaseNoteItemId("note-id");

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("123");
        assertThat(caseNoteReference.getCaseNoteItemId()).isEqualTo("note-id");
    }

    @Test
    void shouldHandleAllArgsConstructor() {
        CaseNoteReference caseNoteReference = new CaseNoteReference("note-id");
        caseNoteReference.setCaseReference("123");

        assertThat(caseNoteReference.getCaseReference()).isEqualTo("123");
        assertThat(caseNoteReference.getCaseNoteItemId()).isEqualTo("note-id");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        CaseNoteReference ref1 = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteItemId("note-1")
            .build();

        CaseNoteReference ref2 = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteItemId("note-1")
            .build();

        CaseNoteReference ref3 = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteItemId("note-2")
            .build();

        assertThat(ref1).isEqualTo(ref2).hasSameHashCodeAs(ref2).isNotEqualTo(ref3);
    }
}
