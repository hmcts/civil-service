package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dj.DjSubmissionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class DjSubmissionTaskTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DjSubmissionService submissionService;

    @Test
    void shouldDelegateSubmissionToService() {
        DjSubmissionTask task = new DjSubmissionTask(submissionService);
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        CaseData updatedCaseData = CaseData.builder().ccdCaseReference(2L).build();
        when(submissionService.prepareSubmission(caseData, AUTH_TOKEN)).thenReturn(updatedCaseData);

        CallbackParams params = CallbackParams.builder()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.SUBMISSION);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(updatedCaseData);
        assertThat(result.errors()).isEmpty();
        verify(submissionService).prepareSubmission(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldSupportSubmissionStageOnly() {
        DjSubmissionTask task = new DjSubmissionTask(submissionService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.SUBMISSION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isFalse();
    }
}
