package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBundleMigrationTaskTest {

    @Mock
    private BundleCreationService bundleCreationService;

    private CreateBundleMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new CreateBundleMigrationTask(bundleCreationService);
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertEquals("CreateBundleMigrationTask", task.getTaskName());
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertEquals(
            "Create Bundle for cases via migration task",
            task.getEventSummary()
        );
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertEquals(
            "This task creates the bundle for cases based on the provided case references.",
            task.getEventDescription()
        );
    }

    @Test
    void shouldCreateBundleAndReturnSameCaseData() {
        // Arrange
        CaseData caseData = CaseData.builder().build();

        CaseReference caseReference = mock(CaseReference.class);
        when(caseReference.getCaseReference()).thenReturn("1234567890123456");

        CaseData result = task.migrateCaseData(caseData, caseReference);

        verify(bundleCreationService)
            .createBundle(1234567890123456L);

        assertSame(caseData, result);
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        CaseReference caseReference = mock(CaseReference.class);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(null, caseReference)
        );

        assertEquals(
            "CaseData and CaseReference must not be null",
            exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertEquals(
            "CaseData and CaseReference must not be null",
            exception.getMessage()
        );
    }
}
