package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CaseTakenOfflineForSpecApplicantNotificationHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineForSpecHandlerTest {

    @InjectMocks
    private CaseTakenOfflineForSpecApplicantNotificationHandler handler;

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC").build()).build())).isEqualTo(TASK_ID);
    }
}
