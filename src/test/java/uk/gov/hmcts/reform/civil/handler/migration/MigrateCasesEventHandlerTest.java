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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
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
        when(externalTask.getVariable("caseIds")).thenReturn(List.of());
        when(externalTask.getVariable("scenario")).thenReturn(null);
        when(externalTask.getVariable("csvFileName")).thenReturn("test.csv");

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
        verify(asyncCaseMigrationService, times(1)).migrateCasesAsync(migrationTask, mockReferences, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldHandleTaskWithCaseIdsAndScenario() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("caseIds")).thenReturn(List.of("123", "456"));
        when(externalTask.getVariable("scenario")).thenReturn("SCENARIO_1");
        when(externalTask.getVariable("state")).thenReturn(null);

        MigrationTask<? extends CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn((Class) DashboardScenarioCaseReference.class);
        doReturn(Optional.of(migrationTask))
            .when(migrationTaskFactory)
            .getMigrationTask("testTask");

        ExternalTaskData result = handler.handleTask(externalTask);

        assertNotNull(result);
        verify(asyncCaseMigrationService, times(1))
            .migrateCasesAsync(eq(migrationTask), anyList(), isNull());
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

    @Test
    void shouldFallbackToCsvWhenCaseIdsEmpty() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("caseIds")).thenReturn(List.of());
        when(externalTask.getVariable("scenario")).thenReturn(null);
        when(externalTask.getVariable("state")).thenReturn(null);
        when(externalTask.getVariable("csvFileName")).thenReturn("test.csv");

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn(CaseReference.class);
        when(migrationTaskFactory.getMigrationTask("testTask")).thenReturn(Optional.of(migrationTask));

        List<CaseReference> mockReferences = List.of(new CaseReference("999"));
        when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, "test.csv")).thenReturn(mockReferences);

        handler.handleTask(externalTask);

        verify(asyncCaseMigrationService).migrateCasesAsync(migrationTask, mockReferences, null);
    }

    @Test
    void shouldReturnEmptyExternalTaskDataWhenNoCaseReferencesFound() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("caseIds")).thenReturn(List.of());
        when(externalTask.getVariable("scenario")).thenReturn(null); // stub scenario
        when(externalTask.getVariable("csvFileName")).thenReturn("empty.csv"); // stub csvFileName

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getType()).thenReturn(CaseReference.class);
        when(migrationTaskFactory.getMigrationTask("testTask")).thenReturn(Optional.of(migrationTask));

        // Use lenient for CSV loader to avoid strict stubbing errors
        lenient().when(caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, "empty.csv"))
            .thenReturn(List.of());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertNotNull(result);
        verify(asyncCaseMigrationService, times(0)).migrateCasesAsync(any(), anyList(), any());
    }
}
