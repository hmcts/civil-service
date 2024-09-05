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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetriggerCaseFlagEventHandlerTest {

    private static final String EVENT_DESCRIPTION = "Process ID: 1";

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private RetriggerCaseFlagEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RetriggerCaseFlagEventHandler(coreCaseDataService);
    }

    @Test
    void testHandleTask_RetriggerRespondent1Flag() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).updateCaseFlagEvent(
            eq(1L),
            eq(CaseEvent.UPDATE_CASE_DATA),
            eq("Re-trigger of UPDATE_CASE_DATA"),
            eq(EVENT_DESCRIPTION)
        );
        verify(coreCaseDataService).updateCaseFlagEvent(
            eq(2L),
            eq(CaseEvent.UPDATE_CASE_DATA),
            eq("Re-trigger of UPDATE_CASE_DATA"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerCasesEmptyCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn(null);
        // expect exception when handler.handleTask is called
        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }
}
