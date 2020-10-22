package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private ExternalTask mockExternalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @Autowired
    private CaseEventTaskHandler caseEventTaskHandler;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
        when(mockExternalTask.getActivityId()).thenReturn("activityId");

        Map<String, Object> variables = Map.of("caseId", CASE_ID,
                                               "caseEvent", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name()
        );

        when(mockExternalTask.getAllVariables()).thenReturn(variables);
    }

    @Nested
    class SuccessHandler {

        @Test
        void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
            CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(eq(CASE_ID), eq(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE)))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            caseEventTaskHandler.execute(mockExternalTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(eq(CASE_ID), eq(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE));
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockExternalTask);
        }
    }
}
