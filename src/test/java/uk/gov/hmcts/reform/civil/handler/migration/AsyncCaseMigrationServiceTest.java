package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CaseEvent.UPDATE_CASE_DATA))).thenReturn(startEventResponse);

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = mock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.class);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        CaseData caseData = mock(CaseData.class);

        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);
        when(migrationTask.migrateCaseData(ArgumentMatchers.any(CaseData.class), ArgumentMatchers.any(CaseReference.class))).thenReturn(caseData);

        asyncCaseMigrationService.migrateCasesAsync(migrationTask, mockReferences, null);

        // Assert
        verify(coreCaseDataService, times(2)).startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CaseEvent.UPDATE_CASE_DATA));
        verify(coreCaseDataService, times(2)).submitUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(CaseDataContent.class));
    }

    @Test
    void shouldBuildCaseDataContentSuccessfully() {
        // Arrange
        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        when(startEventResponse.getToken()).thenReturn("testToken");
        when(startEventResponse.getEventId()).thenReturn("testEventId");

        CaseData caseData = mock(CaseData.class);
        Map<String, Object> mockData = Map.of("key1", "value1", "key2", "value2");
        when(caseData.toMap(ArgumentMatchers.any())).thenReturn(mockData);

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
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

    @Test
    void shouldUpdateCaseStateWhenUpdatedStatePresent() {
        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        CaseData caseData = mock(CaseData.class);
        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = mock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.class);

        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CaseEvent.UPDATE_CASE_DATA))).thenReturn(startEventResponse);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        CaseReference caseReference = new CaseReference("12345");
        when(migrationTask.migrateCaseData(caseData, caseReference)).thenReturn(caseData);
        when(migrationTask.getUpdatedState(ArgumentMatchers.any())).thenReturn(Optional.of("NEW_STATE"));
        when(migrationTask.getEventSummary()).thenReturn("summary");
        when(migrationTask.getEventDescription()).thenReturn("description");

        // Act
        List<CaseReference> caseReferences = List.of(caseReference);
        asyncCaseMigrationService.migrateCasesAsync(migrationTask, caseReferences, "OLD_STATE");

        // Assert
        verify(coreCaseDataService).startUpdate(ArgumentMatchers.eq("12345"), ArgumentMatchers.any(CaseEvent.class));
    }

    @Test
    void shouldHandleRuntimeExceptionDuringMigration() {
        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CaseEvent.UPDATE_CASE_DATA)))
            .thenThrow(new RuntimeException("Test Exception"));

        CaseReference caseReference = new CaseReference("12345");
        List<CaseReference> caseReferences = List.of(caseReference);
        asyncCaseMigrationService.migrateCasesAsync(migrationTask, caseReferences, null);

        // Assert: verify no submission attempted
        verify(coreCaseDataService, times(0)).submitUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(CaseDataContent.class));
    }

    @Test
    void shouldResetRequestContextHolderAfterEachCase() {
        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(mock(StartEventResponse.class));

        asyncCaseMigrationService.migrateCasesAsync(migrationTask, List.of(new CaseReference("12345")), null);

        assertNull(RequestContextHolder.getRequestAttributes(), "RequestContext should be reset after migration");
    }

    @Test
    void shouldPauseBetweenBatchesWhenBatchSizeReached() {
        AsyncCaseMigrationService batchService = new AsyncCaseMigrationService(
            coreCaseDataService,
            caseDetailsConverter,
            objectMapper,
            1, // batch size = 1
            0  // avoid real sleep
        );

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.migrateCaseData(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mock(CaseData.class));
        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(mock(StartEventResponse.class));

        List<CaseReference> caseReferences = List.of(new CaseReference("1"), new CaseReference("2"));
        batchService.migrateCasesAsync(migrationTask, caseReferences, null);

        verify(coreCaseDataService, times(2)).startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CaseEvent.UPDATE_CASE_DATA));
    }

    @Test
    void shouldNotChangeStateWhenUpdatedStateNotPresent() {
        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId("eventX")
            .token("tokenX")
            .caseDetails(CaseDetails.builder().id(10L).state("STATE").build())
            .build();

        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(startEventResponse);
        when(caseDetailsConverter.toCaseData((CaseDetails) ArgumentMatchers.any())).thenReturn(mock(CaseData.class));
        when(migrationTask.migrateCaseData(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mock(CaseData.class));
        when(migrationTask.getUpdatedState(ArgumentMatchers.any())).thenReturn(Optional.empty());
        when(migrationTask.getEventSummary()).thenReturn("summary");
        when(migrationTask.getEventDescription()).thenReturn("description");

        asyncCaseMigrationService.migrateCasesAsync(migrationTask, List.of(new CaseReference("123")), "STATE");

        // Should still submit updates but without new state
        verify(coreCaseDataService).submitUpdate(ArgumentMatchers.eq("123"), ArgumentMatchers.any(CaseDataContent.class));
    }

    @Test
    void shouldIncludeCorrectEventDetailsInBuiltCaseDataContent() {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId("event123")
            .token("tokenABC")
            .caseDetails(CaseDetails.builder().id(999L).state("STATE").build())
            .build();

        CaseData caseData = mock(CaseData.class);
        when(caseData.toMap(ArgumentMatchers.any())).thenReturn(Map.of("a", "b"));

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.getEventSummary()).thenReturn("Summary123");
        when(migrationTask.getEventDescription()).thenReturn("Desc456");

        CaseDataContent content = asyncCaseMigrationService.buildCaseDataContent(startEventResponse, caseData, migrationTask);

        assertEquals("event123", content.getEvent().getId());
        assertEquals("tokenABC", content.getEventToken());
        assertEquals("Summary123", content.getEvent().getSummary());
        assertEquals("Desc456", content.getEvent().getDescription());
        assertEquals(Map.of("a", "b"), content.getData());
    }

    @Test
    void shouldPassCorrectCaseDataContentToSubmitUpdate() {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId("event123")
            .token("token123")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .state("STATE")
                             .build())
            .build();

        when(coreCaseDataService.startUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(startEventResponse);
        when(caseDetailsConverter.toCaseData((CaseDetails) ArgumentMatchers.any())).thenReturn(mock(CaseData.class));

        @SuppressWarnings("unchecked")
        MigrationTask<CaseReference> migrationTask = mock(MigrationTask.class);
        when(migrationTask.migrateCaseData(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mock(CaseData.class));
        when(migrationTask.getEventSummary()).thenReturn("summary");
        when(migrationTask.getEventDescription()).thenReturn("description");

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        asyncCaseMigrationService.migrateCasesAsync(migrationTask, List.of(new CaseReference("1")), null);

        verify(coreCaseDataService).submitUpdate(ArgumentMatchers.eq("1"), captor.capture());
        CaseDataContent sent = captor.getValue();

        assertEquals("event123", sent.getEvent().getId());
        assertEquals("token123", sent.getEventToken());
        assertEquals("summary", sent.getEvent().getSummary());
    }
}
