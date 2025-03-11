package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetriggerUpdateLocationDataHandlerTest {

    private static final String EVENT_DESCRIPTION = "Process ID: 1";

    @Mock
    private CoreCaseDataService coreCaseDataService;
    private RetriggerUpdateLocationDataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RetriggerUpdateLocationDataHandler(coreCaseDataService);
    }

    @Test
    void testHandleTask_RetriggerUpdateCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("UPDATE_CASE_DATA");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("ePimsId")).thenReturn("123456");
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            1L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "Re-trigger of UPDATE_CASE_DATA",
            EVENT_DESCRIPTION
        );
        verify(coreCaseDataService).triggerUpdateLocationEpimdsIdEvent(
            2L,
            CaseEvent.UPDATE_CASE_DATA,
            "123456",
            "Re-trigger of UPDATE_CASE_DATA",
            EVENT_DESCRIPTION
        );
    }

    @Test
    void testHandleTask_RetriggerUpdateLocationWithMissingCaseEvent() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("CASE_EVENT");
        when(externalTask.getVariable("caseIds")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingEpimsId() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("CASE_EVENT");
        when(externalTask.getVariable("caseIds")).thenReturn("1");
        when(externalTask.getVariable("ePimsId")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }
}
