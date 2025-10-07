package uk.gov.hmcts.reform.civil.handler.migration;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardScenarioCaseReference;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
        handler = new MigrateCasesEventHandler(
            caseReferenceCsvLoader,
            migrationTaskFactory,
            asyncCaseMigrationService,
            "DUMMY_KEY"
        );
    }

    @Test
    void shouldHandleTaskSuccessfullyWithCsv() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("csvFileName")).thenReturn("test.csv");
        when(externalTask.getVariable("caseIds")).thenReturn(null);
        when(externalTask.getVariable("scenario")).thenReturn(null);

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn(CaseReference.class);
        when(migrationTaskFactory.getMigrationTask("testTask")).thenReturn(Optional.of(migrationTask));

        List<CaseReference> mockReferences = List.of(
            new CaseReference("12345"),
            new CaseReference("67890")
        );
        when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, "test.csv")).thenReturn(mockReferences);

        ExternalTaskData result = handler.handleTask(externalTask);

        assertNotNull(result);
        verify(asyncCaseMigrationService, times(1)).migrateCasesAsync(migrationTask, mockReferences);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldHandleTaskWithCaseIdsAndScenario() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("caseIds")).thenReturn("123,456");
        when(externalTask.getVariable("scenario")).thenReturn("SCENARIO_1");

        MigrationTask<? extends CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn((Class) DashboardScenarioCaseReference.class);
        doReturn(Optional.of(migrationTask))
            .when(migrationTaskFactory)
            .getMigrationTask("testTask");

        ExternalTaskData result = handler.handleTask(externalTask);

        assertNotNull(result);
        verify(asyncCaseMigrationService, times(1))
            .migrateCasesAsync(eq(migrationTask), anyList());
    }

    @Test
    void shouldThrowExceptionWhenTaskNameMissing() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void shouldThrowExceptionWhenMigrationTaskNotFound() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("unknownTask");

        when(migrationTaskFactory.getMigrationTask("unknownTask")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void shouldReturnCaseReferencesFromEncryptedCsvFile() {
        String csvFileName = "encrypted.csv";
        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));

        try (MockedStatic<CaseMigrationEncryptionUtil> mockedStatic = Mockito.mockStatic(CaseMigrationEncryptionUtil.class)) {
            mockedStatic.when(() -> CaseMigrationEncryptionUtil.isFileEncrypted(csvFileName)).thenReturn(true);
            when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, csvFileName, "DUMMY_KEY")).thenReturn(mockReferences);

            List<CaseReference> result = handler.getCaseReferenceList(CaseReference.class, csvFileName);

            assertEquals(2, result.size());
            assertEquals("12345", result.get(0).getCaseReference());
            assertEquals("67890", result.get(1).getCaseReference());
        }
    }

    @Test
    void shouldReturnEmptyListWhenCsvFileNameIsNull() {
        List<CaseReference> result = handler.getCaseReferenceList(CaseReference.class, null);
        assertTrue(result.isEmpty());
    }
}
