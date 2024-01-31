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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
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

@SpringBootTest(classes = {
    TranslatedDocumentUploadedClaimantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class TranslatedDocumentUploadedClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private TranslatedDocumentUploadedClaimantNotificationHandler handler;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    private static final String emailTemplate1 = "template-id";
    private static final String emailTemplate2 = "template-id-Bilingual";
    private static final String emailTemplate3 = "template-id-English";
    private static final String claimantLREmail = "applicantsolicitor@example.com";
    private static final String claimantLiPEmail = "rambo@email.com";
    private static final String legacyCaseReference = "translated-document-uploaded-claimant-notification-000DC001";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyClaimantTranslatedDocumentUploaded()).thenReturn(emailTemplate1);
            when(notificationsProperties.getNotifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInBilingual()).thenReturn(emailTemplate2);
            when(notificationsProperties.getNotifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInEnglish()).thenReturn(emailTemplate3);
        }

        @Test
        void shouldNotifyApplicantParty_whenInvoked() {
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
                claimantLREmail,
                emailTemplate1,
                getNotificationDataMapSpecClaimantLR(caseData),
                legacyCaseReference
            );

        }

        @Test
        void  shouldNotifyLipClaimantForClaimIssueTranslatedDoc_whenR2EnabledAndClaimIssuedInBilingual() {
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
                claimantLiPEmail,
                emailTemplate2,
                getNotificationDataMapSpecClaimantLIP(caseData),
                legacyCaseReference
            );
        }

        @Test
        void  shouldNotifyLipClaimantForClaimIssueTranslatedDoc_whenR2EnabledAndClaimIssuedInEnglish() {
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
                claimantLiPEmail,
                emailTemplate3,
                getNotificationDataMapSpecClaimantLIP(caseData),
                legacyCaseReference
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
