package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;

@ExtendWith(MockitoExtension.class)
class EndBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1234";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private EndBusinessProcessTaskHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        EventProperties eventProperties = new EventProperties();
        eventProperties.setRetryCount(3);
        handler = new EndBusinessProcessTaskHandler(
            new ExternalTaskCompletionService(),
            eventProperties,
            coreCaseDataService,
            caseDetailsConverter,
            objectMapper
        );
    }

    @BeforeEach
    void init() {
        lenient().when(mockExternalTask.getTopicName()).thenReturn("test");
        lenient().when(mockExternalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        lenient().when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", CASE_ID,
                "caseEvent", END_BUSINESS_PROCESS
            ));
    }

    @Test
    void shouldNotTriggerEndBusinessProcessCCDEvent_whenCalledMoreThanOnceInSequence() {
        CaseData caseData = new CaseDataBuilder()
            .atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.FINISHED))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);

        CaseDataContent caseDataContentWithFinishedStatus = getCaseDataContent(caseDetails, startEventResponse);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, END_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitUpdate(CASE_ID, caseDataContentWithFinishedStatus);
        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void shouldTriggerEndBusinessProcessCCDEventAndUpdateBusinessProcessStatusToFinished_whenCalled() {
        CaseData caseData = new CaseDataBuilder()
            .atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, END_BUSINESS_PROCESS);
        verify(coreCaseDataService).submitUpdate(CASE_ID, getCaseDataContent(caseDetails, startEventResponse));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void shouldBeRetryGuarded() {
        assertThat(handler.isRetryGuarded()).isTrue();
    }

    @Test
    void shouldRetryOnBadGatewayDuringSubmitUpdate() {
        when(mockExternalTask.getRetries()).thenReturn(null);

        CaseData caseData = new CaseDataBuilder()
            .atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);

        FeignException badGateway = FeignException.errorStatus(
            "submitUpdate",
            Response.builder()
                .request(Request.create(
                    Request.HttpMethod.POST,
                    "http://core-case-data-api/cases/" + CASE_ID + "/events",
                    Collections.emptyMap(),
                    null,
                    StandardCharsets.UTF_8,
                    null
                ))
                .status(502)
                .reason("Bad Gateway")
                .build()
        );
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenThrow(badGateway);

        handler.execute(mockExternalTask, externalTaskService);

        verify(externalTaskService).handleFailure(
            eq(mockExternalTask),
            anyString(),
            anyString(),
            eq(2),
            anyLong()
        );
        verify(externalTaskService, never()).complete(any(), any());
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token("1234")
            .eventId(END_BUSINESS_PROCESS.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDataContent getCaseDataContent(CaseDetails caseDetails, StartEventResponse value) {
        caseDetails.getData().put("businessProcess", new BusinessProcess().setStatus(BusinessProcessStatus.FINISHED));

        return CaseDataContent.builder()
            .eventToken(value.getToken())
            .event(Event.builder()
                       .id(value.getEventId())
                       .build())
            .data(caseDetails.getData())
            .build();
    }
}
