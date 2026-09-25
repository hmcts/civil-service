package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HEARING_NOTICE;

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

    @Mock
    private RuntimeService runtimeService;

    private EndBusinessProcessTaskHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        handler = new EndBusinessProcessTaskHandler(
            new ExternalTaskCompletionService(),
            new EventProperties(),
            coreCaseDataService,
            caseDetailsConverter,
            objectMapper,
            runtimeService
        );
    }

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockExternalTask.getAllVariables())
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldTriggerEndBusinessProcessCCDEventAndUpdateBusinessProcessStatusToFinished_whenCalled(boolean flagAbsent) {
        if (!flagAbsent) {
            when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
                "caseId", CASE_ID, "caseEvent", END_BUSINESS_PROCESS, "hearingNoticeSkipped", false));
        }
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
        verify(coreCaseDataService, never()).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);
        verifyNoInteractions(runtimeService);
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void shouldTriggerManualHearingListingTask_whenHearingNoticeWasSkipped() {
        CaseData caseData = new CaseDataBuilder()
            .atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);
        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);
        when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID,
            "caseEvent", END_BUSINESS_PROCESS,
            "hearingNoticeSkipped", true
        ));

        handler.execute(mockExternalTask, externalTaskService);

        var ordered = inOrder(runtimeService, coreCaseDataService, externalTaskService);
        ordered.verify(runtimeService).setVariable(PROCESS_INSTANCE_ID, "invalidHearingNoticePending", true);
        ordered.verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        ordered.verify(coreCaseDataService).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);
        ordered.verify(runtimeService).setVariable(PROCESS_INSTANCE_ID, "invalidHearingNoticePending", false);
        ordered.verify(externalTaskService).complete(mockExternalTask, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotTriggerDuplicateHearingTask_whenSkippedProcessIsAlreadyFinished(boolean pendingFlagAbsent) {
        stubSkippedProcess(BusinessProcessStatus.FINISHED);
        if (!pendingFlagAbsent) {
            when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
                "caseId", CASE_ID, "caseEvent", END_BUSINESS_PROCESS,
                "hearingNoticeSkipped", true, "invalidHearingNoticePending", false));
        }

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(CaseDataContent.class));
        verify(coreCaseDataService, never()).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);
        verifyNoInteractions(runtimeService);
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void shouldNotTriggerHearingTask_whenEndingBusinessProcessFails() {
        stubSkippedProcess(BusinessProcessStatus.READY);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class)))
            .thenThrow(new RuntimeException("CCD update failed"));

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService, never()).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);
        verify(externalTaskService, never()).complete(mockExternalTask, null);
        verify(externalTaskService).handleFailure(eq(mockExternalTask), eq("CCD update failed"),
                                                anyString(), anyInt(), anyLong());
    }

    @Test
    void shouldRetryHearingTaskCreation_whenEventFailsAfterBusinessProcessEnds() {
        stubSkippedProcess(BusinessProcessStatus.READY);
        doThrow(new RuntimeException("CCD event failed")).doNothing()
            .when(coreCaseDataService).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);

        handler.execute(mockExternalTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockExternalTask, null);
        verify(externalTaskService).handleFailure(eq(mockExternalTask), eq("CCD event failed"),
                                                anyString(), anyInt(), anyLong());
        verify(runtimeService).setVariable(PROCESS_INSTANCE_ID, "invalidHearingNoticePending", true);
        verify(runtimeService, never()).setVariable(PROCESS_INSTANCE_ID, "invalidHearingNoticePending", false);

        // The successful END_BUSINESS_PROCESS update is persisted before the failed event call.
        stubSkippedProcess(BusinessProcessStatus.FINISHED);
        when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID, "caseEvent", END_BUSINESS_PROCESS,
            "hearingNoticeSkipped", true, "invalidHearingNoticePending", true));
        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService, times(1)).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(coreCaseDataService, times(2)).triggerEvent(Long.valueOf(CASE_ID), INVALID_HEARING_NOTICE);
        verify(runtimeService).setVariable(PROCESS_INSTANCE_ID, "invalidHearingNoticePending", false);
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    private void stubSkippedProcess(BusinessProcessStatus status) {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(status)).build();
        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS))
            .thenReturn(startEventResponse(new CaseDetailsBuilder().data(caseData).build()));
        when(mockExternalTask.getAllVariables()).thenReturn(Map.of(
            "caseId", CASE_ID, "caseEvent", END_BUSINESS_PROCESS, "hearingNoticeSkipped", true));
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
