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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class DefendantLipResponseRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefendantLipResponseRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final String defendantEmail = "sherlock@scotlandyard.co.uk";
        private final String legacyReference = "000MC001";

        @Test
        void shouldSendEmailToLipDefendant() {
            String emailTemplate = "emailTemplate";
            given(notificationsProperties.getRespondentLipResponseSubmissionTemplate()).willReturn(emailTemplate);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build()
                .builder()
                .legacyCaseReference(legacyReference)
                .respondent1(Party
                                 .builder().type(Party.Type.INDIVIDUAL)
                                 .individualTitle("Mr")
                                 .individualFirstName("Sherlock")
                                 .individualLastName("Holmes")
                                 .partyEmail(defendantEmail)
                                 .build())
                .applicant1(Party.builder()
                                .type(Party.Type.COMPANY)
                                .companyName("Bad guys ltd")
                                .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                defendantEmail,
                emailTemplate,
                getNotificationDataMap(caseData),
                "defendant-lip-response-respondent-notification-" + legacyReference
            );
        }

        @Test
        void shouldSendBilingualEmailToLipDefendant() {
            String bilingualEmailTemplate = "bilingualEmailTemplate";
            given(notificationsProperties.getRespondentLipResponseSubmissionBilingualTemplate()).willReturn(
                bilingualEmailTemplate);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .legacyCaseReference(legacyReference)
                .respondent1(Party
                                 .builder().type(Party.Type.INDIVIDUAL)
                                 .individualTitle("Mr")
                                 .individualFirstName("Sherlock")
                                 .individualLastName("Holmes")
                                 .partyEmail(defendantEmail)
                                 .build())
                .applicant1(Party.builder()
                                .type(Party.Type.COMPANY)
                                .companyName("Bad guys ltd")
                                .build())
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                defendantEmail,
                bilingualEmailTemplate,
                getNotificationDataMap(caseData),
                "defendant-lip-response-respondent-notification-" + legacyReference
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
            );
        }

    }
}
