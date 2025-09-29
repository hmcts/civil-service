package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncCaseMigrationServiceTest {

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    private ObjectMapper objectMapper = new ObjectMapper();

    private AsyncCaseMigrationService asyncCaseMigrationService;

    @BeforeEach
    void setUp() {
        caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        MockitoAnnotations.openMocks(this);
        asyncCaseMigrationService = new AsyncCaseMigrationService(
            coreCaseDataService,
            caseDetailsConverter,
            objectMapper,
            500, // migrationBatchSize
            10 // migrationWaitTime
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
        when(migrationTask.getEventDescription()).thenReturn("Test Migration Task");
        when(migrationTask.getEventSummary()).thenReturn("Migrating cases for test task");

        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        when(coreCaseDataService.startUpdate(anyString(), eq(CaseEvent.UPDATE_CASE_DATA))).thenReturn(startEventResponse);

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = mock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.class);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        CaseData caseData = mock(CaseData.class);

        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);
        when(migrationTask.migrateCaseData(any(CaseData.class), any(CaseReference.class))).thenReturn(caseData);

        asyncCaseMigrationService.migrateCasesAsync(migrationTask, mockReferences);

        // Assert
        verify(coreCaseDataService, times(2)).startUpdate(anyString(), eq(CaseEvent.UPDATE_CASE_DATA));
        verify(coreCaseDataService, times(2)).submitUpdate(anyString(), any(CaseDataContent.class));
    }

    @Test
    void shouldBuildCaseDataContentSuccessfully() {
        // Arrange
        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        when(startEventResponse.getToken()).thenReturn("testToken");
        when(startEventResponse.getEventId()).thenReturn("testEventId");

        CaseData caseData = mock(CaseData.class);
        Map<String, Object> mockData = Map.of("key1", "value1", "key2", "value2");
        when(caseData.toMap(any())).thenReturn(mockData);

        MigrationTask migrationTask = mock(MigrationTask.class);
        when(migrationTask.getEventDescription()).thenReturn("Test Migration Task");
        when(migrationTask.getEventSummary()).thenReturn("Migrating cases for test task");

        // Act
        CaseDataContent result = asyncCaseMigrationService.buildCaseDataContent(startEventResponse, caseData, migrationTask);

        // Assert
        assertEquals("testToken", result.getEventToken());
        assertEquals("testEventId", result.getEvent().getId());
        assertEquals(mockData, result.getData());

        assertEquals("Test Migration Task", result.getEvent().getDescription());
        assertEquals("Migrating cases for test task", result.getEvent().getSummary());
    }

}
