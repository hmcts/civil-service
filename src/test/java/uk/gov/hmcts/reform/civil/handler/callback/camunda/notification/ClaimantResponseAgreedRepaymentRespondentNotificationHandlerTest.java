package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.*;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    ClaimantResponseAgreedRepaymentRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimantResponseAgreedRepaymentRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private PinInPostConfiguration pipInPostConfiguration;
    @Autowired
    private ClaimantResponseAgreedRepaymentRespondentNotificationHandler handler;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentCcjNotificationTemplate()).thenReturn("template-id");
            when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("test.url");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "claimant-agree-repayment-respondent-notification-000DC001"
            );
        }
    }

    @NotNull
    public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }
}
