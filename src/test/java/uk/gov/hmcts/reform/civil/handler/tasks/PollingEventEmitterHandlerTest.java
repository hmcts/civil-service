package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    PollingEventEmitterHandler.class})
class PollingEventEmitterHandlerTest {

    @MockBean
    private ExternalTask externalTask;

    @MockBean
    private ExternalTaskService externalTaskService;

    @MockBean
    private CaseReadyBusinessProcessSearchService searchService;

    @MockBean
    private EventEmitterService eventEmitterService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private PollingEventEmitterHandler pollingEventEmitterHandler;

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;

    @BeforeEach
    void init() {
        caseDetails1 = CaseDetails.builder().id(1L).data(
            Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT1"))).build();
        caseDetails2 = CaseDetails.builder().id(2L).data(
            Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT2"))).build();
        caseDetails3 = CaseDetails.builder().id(3L).data(
            Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT3"))).build();
        when(searchService.getCases()).thenReturn(List.of(caseDetails1, caseDetails2, caseDetails3));
    }

    @Test
    void shouldNotSendMessageAndTriggerEvent_whenZeroCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(searchService).getCases();
        verifyNoInteractions(eventEmitterService);
        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesFound() {
        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(searchService).getCases();
        verify(eventEmitterService).emitBusinessProcessCamundaEvent(
            caseDetailsConverter.toCaseData(caseDetails1),
            true
        );
        verify(eventEmitterService).emitBusinessProcessCamundaEvent(
            caseDetailsConverter.toCaseData(caseDetails2),
            true
        );
        verify(eventEmitterService).emitBusinessProcessCamundaEvent(
            caseDetailsConverter.toCaseData(caseDetails3),
            true
        );
        verify(externalTaskService).complete(externalTask);

        verifyNoMoreInteractions(eventEmitterService);
    }

    @Test
    void shouldHave0Retries_whenException() {
        String errorMessage = "there was an error";

        //Mockito instance of external task returns 0 instead of null
        when(externalTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(externalTaskService, never()).complete(externalTask);
        verify(externalTaskService).handleFailure(
            eq(externalTask),
            eq(errorMessage),
            anyString(),
            eq(0),
            eq(500L)
        );

        verifyNoMoreInteractions(eventEmitterService);
    }

    private BusinessProcess businessProcessWithCamundaEvent(String camundaEvent) {
        return BusinessProcess.builder()
            .activityId("testActivityId")
            .processInstanceId("testInstanceId")
            .camundaEvent(camundaEvent)
            .status(READY)
            .build();
    }
}
