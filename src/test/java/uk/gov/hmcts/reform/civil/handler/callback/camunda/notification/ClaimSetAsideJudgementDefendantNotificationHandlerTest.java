package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@SpringBootTest(classes = {
    ClaimSetAsideJudgementDefendantNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
public class ClaimSetAsideJudgementDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {
    public static final String TEMPLATE_ID = "template-id";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Captor
    private ArgumentCaptor<String> targetEmail;

    @Captor
    private ArgumentCaptor<String> emailTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;

    @Autowired
    private ClaimSetAsideJudgementDefendantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifySetAsideJudgementTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyDefendantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.SET_ASIDE_JUDGMENT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "set-aside-judgement-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantBothSolicitors_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.SET_ASIDE_JUDGMENT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                targetEmail.capture(),
                emailTemplate.capture(),
                notificationDataMap.capture(),
                reference.capture()
            );
            //Email to respondent1
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondentsolicitor@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("template-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("set-aside-judgement-applicant-notification-000DC001");
            //Email to respondent2
            assertThat(targetEmail.getAllValues().get(1)).isEqualTo("respondentsolicitor2@example.com");
            assertThat(emailTemplate.getAllValues().get(1)).isEqualTo("template-id");
            assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMap2(caseData));
            assertThat(reference.getAllValues().get(1)).isEqualTo("set-aside-judgement-applicant-notification-000DC001");

        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            LEGAL_ORG, "Test Org Name",
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName()
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap2(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getRespondent2().getPartyName(),
            LEGAL_ORG, "Test Org Name",
            DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName()
        );
    }

}
