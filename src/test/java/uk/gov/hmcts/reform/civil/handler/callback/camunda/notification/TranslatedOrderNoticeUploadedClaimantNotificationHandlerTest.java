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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@ExtendWith(MockitoExtension.class)
public class TranslatedOrderNoticeUploadedClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private TranslatedOrderNoticeUploadedClaimantNotificationHandler handler;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;
    private static final String CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL = "template-id-bilingual-translation";
    private static final String CLAIMANT_LIP_EMAIL = "rambo@email.com";
    private static final String LEGACY_CASE_REF = "translated-order-notice-uploaded-claimant-notification-000DC001";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotNotifyApplicantLR_whenInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .applicant1Represented(YesOrNo.YES)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verifyNoInteractions(notificationService);

        }

        @Test
        void  shouldNotifyLipClaimantForOrderTranslatedDoc_whenBilingual() {
            // Given
            when(notificationsProperties.getNotifyLiPOrderTranslatedTemplate()).thenReturn(CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStatePendingClaimIssued()
                    .build().toBuilder()
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference("BOTH")
                    .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
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
        void  shouldNotNotifyLipClaimantForOrderTranslatedDoc_whenEnglish() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("ENGLISH")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
                    .build()).build();
            // When
            handler.handle(params);

            // Then
            verifyNoInteractions(notificationService);
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpecClaimantLIP(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME, caseData.getApplicant1().getPartyName()
            );
        }
    }
}
