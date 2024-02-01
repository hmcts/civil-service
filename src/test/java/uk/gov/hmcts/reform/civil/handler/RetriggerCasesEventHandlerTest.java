package uk.gov.hmcts.reform.civil.handler;

import feign.FeignException;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetriggerCasesEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private RetriggerCasesEventHandler retriggerCasesEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleTask_RetriggerClaimantResponse() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("eventForRetrigger")).thenReturn(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE);
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = new RetriggerCasesEventHandler(coreCaseDataServiceMock);

        handler.handleTask(externalTask);

        verify(coreCaseDataServiceMock).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE));
    }

    @Test
    void testHandleTask_RetriggerClaimantResponseSpecific() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("eventForRetrigger")).thenReturn(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC);
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = new RetriggerCasesEventHandler(coreCaseDataServiceMock);

        handler.handleTask(externalTask);

        verify(coreCaseDataServiceMock).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC));
    }

    @Test
    void testHandleTask_RetriggerCases() {
        // Given
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("eventForRetrigger")).thenReturn(CaseEvent.RETRIGGER_CASES);
        CoreCaseDataService coreCaseDataServiceMock = mock(CoreCaseDataService.class);

        RetriggerCasesEventHandler handler = new RetriggerCasesEventHandler(coreCaseDataServiceMock);

        // When
        handler.handleTask(externalTask);

        // Then
        verify(coreCaseDataServiceMock).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CASES));
    }

    @Test
    void testUpdateCaseByEvent_RetriggerClaimantResponse_Successful() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");
        CaseEvent caseEvent = CaseEvent.RETRIGGER_CLAIMANT_RESPONSE;

        retriggerCasesEventHandler.updateCaseByEvent(caseIdList, caseEvent);

        caseIdList.forEach(caseId -> verify(coreCaseDataService).triggerEvent(Long.parseLong(caseId), caseEvent));
    }

    @Test
    void testUpdateCaseByEvent_RetriggerClaimantResponse_EmptyList() {
        List<String> caseIdList = Collections.emptyList();
        CaseEvent caseEvent = CaseEvent.RETRIGGER_CLAIMANT_RESPONSE;

        retriggerCasesEventHandler.updateCaseByEvent(caseIdList, caseEvent);

        verify(coreCaseDataService, never()).triggerEvent(anyLong(), any());
    }

    @Test
    void testUpdateCaseByEvent_RetriggerClaimantResponse_FeignException() {
        List<String> caseIdList = Collections.singletonList("1");
        CaseEvent caseEvent = CaseEvent.RETRIGGER_CLAIMANT_RESPONSE;
        doThrow(FeignException.class).when(coreCaseDataService).triggerEvent(anyLong(), eq(caseEvent));

        assertThrows(FeignException.class, () -> retriggerCasesEventHandler.updateCaseByEvent(caseIdList, caseEvent));

        verify(coreCaseDataService).triggerEvent(anyLong(), eq(caseEvent));
    }

    @Test
    void testUpdateCaseByEvent_RetriggerClaimantResponseSpecific_Successful() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");
        CaseEvent caseEvent = CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC;

        retriggerCasesEventHandler.updateCaseByEvent(caseIdList, caseEvent);

        caseIdList.forEach(caseId -> verify(coreCaseDataService).triggerEvent(Long.parseLong(caseId), caseEvent));
    }

    @Test
    void testUpdateCaseByEvent_RetriggerCases_Successful() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");
        CaseEvent caseEvent = CaseEvent.RETRIGGER_CASES;

        retriggerCasesEventHandler.updateCaseByEvent(caseIdList, caseEvent);

        caseIdList.forEach(caseId -> verify(coreCaseDataService).triggerEvent(Long.parseLong(caseId), caseEvent));
    }
}
