package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class ClaimantDefendantAgreedMediationRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantDefendantAgreedMediationRespondentNotificationHandler  handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
            when(notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplate()).thenReturn("template-id");

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
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_AGREEMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-agreement-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLiPBilingual_whenInvoked() {
            when(notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplateWelsh()).thenReturn("template-id-welsh");

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
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_AGREEMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-welsh",
                getNotificationDataMapSpec(caseData),
                "mediation-agreement-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLR_whenInvoked() {
            when(notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("defendant solicitor org").build()));

            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .setClaimTypeToSpecClaim()
                .specRespondent1Represented(YES)
                .respondent1Represented(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_AGREEMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-agreement-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1LR_whenInvokedCarm() {
            when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn("template-mediation-id");
            when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("defendant solicitor org").build()));
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .setClaimTypeToSpecClaim()
                .specRespondent1Represented(YES)
                .respondent1Represented(YES)
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_AGREEMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-mediation-id",
                getNotificationDataMapCarm(caseData),
                "mediation-agreement-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2LR_whenInvokedCarm() {
            when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn("template-mediation-id");
            when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("defendant solicitor 2 org").build()));
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .multiPartyClaimTwoDefendantSolicitorsSpec()
                .setClaimTypeToSpecClaim()
                .specRespondent1Represented(YES)
                .respondent1Represented(YES)
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-mediation-id",
                getNotificationDataMapRespondent2Carm(caseData),
                "mediation-agreement-respondent-notification-000DC001"
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            if (caseData.isRespondent1NotRepresented()) {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                    PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                    CASEMAN_REF, caseData.getLegacyCaseReference()
                );
            } else {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
                    CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                    PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                    CASEMAN_REF, caseData.getLegacyCaseReference()
                );
            }
        }

        @NotNull
        public Map<String, String> getNotificationDataMapCarm(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapRespondent2Carm(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            );
        }

        public String getRespondentLegalOrganizationName(CaseData caseData) {
            String id = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            Optional<Organisation> organisation = organisationService.findOrganisationById(id);

            String respondentLegalOrganizationName = null;
            if (organisation.isPresent()) {
                respondentLegalOrganizationName = organisation.get().getName();
            }
            return respondentLegalOrganizationName;
        }
    }
}
