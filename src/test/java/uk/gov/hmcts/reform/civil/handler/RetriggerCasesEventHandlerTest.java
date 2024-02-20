package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetriggerCasesEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private RetriggerCasesEventHandler retriggerCasesEventHandler;

    @Test
    void testHandleTask_RetriggerClaimantResponse() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = spy(new RetriggerCasesEventHandler(coreCaseDataServiceMock));

        handler.handleTask(externalTask);

        verify(coreCaseDataServiceMock).triggerEvent(1L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE);
        verify(coreCaseDataServiceMock).triggerEvent(2L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE);
    }

    @Test
    void testHandleTask_RetriggerClaimantResponseSpecific() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE_SPEC");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = spy(new RetriggerCasesEventHandler(coreCaseDataServiceMock));

        handler.handleTask(externalTask);

        verify(coreCaseDataServiceMock).triggerEvent(1L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC);
        verify(coreCaseDataServiceMock).triggerEvent(2L, CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC);
    }

    @Test
    void testHandleTask_RetriggerCases() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = spy(new RetriggerCasesEventHandler(coreCaseDataServiceMock));

        handler.handleTask(externalTask);

        verify(coreCaseDataServiceMock).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);
        verify(coreCaseDataServiceMock).triggerEvent(2L, CaseEvent.RETRIGGER_CASES);
    }

}
