package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.EmailTO;
import uk.gov.hmcts.reform.civil.notification.handlers.AddDefendantLitigationFriendNotificationHandler;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.LitigationFriendAddedNotificationHandler.REFERENCE_TEMPLATE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.LitigationFriendAddedNotificationHandler.REFERENCE_TEMPLATE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

@ExtendWith(MockitoExtension.class)
class LitigationFriendAddedNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    AddDefendantLitigationFriendNotificationHandler addDefendantLitigationFriendNotificationHandler;

    @InjectMocks
    private LitigationFriendAddedNotificationHandler handler;

    @NotNull
    private Map<String, String> getApplicantNotificationDataMap(CaseData caseData) {
        return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
        );
    }

    @NotNull
    private Map<String, String> getDefendantNotificationDataMap(CaseData caseData) {
        return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
        );
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);
        }

        @NotNull
        private EmailTO getEmailTO(CaseData caseData) {
            return EmailTO.builder()
                    .caseData(caseData)
                    .emailTemplate("template-id")
                    .applicantSol1Email(caseData.getApplicantSolicitor1UserDetails().getEmail())
                    .applicantSol1Params(getApplicantNotificationDataMap(caseData))
                    .applicantRef(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
                    .respondentSol1Email(caseData.getRespondentSolicitor1EmailAddress())
                    .respondentSol1Params(getDefendantNotificationDataMap(caseData))
                    .respondentRef(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
                    .respondentSol2Email(caseData.getRespondentSolicitor2EmailAddress())
                    .respondentSol2Params(getDefendantNotificationDataMap(caseData))
                    .canSendEmailToRespondentSol2(true)
                    .build();
        }
    }
}
