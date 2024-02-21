package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetriggerCasesEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private RetriggerCasesEventHandler handler;

    @Test
    void testHandleTask_RetriggerClaimantResponse() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE);
        verify(coreCaseDataService).triggerEvent(2L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE);
    }

    @Test
    void testHandleTask_RetriggerClaimantResponseSpecific() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE_SPEC");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC);
        verify(coreCaseDataService).triggerEvent(2L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC);
    }

    @Test
    void testHandleTask_RetriggerCases() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);
        verify(coreCaseDataService).triggerEvent(2L, CaseEvent.RETRIGGER_CASES);
    }

    @Test
    void testHandleTask_RetriggerCasesWithMissingCaseEvent() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerCasesWithMissingCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("CASE_EVENT");
        when(externalTask.getVariable("caseIds")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerCasesThrowsExceptionAndCarriesOn() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        doThrow(new RuntimeException()).when(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(2L, CaseEvent.RETRIGGER_CASES);
    }

}
