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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerLip.TASK_ID_LIP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerLipTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerLip handler;
    private static final String RESPONDENT_EMAIL_TEMPLATE = "template-id-respondent";
    private static final String BILINGUAL_RESPONDENT_EMAIL_TEMPLATE = "bilingual-id-respondent";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked_spec_lip() {
            when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("spec-lip-template-id");
            when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "spec-lip-template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyLiPRespondent_whenInvoked_spec_lip() {
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(RESPONDENT_EMAIL_TEMPLATE);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1Represented(YES)
                .responseClaimTrack(SMALL_CLAIM.name())
                .applicant1ProceedWithClaim(NO)
                .build().toBuilder()
                .defenceRouteRequired(DISPUTES_THE_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMapPartAdmit(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyLiPRespondent_whenInvoked_spec_lip_response_in_bilingual() {
            when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(BILINGUAL_RESPONDENT_EMAIL_TEMPLATE);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1Represented(YES)
                .responseClaimTrack(SMALL_CLAIM.name())
                .applicant1ProceedWithClaim(NO)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .build().toBuilder()
                .defenceRouteRequired(DISPUTES_THE_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                BILINGUAL_RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMapPartAdmit(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenInvoked_spec_partAdmit() {
            when(notificationsProperties.getNotifyRespondentLipPartAdmitPayImmediatelyAcceptedSpec()).thenReturn("spec-lip-template-part-admit-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
                .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "spec-lip-template-part-admit-id",
                getNotificationDataMapPartAdmit(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapPartAdmit(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP").build()).build()))
            .isEqualTo(TASK_ID_LIP);
    }
}
