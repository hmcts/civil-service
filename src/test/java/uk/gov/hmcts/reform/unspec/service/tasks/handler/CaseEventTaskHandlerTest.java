package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;

@ExtendWith(SpringExtension.class)
class CaseEventTaskHandlerTest {

    private static final Long CASE_ID = 1L;

    @Mock
    private ExternalTask mockExternalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @InjectMocks
    private CaseEventTaskHandler caseEventTaskHandler;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
        when(mockExternalTask.getActivityId()).thenReturn("activityId");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "CCD_ID", CASE_ID,
                "CASE_EVENT", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name()
            ));
    }

    @Nested
    class SuccessHandler {

        @Test
        void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
            caseEventTaskHandler.execute(mockExternalTask, externalTaskService);

            verify(coreCaseDataService).triggerEvent(
                eq(CASE_ID),
                eq(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE),
                anyMap()
            );
            verify(externalTaskService).complete(mockExternalTask);
        }
    }
}
