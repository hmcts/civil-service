package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DismissedDeadlineCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangeDismissedDeadlineDateMigrationTaskTest {

    private ChangeDismissedDeadlineDateMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new ChangeDismissedDeadlineDateMigrationTask();
    }

    @Test
    void shouldUpdateClaimDismissedDeadlineFromCaseReference() {
        CaseData caseData = CaseData.builder()
            .claimDismissedDeadline(LocalDateTime.of(2024, 8, 6, 12, 0))
            .build();
        DismissedDeadlineCaseReference caseReference = new DismissedDeadlineCaseReference()
            .setDismissedDeadline("2026-07-17T10:15:30");
        caseReference.setCaseReference("1234567890123456");

        CaseData updatedCaseData = task.migrateCaseData(caseData, caseReference);

        assertThat(updatedCaseData).isSameAs(caseData);
        assertThat(updatedCaseData.getClaimDismissedDeadline())
            .isEqualTo(LocalDateTime.of(2026, 7, 17, 10, 15, 30));
    }

    @Test
    void shouldLeaveClaimDismissedDeadlineUnchangedWhenDismissedDeadlineIsMissing() {
        LocalDateTime existingDeadline = LocalDateTime.of(2024, 8, 6, 12, 0);
        CaseData caseData = CaseData.builder()
            .claimDismissedDeadline(existingDeadline)
            .build();
        DismissedDeadlineCaseReference caseReference = new DismissedDeadlineCaseReference();
        caseReference.setCaseReference("1234567890123456");

        CaseData updatedCaseData = task.migrateCaseData(caseData, caseReference);

        assertThat(updatedCaseData).isSameAs(caseData);
        assertThat(updatedCaseData.getClaimDismissedDeadline()).isEqualTo(existingDeadline);
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        DismissedDeadlineCaseReference caseReference = new DismissedDeadlineCaseReference();
        caseReference.setCaseReference("1234567890123456");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(null, caseReference)
        );

        assertThat(exception).hasMessage("CaseData must not be null");
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertThat(exception).hasMessage("CaseReference fields must not be null");
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceValueIsNull() {
        CaseData caseData = CaseData.builder().build();
        DismissedDeadlineCaseReference caseReference = new DismissedDeadlineCaseReference();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, caseReference)
        );

        assertThat(exception).hasMessage("CaseReference fields must not be null");
    }

    @Test
    void shouldReturnCorrectTaskMetadata() {
        assertThat(task.getTaskName()).isEqualTo("ChangeDismissedDeadlineDateMigrationTask");
        assertThat(task.getEventSummary()).isEqualTo("Change Dismissed Deadline Date Migration");
        assertThat(task.getEventDescription())
            .isEqualTo("This task changes the dismissed deadline date for cases based on the provided case references.");
        assertThat(task.getType()).isEqualTo(DismissedDeadlineCaseReference.class);
    }
}
