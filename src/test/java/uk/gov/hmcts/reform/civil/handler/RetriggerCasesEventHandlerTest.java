package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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
        when(externalTask.getVariable("caseData")).thenReturn(null);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(1L), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE), anyMap());
        verify(coreCaseDataService).triggerEvent(eq(2L), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE), anyMap());
    }

    @Test
    void testHandleTask_RetriggerClaimantResponseSpecific() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE_SPEC");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(null);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(1L), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC), anyMap());
        verify(coreCaseDataService).triggerEvent(eq(2L), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC), anyMap());
    }

    @Test
    void testHandleTask_RetriggerCases() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(null);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(1L), eq(CaseEvent.RETRIGGER_CASES), anyMap());
        verify(coreCaseDataService).triggerEvent(eq(2L), eq(CaseEvent.RETRIGGER_CASES), anyMap());
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
        when(externalTask.getVariable("caseData")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerCasesThrowsExceptionAndCarriesOn() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        doThrow(new RuntimeException()).when(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(2L), eq(CaseEvent.RETRIGGER_CASES), anyMap());
    }

    @Test
    void testHandleTask_RetriggerCasesSetsCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        Map<String, Object> caseData = Map.of("myField", "myValue");
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(caseData);
        doThrow(new RuntimeException()).when(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(2L), eq(CaseEvent.RETRIGGER_CASES), eq(caseData));
    }

}
