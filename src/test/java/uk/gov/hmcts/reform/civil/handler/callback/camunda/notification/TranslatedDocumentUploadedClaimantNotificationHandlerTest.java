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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
public class TranslatedDocumentUploadedClaimantNotificationHandlerTest {

    @InjectMocks
    private TranslatedDocumentUploadedClaimantNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    private static final String CLAIMANT_LR_EMAIL_TEMPLATE = "template-id";
    private static final String CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL = "template-id-Bilingual";
    private static final String CLAIMANT_LIP_EMAIL_TEMPLATE_ENGLISH = "template-id-English";
    private static final String CLAIMANT_LR_EMAIL = "applicantsolicitor@example.com";
    private static final String CLAIMANT_LIP_EMAIL = "rambo@email.com";
    private static final String LEGACY_CASE_REF = "translated-document-uploaded-claimant-notification-000DC001";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantParty_whenInvoked() {
            when(notificationsProperties.getNotifyClaimantTranslatedDocumentUploaded()).thenReturn(CLAIMANT_LR_EMAIL_TEMPLATE);
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED.name())
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                CLAIMANT_LR_EMAIL,
                CLAIMANT_LR_EMAIL_TEMPLATE,
                getNotificationDataMapSpecClaimantLR(caseData),
                LEGACY_CASE_REF
            );

        }

        @Test
        void  shouldNotifyLipClaimantForClaimIssueTranslatedDoc_whenR2EnabledAndClaimIssuedInBilingual() {
            when(notificationsProperties.getNotifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInBilingual()).thenReturn(CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL);
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStatePendingClaimIssued()
                    .build().toBuilder()
                    .respondent1Represented(YesOrNo.NO)
                    .specRespondent1Represented(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference("BOTH")
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED.name())
                            .build()).build();
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                CLAIMANT_LIP_EMAIL,
                CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL,
                getNotificationDataMapSpecClaimantLIP(caseData),
                LEGACY_CASE_REF
            );
        }

        @Test
        void  shouldNotifyLipClaimantForClaimIssueTranslatedDoc_whenR2EnabledAndClaimIssuedInEnglish() {
            when(notificationsProperties.getNotifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInEnglish()).thenReturn(CLAIMANT_LIP_EMAIL_TEMPLATE_ENGLISH);
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED.name())
                    .build()).build();
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                CLAIMANT_LIP_EMAIL,
                CLAIMANT_LIP_EMAIL_TEMPLATE_ENGLISH,
                getNotificationDataMapSpecClaimantLIP(caseData),
                LEGACY_CASE_REF
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpecClaimantLR(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData)
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpecClaimantLIP(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
            );
        }

        private String getApplicantLegalOrganizationName(CaseData caseData) {
            String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
            Optional<Organisation> organisation = organisationService.findOrganisationById(id);
            return organisation.isPresent() ? organisation.get().getName() :
                caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
        }
    }
}
