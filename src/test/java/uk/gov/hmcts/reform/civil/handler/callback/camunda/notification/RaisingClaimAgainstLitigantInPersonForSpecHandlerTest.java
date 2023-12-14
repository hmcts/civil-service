package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RaisingClaimAgainstLitigantInPersonForSpecNotificationHandler.class
})
public class RaisingClaimAgainstLitigantInPersonForSpecHandlerTest {

    private static final String TEMPLATE_ID = "template-id";

    @Autowired
    private RaisingClaimAgainstLitigantInPersonForSpecNotificationHandler handler;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setup() {
        when(notificationsProperties.getClaimantSolicitorSpecCaseWillProgressOffline()).thenReturn(TEMPLATE_ID);
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenInvoked() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        // When
        handler.handle(params);

        // Then
        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "applicant-create-case-handed-offline-notification-000DC001"
        );
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC").build())
                                                 .build())).isEqualTo(RaisingClaimAgainstLitigantInPersonForSpecNotificationHandler.TASK_ID);
    }
}
