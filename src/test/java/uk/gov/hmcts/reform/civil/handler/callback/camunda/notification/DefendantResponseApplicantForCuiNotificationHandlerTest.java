package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class DefendantResponseApplicantForCuiNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DefendantResponseApplicantForCuiNotificationHandler handler;

    @Mock
    private FeatureToggleService toggleService;

    @Nested
    class AboutToSubmitCallback {

        private static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
        private static final String REFERENCE = "defendant-response-applicant-notification-000DC001";
        private static final String APPLICANT_EMAIL = "rambo@email.com";
        private static final String TEMPLATE_ID = "template-id";
        private static final String TEMPLATE_ID_MEDIATION = "template-id-mediation";
        private static final String TEMPLATE_ID_NO_MEDIATION = "template-id-no-mediation";
        private static final String TEMPLATE_ID_LIP_CLAIMANT = "template-id-lip-claimant";
        private static final String CLAIM_LEGAL_ORG_NAME = "Signer Name";

        @Test
        void shouldNotifyApplicantSolicitorForPartAdmit_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullAdmitOrPartAdmitTemplate())
                .thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorForFullDefenceWithMediation_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullDefenceWithMediationTemplate())
                .thenReturn(TEMPLATE_ID_MEDIATION);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .responseClaimMediationSpecRequired(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID_MEDIATION,
                getNotificationFullDefenceDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorForFullDefenceNoMediation_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullDefenceNoMediationTemplate())
                .thenReturn(TEMPLATE_ID_NO_MEDIATION);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .responseClaimMediationSpecRequired(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID_NO_MEDIATION,
                getNotificationFullDefenceDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyLiPClaimant_whenInvoked() {
            when(notificationsProperties.getNotifyLiPClaimantDefendantResponded()).thenReturn(TEMPLATE_ID_LIP_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_EMAIL,
                TEMPLATE_ID_LIP_CLAIMANT,
                getNotificationDataMapForLiPClaimant(caseData),
                REFERENCE
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME,
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001",
                "defendantName", "Mr. Sole Trader"
            );
        }

        @NotNull
        private Map<String, String> getNotificationFullDefenceDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME,
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapForLiPClaimant(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }
    }
}
