package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.DEADLINE;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@SpringBootTest(classes = {
    NotifyClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotifyClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @Autowired
    private NotifyClaimCallbackHandler handler;

    private final LocalDateTime notificationDate = LocalDateTime.now();
    private final LocalDateTime deadline = notificationDate.toLocalDate().atTime(END_OF_BUSINESS_DAY);

    @BeforeEach
    void setup() {
        when(time.now()).thenReturn(notificationDate);
        when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDate.class))).thenReturn(deadline);
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldUpdateBusinessProcessAndAddNotificationDeadline_when14DaysIsBeforeThe4MonthDeadline() {
            LocalDateTime claimNotificationDeadline = notificationDate.plusMonths(4);
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification()
                .claimNotificationDeadline(claimNotificationDeadline)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(CallbackType.ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM.name(), "READY");

            assertThat(response.getData())
                .containsEntry("claimNotificationDate", notificationDate.format(ISO_DATE_TIME))
                .containsEntry("claimDetailsNotificationDeadline", deadline.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetClaimDetailsNotificationAsClaimNotificationDeadline_when14DaysIsAfterThe4MonthDeadline() {
            LocalDateTime claimNotificationDeadline = notificationDate.minusDays(5);
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification()
                .claimNotificationDeadline(claimNotificationDeadline)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(CallbackType.ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("claimDetailsNotificationDeadline", claimNotificationDeadline.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetClaimDetailsNotificationAsClaimNotificationDeadline_when14DaysIsSameDayAs4MonthDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification()
                .claimNotificationDeadline(deadline)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(CallbackType.ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("claimDetailsNotificationDeadline", deadline.format(ISO_DATE_TIME));
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String CONFIRMATION_BODY = "<br />The defendant legal representative's organisation has "
            + "been notified and granted access to this claim.\n\n"
            + "You must notify the defendant with the claim details by %s";

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
            String confirmationBody = String.format(CONFIRMATION_BODY, formattedDeadline);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Notification of claim sent%n## Claim number: 000LR001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }
    }
}
