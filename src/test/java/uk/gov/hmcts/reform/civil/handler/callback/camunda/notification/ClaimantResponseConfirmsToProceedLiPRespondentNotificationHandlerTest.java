package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String RESPONDENT_EMAIL_TEMPLATE = "template-id-respondent";
        private static final String RESPONDENT_MEDIATION_EMAIL_TEMPLATE = "template-mediation-id-respondent";
        private static final String RESPONDENT_EMAIL_ID = "sole.trader@email.com";
        private static final String REFERENCE_NUMBER = "claimant-confirms-to-proceed-respondent-notification-000DC001";
        private static final String DEFENDANT = "Mr. Sole Trader";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(
                RESPONDENT_EMAIL_TEMPLATE);
            when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn(
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        }

        @Test
        void shouldNotifyLipRespondent_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                RESPONDENT_EMAIL_ID,
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLipRespondent_whenTranslatedDocUploaded() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setClaimantBilingualLanguagePreference("BOTH");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                RESPONDENT_EMAIL_ID,
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotNotifyLipRespondent_ifBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setClaimantBilingualLanguagePreference("BOTH");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyLipRespondent_ifNoPartyEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.getRespondent1().setPartyEmail(null);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyLRRespondent_whenApplicantProceeds() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(NO)
                                 .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE,
                getNotificationDataMapCarm(caseData),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONDENT_NAME, DEFENDANT
            );
        }

        private Map<String, String> getNotificationDataMapCarm(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name()).build()).build())).isEqualTo(TASK_ID);
        }

    }

}
