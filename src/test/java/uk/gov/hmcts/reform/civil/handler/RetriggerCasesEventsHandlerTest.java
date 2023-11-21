package uk.gov.hmcts.reform.civil.handler;

import feign.FeignException;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetriggerCasesEventsHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private RetriggerCasesEventsHandler retriggerCasesEventsHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleTask() {
        ExternalTask externalTask = mock(ExternalTask.class);

        // Calling the method to test
        retriggerCasesEventsHandler.handleTask(externalTask);
        when(retriggerCasesEventsHandler.readCaseIds(anyString())).thenReturn(singletonList("123L"));

        when(coreCaseDataService.startUpdate(eq("123L"), eq(CaseEvent.RETRIGGER_CASES)))
            .thenThrow(FeignException.class);

        // Mocking the coreCaseDataService to return case data
        when(coreCaseDataService.getCase(eq(123L))).thenReturn(CaseDetails.builder().data(Collections.emptyMap()).build());

      
        verify(retriggerCasesEventsHandler).readCaseIds(anyString());
        verify(coreCaseDataService).startUpdate(eq("123L"), eq(CaseEvent.RETRIGGER_CASES));
        verify(coreCaseDataService).submitUpdate(eq("123L"), any());
    }
}
