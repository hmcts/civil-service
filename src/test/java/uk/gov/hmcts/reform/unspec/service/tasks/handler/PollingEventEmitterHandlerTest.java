package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.service.search.CaseReadyBusinessProcessSearchService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;

@SpringBootTest(classes = {JacksonAutoConfiguration.class, CaseDetailsConverter.class})
class PollingEventEmitterHandlerTest {

    @MockBean
    private ExternalTask externalTask;

    @MockBean
    private ExternalTaskService externalTaskService;

    @MockBean
    private CaseReadyBusinessProcessSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private RuntimeService runtimeService;

    @MockBean
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    private PollingEventEmitterHandler pollingEventEmitterHandler;

    @BeforeEach
    void init() {
        pollingEventEmitterHandler = new PollingEventEmitterHandler(
            searchService,
            caseDetailsConverter,
            applicationEventPublisher,
            runtimeService
        );
        when(externalTask.getTopicName()).thenReturn("test");
        when(searchService.getCases()).thenReturn(List.of(
            CaseDetails.builder().id(1L).data(
                Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT1"))).build(),
            CaseDetails.builder().id(2L).data(
                Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT2"))).build(),
            CaseDetails.builder().id(3L).data(
                Map.of("businessProcess", businessProcessWithCamundaEvent("TEST_EVENT3"))).build()
        ));
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldNotSendMessageAndTriggerEvent_whenZeroCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(searchService).getCases();
        verifyNoInteractions(runtimeService);
        verifyNoInteractions(applicationEventPublisher);
        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void shouldSendMessageAndTriggerEvents_whenCasesFound() {
        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(searchService).getCases();
        verify(runtimeService).createMessageCorrelation("TEST_EVENT1");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 1L);
        verify(applicationEventPublisher).publishEvent(
            new DispatchBusinessProcessEvent(1L, businessProcessWithCamundaEvent("TEST_EVENT1")));

        verify(runtimeService).createMessageCorrelation("TEST_EVENT2");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 2L);
        verify(applicationEventPublisher).publishEvent(
            new DispatchBusinessProcessEvent(2L, businessProcessWithCamundaEvent("TEST_EVENT2")));

        verify(runtimeService).createMessageCorrelation("TEST_EVENT3");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 3L);
        verify(applicationEventPublisher).publishEvent(
            new DispatchBusinessProcessEvent(3L, businessProcessWithCamundaEvent("TEST_EVENT3")));

        verify(messageCorrelationBuilder, times(3)).correlateStartMessage();
        verify(externalTaskService).complete(externalTask);

        verifyNoMoreInteractions(runtimeService);
        verifyNoMoreInteractions(messageCorrelationBuilder);
        verifyNoMoreInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSkipFailedCaseAndContinueProcess_whenExceptionThrownForCase() {
        when(messageCorrelationBuilder.correlateStartMessage())
            .thenReturn(mock(ProcessInstance.class))
            .thenThrow(RuntimeException.class)
            .thenReturn(mock(ProcessInstance.class));

        pollingEventEmitterHandler.execute(externalTask, externalTaskService);

        verify(searchService).getCases();
        verify(runtimeService).createMessageCorrelation("TEST_EVENT1");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 1L);
        verify(applicationEventPublisher).publishEvent(
            new DispatchBusinessProcessEvent(1L, businessProcessWithCamundaEvent("TEST_EVENT1")));

        verify(runtimeService).createMessageCorrelation("TEST_EVENT2");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 2L);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT3");
        verify(messageCorrelationBuilder).setVariable("CCD_ID", 3L);
        verify(applicationEventPublisher).publishEvent(
            new DispatchBusinessProcessEvent(3L, businessProcessWithCamundaEvent("TEST_EVENT3")));

        verify(messageCorrelationBuilder, times(3)).correlateStartMessage();
        verify(externalTaskService).complete(externalTask);

        verifyNoMoreInteractions(runtimeService);
        verifyNoMoreInteractions(messageCorrelationBuilder);
        verifyNoMoreInteractions(applicationEventPublisher);
    }

    private BusinessProcess businessProcessWithCamundaEvent(String camundaEvent) {
        return BusinessProcess.builder()
            .activityId("testActivityId")
            .processInstanceId("testInstanceId")
            .camundaEvent(camundaEvent)
            .status(FINISHED)
            .build();
    }

}
