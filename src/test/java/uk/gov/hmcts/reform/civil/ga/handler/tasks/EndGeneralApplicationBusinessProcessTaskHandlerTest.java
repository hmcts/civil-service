package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS_GASPEC;

@ExtendWith(MockitoExtension.class)
class EndGeneralApplicationBusinessProcessTaskHandlerTest {

    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    private static final String CASE_ID = "1234";
    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private EndGeneralApplicationBusinessProcessTaskHandler handler;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    }

    @Test
    void shouldTriggerEndBusinessProcessCCDEventAndUpdateBusinessProcessStatusToFinished_whenCalled() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        ExternalTaskInput externalTaskInput = ExternalTaskInput.builder()
            .caseId(CASE_ID)
            .build();

        when(mapper.convertValue(any(), eq(ExternalTaskInput.class))).thenReturn(externalTaskInput);
        when(coreCaseDataService.startGaUpdate(CASE_ID, END_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        CaseDataContent caseDataContentWithFinishedStatus = getCaseDataContent(caseDetails, startEventResponse);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startGaUpdate(CASE_ID, END_BUSINESS_PROCESS_GASPEC);
        verify(coreCaseDataService).submitGaUpdate(CASE_ID, caseDataContentWithFinishedStatus);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldTriggerEndBusinessProcessCCDEventAfterSuccessfulPayment() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        ExternalTaskInput t = ExternalTaskInput.builder()
            .caseId(CASE_ID)
            .build();

        when(mapper.convertValue(any(), eq(ExternalTaskInput.class))).thenReturn(t);
        when(coreCaseDataService.startGaUpdate(CASE_ID, END_BUSINESS_PROCESS_GASPEC)).thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        CaseDataContent caseDataContentWithFinishedStatus = getCaseDataContent(caseDetails, startEventResponse);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startGaUpdate(CASE_ID, END_BUSINESS_PROCESS_GASPEC);
        verify(coreCaseDataService).submitGaUpdate(CASE_ID, caseDataContentWithFinishedStatus);
        verify(externalTaskService).complete(any(), any());
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token("1234")
            .eventId(END_BUSINESS_PROCESS_GASPEC.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDataContent getCaseDataContent(CaseDetails caseDetails, StartEventResponse value) {
        caseDetails.getData().put("businessProcess", new BusinessProcess()
            .setStatus(BusinessProcessStatus.FINISHED));

        return CaseDataContent.builder()
            .eventToken(value.getToken())
            .event(Event.builder()
                       .id(value.getEventId())
                       .build())
            .data(caseDetails.getData())
            .build();
    }
}
