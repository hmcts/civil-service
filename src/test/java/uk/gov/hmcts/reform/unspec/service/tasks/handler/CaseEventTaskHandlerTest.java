package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;

@SpringBootTest(classes = {
    CaseEventTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
@ExtendWith(SpringExtension.class)
class CaseEventTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CaseEventTaskHandler caseEventTaskHandler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");

        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);
    }

    @Test
    void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE)))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE));
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE))
            .thenAnswer(invocation -> {
                throw new Exception(errorMessage);
            });

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(mockTask, "worker", errorMessage, 2, 500L);
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        String errorMessage = "there was an error";

        doThrow(new NotFoundException(errorMessage)).when(externalTaskService).complete(mockTask);

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }
}
