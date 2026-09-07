package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateSystemTTLTaskTest {

    private UpdateSystemTTLTask task;

    @BeforeEach
    void setUp() {
        task = new UpdateSystemTTLTask();
    }

    @Test
    void shouldUseExcelCaseReferenceType() {
        assertEquals(ExcelCaseReference.class, task.getType());
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertEquals("UpdateSystemTTLTask", task.getTaskName());
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertEquals("Update case system TTL via migration task", task.getEventSummary());
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertEquals("This task updates system TTL on the case", task.getEventDescription());
    }

    @Test
    void migrateCaseDataShouldReturnSameCaseData() {
        CaseData caseData = CaseData.builder().build();
        ExcelCaseReference caseReference = excelCaseReference("1234567890123456");

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertSame(caseData, result);
    }

    @Test
    void migrateCaseDataShouldThrowWhenCaseDataIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(null, excelCaseReference("1234567890123456"))
        );

        assertEquals("CaseData and CaseReference fields must not be null", exception.getMessage());
    }

    @Test
    void migrateCaseDataShouldThrowWhenCaseReferenceIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(CaseData.builder().build(), null)
        );

        assertEquals("CaseData and CaseReference fields must not be null", exception.getMessage());
    }

    @Test
    void migrateCaseDataShouldThrowWhenCaseReferenceValueIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(CaseData.builder().build(), new ExcelCaseReference())
        );

        assertEquals("CaseData and CaseReference fields must not be null", exception.getMessage());
    }

    private ExcelCaseReference excelCaseReference(String caseReference) {
        ExcelCaseReference reference = new ExcelCaseReference();
        reference.setCaseReference(caseReference);
        return reference;
    }
}
