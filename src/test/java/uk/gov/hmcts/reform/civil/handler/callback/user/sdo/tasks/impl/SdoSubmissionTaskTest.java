package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSubmissionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoSubmissionTaskTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private SdoSubmissionService submissionService;

    @Test
    void shouldDelegateSubmissionToService() {
        SdoSubmissionTask task = new SdoSubmissionTask(submissionService);
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();
        CaseData updatedCaseData = CaseData.builder().ccdCaseReference(456L).build();
        when(submissionService.prepareSubmission(caseData, AUTH_TOKEN)).thenReturn(updatedCaseData);

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();
        SdoTaskContext context = new SdoTaskContext(caseData, params, SdoLifecycleStage.SUBMISSION);

        SdoTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(updatedCaseData);
        assertThat(result.errors()).isEmpty();
        verify(submissionService).prepareSubmission(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldSupportSubmissionStageOnly() {
        SdoSubmissionTask task = new SdoSubmissionTask(submissionService);

        assertThat(task.supports(SdoLifecycleStage.SUBMISSION)).isTrue();
        assertThat(task.supports(SdoLifecycleStage.MID_EVENT)).isFalse();
    }
}
