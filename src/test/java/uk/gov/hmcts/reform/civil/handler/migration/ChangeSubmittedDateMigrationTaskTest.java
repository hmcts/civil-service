package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ChangeSubmittedDateMigrationTaskTest {

    private ChangeSubmittedDateMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new ChangeSubmittedDateMigrationTask();
    }

    @Test
    void shouldUpdateSubmittedDateSuccessfully() {
        // Arrange
        CaseData caseData = CaseData.builder().submittedDate(LocalDateTime.of(2024, 8, 6, 12, 0)).build();
        CaseReference caseReference = mock(CaseReference.class);

        // Act
        CaseData updatedCaseData = task.migrateCaseData(caseData, caseReference);

        // Assert
        assertNotNull(updatedCaseData.getSubmittedDate());
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        // Arrange
        CaseReference caseReference = mock(CaseReference.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(null, caseReference));
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
    }
}
