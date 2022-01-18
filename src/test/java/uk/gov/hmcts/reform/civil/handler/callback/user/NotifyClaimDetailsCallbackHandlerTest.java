package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.DEADLINE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;

@SpringBootTest(classes = {
    NotifyClaimDetailsCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class NotifyClaimDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private NotifyClaimDetailsCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateDynamicListWithOptions_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertTrue(response.getData().containsKey("defendantSolicitorNotifyClaimDetailsOptions"));
        }
    }

    @Nested
    class MidEventValidateOptionsCallback {

        private static final String PAGE_ID = "validateNotificationOption";
        public static final String WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR =
            "Your claim will progress offline if you only notify one Defendant of the claim details.";

        @Test
        void shouldThrowWarning_whenNotifyingOnlyOneRespondentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).contains(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }
    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenNoDocumentsBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorStateBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValidBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Nested
        class AboutToSubmit {
            private LocalDateTime localDateTime;
            private LocalDateTime newDate;
            private LocalDateTime sixMonthDate;

            @BeforeEach
            void setup() {
                localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
                newDate = LocalDateTime.of(2020, 1, 15, 16, 0, 0);
                sixMonthDate = LocalDateTime.of(2020, 7, 1, 0, 0, 0);
                when(time.now()).thenReturn(localDateTime);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(localDateTime)).thenReturn(newDate);
                when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, localDateTime.toLocalDate()))
                    .thenReturn(sixMonthDate);
            }

            @Test
            void shouldUpdateBusinessProcess_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent", "status")
                    .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name(), "READY");

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDate", localDateTime.format(ISO_DATE_TIME))
                    .containsEntry("respondent1ResponseDeadline", newDate.format(ISO_DATE_TIME))
                    .containsEntry("claimDismissedDeadline", sixMonthDate.format(ISO_DATE_TIME));
            }

            @Test
            void shouldUpdateBusinessProcess_whenInvoked1v2DifferentSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent", "status")
                    .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name(), "READY");

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDate", localDateTime.format(ISO_DATE_TIME))
                    .containsEntry("respondent1ResponseDeadline", newDate.format(ISO_DATE_TIME))
                    .containsEntry("respondent2ResponseDeadline", newDate.format(ISO_DATE_TIME))
                    .containsEntry("claimDismissedDeadline", sixMonthDate.format(ISO_DATE_TIME));
            }
        }

        @Nested
        class SubmittedCallback {

            private static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation"
                + " has been notified of the claim details.%n%n"
                + "They must respond by %s. Your account will be updated and you will be sent an email.";

            public static final String CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim "
                + "details sent to 1 Defendant legal representative only.%n%n"
                + "You must notify the other defendant legal representative of the claim details by %s";

            @BeforeEach
            void setup() {
                when(featureToggleService.isMultipartyEnabled()).thenReturn(true);
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String formattedDeadline = formatLocalDateTime(RESPONSE_DEADLINE, DATE_TIME_AT);
                String confirmationBody = format(CONFIRMATION_SUMMARY, formattedDeadline)
                    + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                        .confirmationBody(confirmationBody)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingBothParties_whenInvoked() {

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                    .build();

                CallbackParams params = callbackParamsOf(V_1, caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
                String confirmationBody = String.format(CONFIRMATION_SUMMARY, formattedDeadline)
                    + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                        .confirmationBody(confirmationBody)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingOneParty_whenInvoked() {

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                    .build();

                CallbackParams params = callbackParamsOf(V_1, caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
                String confirmationBody = String.format(
                    CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY,
                    formattedDeadline
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                        .confirmationBody(confirmationBody)
                        .build());
            }
        }
    }
}
