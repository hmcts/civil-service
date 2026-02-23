package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(123L);
        CaseData updatedCaseData = CaseDataBuilder.builder().build();
        updatedCaseData.setCcdCaseReference(456L);
        when(submissionService.prepareSubmission(caseData, AUTH_TOKEN)).thenReturn(updatedCaseData);

        CallbackParams params = new CallbackParams()
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN));
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.SUBMISSION);

        SdoSubmissionTask task = new SdoSubmissionTask(submissionService);
        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.updatedCaseData()).isEqualTo(updatedCaseData);
        assertThat(result.errors()).isEmpty();
        verify(submissionService).prepareSubmission(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldSupportSubmissionStageOnly() {
        SdoSubmissionTask task = new SdoSubmissionTask(submissionService);

        assertThat(task.supports(DirectionsOrderLifecycleStage.SUBMISSION)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.MID_EVENT)).isFalse();
    }
}
