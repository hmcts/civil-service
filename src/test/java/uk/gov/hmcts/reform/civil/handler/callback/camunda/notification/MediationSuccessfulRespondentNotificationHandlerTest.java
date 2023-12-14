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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    MediationSuccessfulRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class MediationSuccessfulRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private MediationSuccessfulRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate()).willReturn("template-id");
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh()).willReturn("template-id-welsh");
        }

        @Test
        void shouldNotifyRespondent_whenInvoked() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyBilingualRespondent_whenInvoked() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-welsh",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendEmail_whenEventIsCalledAndDefendantHasNoEmail() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail(null)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService, times(0)).sendMail("respondent@example.com",
                                                           "template-id",
                                                           getNotificationDataMapSpec(caseData),
                                                           "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendEmail_whenRespondentIsLR() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .specRespondent1Represented(YesOrNo.YES)
                .respondent1Represented(YesOrNo.YES)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService, times(0)).sendMail("respondent@example.com",
                                                           "template-id",
                                                           getNotificationDataMapSpec(caseData),
                                                           "mediation-successful-respondent-notification-000DC001"
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }

    }
}
