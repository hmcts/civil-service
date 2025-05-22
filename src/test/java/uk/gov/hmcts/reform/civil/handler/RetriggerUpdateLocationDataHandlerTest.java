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
    private static final String YES = "Yes";

    @BeforeEach
    void setUp() {
        handler = new RetriggerUpdateLocationDataHandler(coreCaseDataService);
    }

    @Test
    void testHandleTask_RetriggerUpdateCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
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
}
