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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void testUpdateCaseByEvent() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");

        retriggerCasesEventHandler.updateCaseByEvent(caseIdList, CaseEvent.RETRIGGER_CASES);

        // Assertions
        verify(coreCaseDataService, times(caseIdList.size())).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CASES));
    }

    @Test
    void testUpdateCaseByEventWithFeignException() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");

        // Simulate FeignException
        doThrow(FeignException.class).when(coreCaseDataService).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CASES));

        // Assertions
        FeignException exception = assertThrows(FeignException.class, () ->
            retriggerCasesEventHandler.updateCaseByEvent(caseIdList, CaseEvent.RETRIGGER_CASES)
        );
    }

    @Test
    void testUpdateCaseByEventWithGenericException() {
        List<String> caseIdList = Arrays.asList("1", "2", "3");

        doThrow(new RuntimeException("Simulated RuntimeException")).when(coreCaseDataService)
            .triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CASES));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            retriggerCasesEventHandler.updateCaseByEvent(caseIdList, CaseEvent.RETRIGGER_CASES)
        );

        assertEquals("Simulated RuntimeException", exception.getMessage());
    }

    @Test
    void testUpdateCaseByEventWithEmptyList() {
        List<String> emptyCaseIdList = Collections.emptyList();

        retriggerCasesEventHandler.updateCaseByEvent(emptyCaseIdList, CaseEvent.RETRIGGER_CASES);

        // Assertions
        verify(coreCaseDataService, never()).triggerEvent(anyLong(), eq(CaseEvent.RETRIGGER_CASES));
    }
}


