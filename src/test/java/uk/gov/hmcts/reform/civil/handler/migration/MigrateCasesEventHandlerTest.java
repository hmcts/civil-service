package uk.gov.hmcts.reform.civil.handler.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationEncryptionUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateCasesEventHandlerTest {

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MigrationTaskFactory migrationTaskFactory;

    private MigrateCasesEventHandler handler;

    @BeforeEach
    void setUp() {
        caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        MockitoAnnotations.openMocks(this);
        handler = new MigrateCasesEventHandler(
            caseReferenceCsvLoader,
            coreCaseDataService,
            caseDetailsConverter,
            migrationTaskFactory,
            objectMapper,
            500, // migrationBatchSize
            10,
            "DUMMY_SECRET"// migrationWaitTime
        );
    }

    @Test
    void shouldHandleTaskSuccessfully() {
        // Arrange
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("taskName")).thenReturn("testTask");
        when(externalTask.getVariable("caseIds")).thenReturn("12345,67890");
        when(externalTask.getVariable("csvFileName")).thenReturn(null);

        MigrationTask migrationTask = mock(MigrationTask.class);
        when(migrationTask.getEventDescription()).thenReturn("Test Migration Task");
        when(migrationTask.getEventSummary()).thenReturn("Migrating cases for test task");

        when(migrationTaskFactory.getMigrationTask("testTask")).thenReturn(Optional.of(migrationTask));

        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        when(coreCaseDataService.startUpdate(anyString(), eq(CaseEvent.UPDATE_CASE_DATA))).thenReturn(startEventResponse);

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = mock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.class);
        when(startEventResponse.getCaseDetails()).thenReturn(caseDetails);

        CaseData caseData = mock(CaseData.class);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);
        when(migrationTask.migrateCaseData(caseData)).thenReturn(caseData);

        ExternalTaskData result = handler.handleTask(externalTask);

        // Assert
        assertNotNull(result);
        verify(coreCaseDataService, times(2)).startUpdate(anyString(), eq(CaseEvent.UPDATE_CASE_DATA));
        verify(coreCaseDataService, times(2)).submitUpdate(anyString(), any(CaseDataContent.class));
    }

    @Test
    void shouldReturnCaseReferencesFromCaseIds() {
        // Arrange
        String caseIds = "12345,67890";
        String csvFileName = null;

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(caseIds, csvFileName);

        // Assert
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getCaseReference());
        assertEquals("67890", result.get(1).getCaseReference());
    }

    @Test
    void shouldReturnCaseReferencesFromCsvFile() {
        // Arrange
        String caseIds = null;
        String csvFileName = "test.csv";
        List<CaseReference> mockReferences = List.of(new CaseReference("12345"), new CaseReference("67890"));
        when(caseReferenceCsvLoader.loadCaseReferenceList(csvFileName)).thenReturn(mockReferences);

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(caseIds, csvFileName);

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

        when(caseReferenceCsvLoader.loadCaseReferenceList(csvFileName, "DUMMY_SECRET")).thenReturn(mockReferences);

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(caseIds, csvFileName);

        // Assert
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getCaseReference());
        assertEquals("67890", result.get(1).getCaseReference());

        caseMigrationEncryptionUtilMockedStatic.close();
    }

    @Test
    void shouldReturnEmptyListWhenCaseIdsAndCsvFileNameAreNull() {
        // Arrange
        String caseIds = null;
        String csvFileName = null;

        // Act
        List<CaseReference> result = handler.getCaseReferenceList(caseIds, csvFileName);

        // Assert
        assertEquals(0, result.size());
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
        CaseDataContent result = handler.buildCaseDataContent(startEventResponse, caseData, migrationTask);

        // Assert
        assertEquals("testToken", result.getEventToken());
        assertEquals("testEventId", result.getEvent().getId());
        assertEquals(mockData, result.getData());

        assertEquals("Test Migration Task", result.getEvent().getDescription());
        assertEquals("Migrating cases for test task", result.getEvent().getSummary());
    }
}
