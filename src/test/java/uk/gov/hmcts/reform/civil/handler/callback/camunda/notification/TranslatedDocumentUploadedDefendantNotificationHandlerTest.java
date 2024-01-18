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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    TranslatedDocumentUploadedDefendantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class TranslatedDocumentUploadedDefendantNotificationHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private TranslatedDocumentUploadedDefendantNotificationHandler handler;
    private final String emailTemplate = "template-id";
    private final String defendantEmail = "respondent@example.com";
    private final String legacyCaseReference = "translated-document-uploaded-defendant-notification-000DC001";
    private final String defendantName = "respondent";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(emailTemplate);
        }

        @Test
        void shouldNotifyApplicantParty_whenInvoked() {
            //Given
            Party party = PartyBuilder.builder()
                .individual(defendantName)
                .partyEmail(defendantEmail)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .respondent1(party)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED.name())
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                defendantEmail,
                emailTemplate,
                getNotificationDataMapSpec(caseData),
                legacyCaseReference
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            return Map.of(
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
                );
        }

        @Test
        void  shouldNotNotifyLipDefendantForClaimIssueTranslatedDoc_whenR2Enabled() {
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
                    CallbackRequest.builder().eventId(CaseEvent.NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED.name())
                            .build()).build();
            // When
            handler.handle(params);

            // Assertions
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }
    }

}
