package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderParticipantService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjPrePopulateTaskTest {

    @Mock
    private DirectionsOrderParticipantService participantService;
    @InjectMocks
    private DjPrePopulateTask task;

    @Test
    void shouldPopulateApplicantVRespondentText() {
        CaseData caseData = CaseData.builder().build();
        when(participantService.buildApplicantVRespondentText(any())).thenReturn("Applicant v Respondent");

        CallbackParams params = CallbackParams.builder()
            .params(Collections.emptyMap())
            .build();
        DirectionsOrderTaskContext context =
            new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.PRE_POPULATE);

        DirectionsOrderTaskResult result = task.execute(context);

        assertThat(result.errors()).isEmpty();
        assertThat(result.submittedCallbackResponse()).isNull();
        assertThat(result.updatedCaseData().getApplicantVRespondentText()).isEqualTo("Applicant v Respondent");
        verify(participantService).buildApplicantVRespondentText(caseData);
    }

    @Test
    void shouldSupportPrePopulateStageOnly() {
        assertThat(task.supports(DirectionsOrderLifecycleStage.PRE_POPULATE)).isTrue();
        assertThat(task.supports(DirectionsOrderLifecycleStage.ORDER_DETAILS)).isFalse();
    }
}
