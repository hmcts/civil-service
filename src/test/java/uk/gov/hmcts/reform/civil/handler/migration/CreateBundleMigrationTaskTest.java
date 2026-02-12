package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateBundleMigrationTaskTest {

    @Mock
    private BundleCreationService bundleCreationService;

    @InjectMocks
    private CreateBundleMigrationTask task;

    private CaseData caseData;
    private ExcelCaseReference caseReference;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        caseReference = ExcelCaseReference.builder()
            .caseReference("1234567890123456")
            .build();
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(null, caseReference)
        );

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
        verifyNoInteractions(bundleCreationService);
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
        verifyNoInteractions(bundleCreationService);
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
    void shouldReturnCorrectTaskName() {
        assertEquals(
            "CreateBundleMigrationTask",
            task.getTaskName()
        );
    }

    @Test
    void shouldCreateBundleAndReturnSameCaseData() {
        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertSame(caseData, result);
        assertEquals("BUNDLE_CREATED_NOTIFICATION", result.getBundleEvent()); // 👈 add this
        verify(bundleCreationService).createBundle(1234567890123456L);
    }

    @Test
    void shouldThrowNumberFormatExceptionForInvalidCaseReference() {
        ExcelCaseReference badRef = ExcelCaseReference.builder()
            .caseReference("NOT_A_NUMBER")
            .build();

        assertThrows(NumberFormatException.class,
                     () -> task.migrateCaseData(caseData, badRef)
        );

        verifyNoInteractions(bundleCreationService);
    }
}
