package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardScenarioCaseReference;
import uk.gov.hmcts.reform.civil.handler.event.DashboardScenarioProcessor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RetriggerDashboardScenarioTaskTest {

    private DashboardScenarioProcessor processor;
    private RetriggerDashboardScenarioTask task;

    @BeforeEach
    void setUp() {
        processor = mock(DashboardScenarioProcessor.class);
        task = new RetriggerDashboardScenarioTask(processor);
    }

    @Test
    void migrateCaseData_shouldCallProcessorWithCorrectCaseReference() {
        // Arrange
        CaseData caseData = CaseData.builder().build();
        DashboardScenarioCaseReference caseReference = new DashboardScenarioCaseReference();
        caseReference.setCaseReference("12345");

        // Act
        CaseData result = task.migrateCaseData(caseData, caseReference);

        // Assert
        verify(processor, times(1)).createDashboardScenario("12345", "Scenario.AAA6.ClaimantIntent.FullAdmit.Claimant");
        assertEquals(caseData, result, "migrateCaseData should return the same CaseData");
    }

    @Test
    void migrateCaseData_shouldThrowException_whenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();
        DashboardScenarioCaseReference caseReference = null;

        Exception exception = assertThrows(IllegalArgumentException.class,
                                           () -> task.migrateCaseData(caseData, caseReference));

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
    }

    @Test
    void migrateCaseData_shouldThrowException_whenCaseReferenceValueIsNull() {
        CaseData caseData = CaseData.builder().build();
        DashboardScenarioCaseReference caseReference = new DashboardScenarioCaseReference();
        caseReference.setCaseReference(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                                           () -> task.migrateCaseData(caseData, caseReference));

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
    }

    @Test
    void migrateCaseData_shouldThrowException_whenCaseReferenceIsNotNumeric() {
        CaseData caseData = CaseData.builder().build();
        DashboardScenarioCaseReference caseReference = new DashboardScenarioCaseReference();
        caseReference.setCaseReference("abc");

        assertThrows(NumberFormatException.class,
                     () -> task.migrateCaseData(caseData, caseReference));
    }
}
