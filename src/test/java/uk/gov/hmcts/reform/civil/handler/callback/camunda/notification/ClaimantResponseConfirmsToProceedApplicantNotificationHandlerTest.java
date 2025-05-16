package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimantResponseConfirmsToProceedApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private ClaimantResponseConfirmsToProceedApplicantNotificationHandler handler;

    private static final String TEMPLATE_ID = "template-id";
    private static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn(BILINGUAL_TEMPLATE_ID);
        }

        @Test
        void shouldNotifyLipApplicant_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                    TEMPLATE_ID,
                getNotificationDataMap(),
                "claimant-confirms-to-proceed-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyLipApplicantBilingual_whenClaimIsBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData = caseData.toBuilder()
                    .respondent1Represented(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference(Language.BOTH.toString())
                    .build();
            when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
                    .thenReturn(BILINGUAL_TEMPLATE_ID);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED")
                            .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                    "rambo@email.com",
                    BILINGUAL_TEMPLATE_ID,
                    getNotificationDataMap(),
                    "claimant-confirms-to-proceed-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED").build()).build())).isEqualTo(TASK_ID);
        }

        @Test
        void shouldNotifyLipApplicant_whenTranslatedDocUploaded() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyEmail("rambo@email.com").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("Mr. John").individualLastName("Rambo").build()).build();
            caseData.setClaimantBilingualLanguagePreference("BOTH");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "rambo@email.com",
                BILINGUAL_TEMPLATE_ID,
                getNotificationDataMap(),
                "claimant-confirms-to-proceed-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyLipApplicant_whenTranslatedDocUploadedAndWelshFlagOn() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyEmail("rambo@email.com").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("Mr. John").individualLastName("Rambo").build()).build();
            caseData.setClaimantBilingualLanguagePreference("BOTH");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "rambo@email.com",
                BILINGUAL_TEMPLATE_ID,
                getNotificationDataMap(),
                "claimant-confirms-to-proceed-applicant-notification-000DC001"
            );
        }

        @ParameterizedTest
        @CsvSource({"WELSH, ENGLISH, ENGLISH, WELSH",
            "WELSH, BOTH, ENGLISH, WELSH",
            "WELSH, WELSH, WELSH, WELSH"})
        void shouldNotNotifyLipApplicant_whenMainCaseHasWelshParty(String claimantLang, String respondentLang,
                                                                   Language claimantDocLang, Language respondentDocLang) {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(YesOrNo.NO)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                              .respondent1ResponseLanguage(respondentLang)
                                                                              .build()).build())
                .applicant1(Party.builder().partyEmail("rambo@email.com").type(Party.Type.INDIVIDUAL)
                                .individualFirstName("Mr. John").individualLastName("Rambo").build())
                .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                                                 .documents(respondentDocLang)
                                                                                 .build()).build())
                .applicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(WelshLanguageRequirements.builder()
                                                                              .documents(claimantDocLang)
                                                                              .build()).build())
                .claimantBilingualLanguagePreference(claimantLang)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();
            handler.handle(params);

            verify(notificationService, times(0)).sendMail(
                "rambo@email.com",
                BILINGUAL_TEMPLATE_ID,
                getNotificationDataMap(),
                "claimant-confirms-to-proceed-applicant-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                APPLICANT_ONE_NAME, "Mr. John Rambo"
            );
        }
    }
}
