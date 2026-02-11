package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseNoteReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RemoveCaseNoteTaskTest {

    private RemoveCaseNoteTask task;

    @BeforeEach
    void setUp() {
        task = new RemoveCaseNoteTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("RemoveCaseNoteTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary()).isEqualTo("Remove case note via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription()).isEqualTo("This task removes case note on the case");
    }

    @Test
    void shouldRemoveCaseNoteWhenIdMatches() {
        UUID noteId = UUID.randomUUID();
        CaseNote note = new CaseNote();
        note.setNote("Note to remove");
        List<Element<CaseNote>> caseNotes = new ArrayList<>();
        Element<CaseNote> element = element(note);
        element.setId(noteId);
        caseNotes.add(element);

        CaseData caseData = CaseData.builder()
            .caseNotes(caseNotes)
            .build();

        CaseNoteReference caseNoteReference = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteElementId(noteId.toString())
            .build();

        CaseData result = task.migrateCaseData(caseData, caseNoteReference);

        assertThat(result.getCaseNotes()).isEmpty();
    }

    @Test
    void shouldRemoveCaseNoteWhenIdMatchesAndLeaveOtherNotes() {
        UUID noteId = UUID.randomUUID();
        CaseNote note = new CaseNote();
        note.setNote("Note to remove");
        List<Element<CaseNote>> caseNotes = new ArrayList<>();
        Element<CaseNote> element = element(note);
        element.setId(noteId);
        caseNotes.add(element);
        CaseNote note2 = new CaseNote();
        note2.setNote("Note to retain");
        Element<CaseNote> element2 = element(note2);
        element2.setId(UUID.randomUUID());
        caseNotes.add(element2);

        CaseData caseData = CaseData.builder()
            .caseNotes(caseNotes)
            .build();

        CaseNoteReference caseNoteReference = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteElementId(noteId.toString())
            .build();

        CaseData result = task.migrateCaseData(caseData, caseNoteReference);

        assertThat(result.getCaseNotes()).hasSize(1);
        assertThat(result.getCaseNotes().get(0).getId()).isNotEqualTo(noteId);
    }

    @Test
    void shouldNotRemoveCaseNoteWhenIdDoesNotMatch() {
        UUID noteId = UUID.randomUUID();
        CaseNote note = new CaseNote();
        note.setNote("Note to keep");
        List<Element<CaseNote>> caseNotes = new ArrayList<>();
        Element<CaseNote> element = element(note);
        element.setId(noteId);
        caseNotes.add(element);

        CaseData caseData = CaseData.builder()
            .caseNotes(caseNotes)
            .build();

        CaseNoteReference caseNoteReference = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteElementId(UUID.randomUUID().toString())
            .build();

        CaseData result = task.migrateCaseData(caseData, caseNoteReference);

        assertThat(result.getCaseNotes()).hasSize(1);
        assertThat(result.getCaseNotes().get(0).getId()).isEqualTo(noteId);
    }

    @Test
    void shouldHandleNullCaseNotes() {
        CaseData caseData = CaseData.builder().build();
        CaseNoteReference caseNoteReference = CaseNoteReference.builder()
            .caseReference("123")
            .caseNoteElementId(UUID.randomUUID().toString())
            .build();

        CaseData result = task.migrateCaseData(caseData, caseNoteReference);

        assertThat(result.getCaseNotes()).isNull();
    }
}
