package uk.gov.hmcts.reform.civil.handler.migration;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateCasesEventHandlerTest {

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @Mock
    private MigrationTaskFactory migrationTaskFactory;
    @Mock
    private AsyncCaseMigrationService asyncCaseMigrationService;

    private MigrateCasesEventHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new MigrateCasesEventHandler(
            caseReferenceCsvLoader,
            migrationTaskFactory,
            asyncCaseMigrationService,
            "DUMMY_KEY"
        );
    }

    @Test
    void shouldHandleTaskSuccessfully() {
        // Arrange
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        String csvFileName = "test.csv";
        when(externalTask.getVariable("csvFileName")).thenReturn(csvFileName);

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn(CaseReference.class);

        when(migrationTaskFactory.getMigrationTask("testTask")).thenReturn(Optional.of(migrationTask));

        CaseData caseData = mock(CaseData.class);

        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));
        when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, csvFileName)).thenReturn(mockReferences);

        ExternalTaskData result = handler.handleTask(externalTask);

        // Assert
        assertNotNull(result);
        verify(asyncCaseMigrationService, times(1)).migrateCasesAsync(migrationTask, mockReferences);
    }

    @Test
    void shouldReturnCaseReferencesFromCsvFile() {
        // Arrange
        String caseIds = null;
        String csvFileName = "test.csv";
        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));
        when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, csvFileName)).thenReturn(mockReferences);

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(CaseReference.class, csvFileName);

        // Assert
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getCaseReference());
        assertEquals("67890", result.get(1).getCaseReference());
    }

    @Test
    void shouldReturnCaseReferencesFromEncryptedCsvFile() {
        // Arrange
        String caseIds = null;
        String csvFileName = "encrypted.csv";
        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));

        MockedStatic<CaseMigrationEncryptionUtil> caseMigrationEncryptionUtilMockedStatic = Mockito.mockStatic(CaseMigrationEncryptionUtil.class);
        caseMigrationEncryptionUtilMockedStatic.when(() -> CaseMigrationEncryptionUtil.isFileEncrypted(csvFileName)).thenReturn(true);

        when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, csvFileName, "DUMMY_KEY")).thenReturn(mockReferences);

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(CaseReference.class, csvFileName);

        // Assert
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getCaseReference());
        assertEquals("67890", result.get(1).getCaseReference());

        caseMigrationEncryptionUtilMockedStatic.close();
    }

    @Test
    void shouldReturnEmptyListWhenCaseIdsAndCsvFileNameAreNull() {
        // Arrange
        String csvFileName = null;

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(CaseReference.class, csvFileName);

        // Assert
        assertEquals(0, result.size());
    }

}
