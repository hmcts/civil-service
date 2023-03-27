package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@SpringBootTest(classes = {
    ClaimantResponseNotAgreedRepaymentRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimantResponseNotAgreedRepaymentRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ClaimantResponseNotAgreedRepaymentRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final String emailTemplate = "emailTemplate";
        private final String defendantEmail = "sherlock@scotlandyard.co.uk";
        private final String legacyReference = "000MC001";

        @BeforeEach
        void setUp() {
            given(notificationsProperties.getNotifyClaimantLrTemplate()).willReturn(emailTemplate);
            given(notificationsProperties.getNotifyDefendantLipTemplate()).willReturn(emailTemplate);
        }

        @Test
        void shouldSendEmailToLipDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build()
                .builder()
                .legacyCaseReference(legacyReference)
                .respondent1(Party
                                 .builder().type(Party.Type.INDIVIDUAL)
                                 .individualTitle("Mr")
                                 .individualFirstName("James")
                                 .individualLastName("Cock")
                                 .partyEmail(defendantEmail)
                                 .build())
                .applicant1(Party.builder()
                                .type(Party.Type.COMPANY)
                                .companyName("ABC Company ltd")
                                .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                defendantEmail,
                emailTemplate,
                getNotificationDataMap(caseData),
                "notification-" + legacyReference
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
