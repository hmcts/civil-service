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
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DJApplicantReceivedNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_SPECIFIED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class DJApplicantReceivedNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DJApplicantReceivedNotificationHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            when(notificationsProperties.getApplicantSolicitor1DefaultJudgmentReceived())
                .thenReturn("test-template-received-id");

            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "test-template-received-id",
                getNotificationDataMap(caseData),
                "default-judgment-applicant-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1AndBothSelected() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            when(notificationsProperties.getApplicantSolicitor1DefaultJudgmentReceived())
                .thenReturn("test-template-received-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "test-template-received-id",
                getNotificationDataMap(caseData),
                "default-judgment-applicant-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1AndBothNotSelected() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            when(notificationsProperties.getApplicantSolicitor1DefaultJudgmentRequested())
                .thenReturn("test-template-requested-id");

            //send Requested email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("David")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "test-template-requested-id",
                getNotificationDataMapForRequested(),
                "default-judgment-applicant-requested-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAndLiPvsLiPEnabled() {
            when(notificationsProperties.getApplicantLiPDefaultJudgmentRequested())
                .thenReturn("test-template-requested-lip-id");
            when(featureToggleService.isLipVLipEnabled())
                .thenReturn(true);
            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .claimantUserDetails(IdamUserDetails.builder()
                                         .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                         .email("test@gmail.com")
                                         .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "test-template-requested-lip-id",
                getLipvLiPData(caseData),
                "default-judgment-applicant-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedAndLiPvsLiPEnabledAndBilingual() {
            when(notificationsProperties.getApplicantLiPDefaultJudgmentRequestedBilingualTemplate())
                .thenReturn("test-template-requested-lip-id-bilingual");
            when(featureToggleService.isLipVLipEnabled())
                    .thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                    .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                            .build())
                    .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                            .build())
                    .respondent1Represented(YesOrNo.NO)
                    .specRespondent1Represented(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference(Language.WELSH.name())
                    .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                    "rambo@email.com",
                    "test-template-requested-lip-id-bilingual",
                    getLipvLiPData(caseData),
                    "default-judgment-applicant-received-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                LEGAL_ORG_SPECIFIED, "Test Org Name",
                CLAIM_NUMBER, CASE_ID.toString(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapForRequested() {
            return Map.of(
                LEGAL_ORG_APPLICANT1, "Test Org Name",
                CLAIM_NUMBER, CASE_ID.toString(),
                DEFENDANT_NAME, "David",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        public Map<String, String> getLipvLiPData(CaseData caseData) {
            return Map.of(
                APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                CLAIM_NUMBER, LEGACY_CASE_REFERENCE,
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );

        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED").build()).build())).isEqualTo(TASK_ID);
    }
}
