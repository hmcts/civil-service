package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RemoveCaseDismissedHearingDueDateMigrationTaskTest {

    private RemoveCaseDismissedHearingDueDateMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new RemoveCaseDismissedHearingDueDateMigrationTask();
    }

    @Test
    void shouldKeepCaseDataAndUseExplicitNullificationForCaseDismissedHearingFeeDueDate() {
        LocalDateTime hearingFeeDueDate = LocalDateTime.of(2024, 8, 6, 12, 0);
        CaseData caseData = CaseData.builder()
            .caseDismissedHearingFeeDueDate(hearingFeeDueDate)
            .build();
        CaseReference caseReference = mock(CaseReference.class);

        CaseData updatedCaseData = task.migrateCaseData(caseData, caseReference);

        assertEquals(hearingFeeDueDate, updatedCaseData.getCaseDismissedHearingFeeDueDate());
        assertEquals(List.of("caseDismissedHearingFeeDueDate"), task.getFieldsToNullify());
    }

    @Test
    void shouldDeclareCaseDismissedHearingFeeDueDateForExplicitNullification() {
        assertEquals(List.of("caseDismissedHearingFeeDueDate"), task.getFieldsToNullify());
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        CaseReference caseReference = mock(CaseReference.class);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(null, caseReference)
        );

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = mock(CaseData.class);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertEquals(
            "RemoveCaseDismissedHearingDueDateMigrationTask",
            task.getTaskName()
        );
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertEquals(
            "Remove hearing dismissed due date Migration",
            task.getEventSummary()
        );
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertEquals(
            "This task removes the hearing dismissed due date for cases based on the provided case references.",
            task.getEventDescription()
        );
    }
}
