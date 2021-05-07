package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.unspec.service.Time;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;

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

    @Autowired
    private NotifyClaimDetailsCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
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
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(localDateTime.toLocalDate())).thenReturn(newDate);
                when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, localDateTime.toLocalDate()))
                    .thenReturn(sixMonthDate);
            }

            @Test
            void shouldUpdateBusinessProcess_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
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
        }

        @Nested
        class SubmittedCallback {

            private static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation"
                + " has been notified of the claim details.%n%n"
                + "They must respond by %s. Your account will be updated and you will be sent an email.";

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
        }
    }
}
