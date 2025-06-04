package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReferenceCsvLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.RetriggerUpdateLocationDataHandler.CASE_IDS;
import static uk.gov.hmcts.reform.civil.handler.RetriggerUpdateLocationDataHandler.CASE_IDS_CSV_FILENAME;

@ExtendWith(MockitoExtension.class)
class RetriggerUpdateLocationDataHandlerTest {

    private static final String EVENT_DESCRIPTION = "Process ID: 1";

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    private RetriggerUpdateLocationDataHandler handler;
    private static final String YES = "Yes";

    @BeforeEach
    void setUp() {
        handler = new RetriggerUpdateLocationDataHandler(coreCaseDataService, caseReferenceCsvLoader);
    }

    @Test
    void testHandleTask_RetriggerUpdateCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable(CASE_IDS)).thenReturn("1,2");
        when(externalTask.getVariable("region")).thenReturn("2");
        when(externalTask.getVariable("ePimId")).thenReturn("123456");
        when(externalTask.getVariable("caseManagementLocation")).thenReturn(YES);
        when(externalTask.getVariable("courtLocation")).thenReturn(YES);
        when(externalTask.getVariable("applicant1DQRequestedCourt")).thenReturn(YES);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            1L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "2",
            YES,
            YES,
            YES,
            "Update locations epimId by 123456",
            EVENT_DESCRIPTION
        );
        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            2L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "2",
            YES,
            YES,
            YES,
            "Update locations epimId by 123456",
            EVENT_DESCRIPTION
        );
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingEpimsId() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn("1");
        when(externalTask.getVariable("ePimId")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerUpdateCaseDataFromCsv() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable(CASE_IDS_CSV_FILENAME)).thenReturn("caseIds.csv");
        when(externalTask.getVariable("region")).thenReturn("2");
        when(externalTask.getVariable("ePimId")).thenReturn("123456");
        when(externalTask.getVariable("caseManagementLocation")).thenReturn(YES);
        when(externalTask.getVariable("courtLocation")).thenReturn(YES);
        when(externalTask.getVariable("applicant1DQRequestedCourt")).thenReturn(YES);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        when(caseReferenceCsvLoader.loadCaseReferenceList("caseIds.csv"))
            .thenReturn(List.of(new CaseReference("1"), new CaseReference("2")));

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            1L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "2",
            YES,
            YES,
            YES,
            "Update locations epimId by 123456",
            EVENT_DESCRIPTION
        );
        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            2L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "2",
            YES,
            YES,
            YES,
            "Update locations epimId by 123456",
            EVENT_DESCRIPTION
        );
    }

    @Test
    void testHandleTask_ExceptionDuringCsvLoading() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable(CASE_IDS_CSV_FILENAME)).thenReturn("caseIds.csv");
        when(externalTask.getVariable("region")).thenReturn("2");
        when(externalTask.getVariable("ePimId")).thenReturn("123456");
        when(externalTask.getVariable(CASE_IDS)).thenReturn(null);
        when(externalTask.getVariable("caseManagementLocation")).thenReturn(YES);
        when(externalTask.getVariable("courtLocation")).thenReturn(YES);
        when(externalTask.getVariable("applicant1DQRequestedCourt")).thenReturn(YES);
        when(externalTask.getProcessInstanceId()).thenReturn("1");
        when(caseReferenceCsvLoader.loadCaseReferenceList("caseIds.csv"))
            .thenThrow(new RuntimeException("CSV loading error"));

        handler.handleTask(externalTask);

        verify(coreCaseDataService, never()).triggerUpdateLocationEpimdsIdEvent(
            anyLong(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void testHandleTask_MissingRegionVariable() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable(CASE_IDS)).thenReturn("1,2");
        when(externalTask.getVariable("region")).thenReturn(null);
        when(externalTask.getVariable("ePimId")).thenReturn("123456");

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }
}
