package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ResponseDeadlineExtensionDefendantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ResponseDeadlineExtensionDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ResponseDeadlineExtensionDefendantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final String emailTemplate = "emailTemplate";
        private final String emailTemplateWelsh = "emailTemplateWelsh";
        private final String defendantEmail = "sherlock@scotlandyard.co.uk";
        private final String legacyReference = "000MC001";

        @Test
        void shouldSendEmailToLipDefendantNotBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build()).build())
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
                .respondentSolicitor1AgreedDeadlineExtension(LocalDate.now())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            given(notificationsProperties.getRespondentDeadlineExtension()).willReturn(emailTemplate);
            handler.handle(params);

            verify(notificationService).sendMail(
                defendantEmail,
                emailTemplate,
                getNotificationDataMap(caseData),
                "defendant-deadline-extension-notification-" + legacyReference
            );
        }

        @Test
        void shouldSendEmailToLipDefendantBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
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
                .respondentSolicitor1AgreedDeadlineExtension(LocalDate.now())
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            given(notificationsProperties.getRespondentDeadlineExtensionWelsh()).willReturn(emailTemplateWelsh);
            handler.handle(params);

            verify(notificationService).sendMail(
                defendantEmail,
                emailTemplateWelsh,
                getNotificationDataMap(caseData),
                "defendant-deadline-extension-notification-" + legacyReference
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    "NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION").build()).build())).isEqualTo(TASK_ID);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                AGREED_EXTENSION_DATE, formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE)
            );
        }

    }
}
