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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    ClaimantResponseNotAgreedRepaymentDefendantLipNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimantResponseNotAgreedRepaymentDefendantLipNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationDetailsService organisationDetailsService;
    @Autowired
    private ClaimantResponseNotAgreedRepaymentDefendantLipNotificationHandler handler;
    private static final String ORGANISATION_NAME = "Defendant solicitor org";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyDefendantLipTemplate()).thenReturn("template-id");
            when(notificationsProperties.getNotifyDefendantLipWelshTemplate()).thenReturn("template-welsh-id");
            when(notificationsProperties.getNotifyDefendantLrTemplate()).thenReturn("template-id-lr");
            when(organisationDetailsService.getRespondentLegalOrganizationName(any())).thenReturn(ORGANISATION_NAME);
        }

        @Test
        void shouldNotifyRespondentParty_whenInvoked() {
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .respondent1Represented(null)
                .specRespondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "claimant-reject-repayment-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentPartyInWelsh_whenInvoked() {
            Party respondent1 = PartyBuilder.builder()
                .soleTrader()
                .partyEmail("respondent@example.com")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .respondent1Represented(null)
                .specRespondent1Represented(YesOrNo.NO)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "BOTH").build()).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT").build())
                .build();

            handler.handle(params);
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-welsh-id",
                getNotificationDataMapSpec(caseData),
                "claimant-reject-repayment-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLrParty_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondent1OrgRegistered(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "template-id-lr",
                getNotificationDataMapSpec(caseData),
                "claimant-reject-repayment-respondent-notification-000DC001"
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            if (caseData.isRespondent1NotRepresented()) {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
                );
            } else {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondentLegalOrganizationName(caseData)
                );
            }
        }
    }
}
