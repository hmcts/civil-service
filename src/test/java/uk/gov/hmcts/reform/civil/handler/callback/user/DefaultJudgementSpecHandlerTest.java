package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementSpecHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    InterestCalculator.class,
    FeesService.class

})
public class DefaultJudgementSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgementSpecHandler handler;

    @MockBean
    private FeesService feesService;

    @MockBean
    private InterestCalculator interestCalculator;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedOneDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("defendantDetailsSpec")).isNotNull();
        }
    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "showCertifyStatementSpec";

        @Test
        void shouldReturnDefendantDetails_whenShowCertifyStatementSpecIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("NameUser SurnameUser")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("defendantDetailsSpec")).isNotNull();
        }
    }

    @Nested
    class MidEventPartialPayment {

        private static final String PAGE_ID = "claimPartialPayment";

        @Test
        void shouldReturnError_whenPartialPaymentLargerFullClaim() {
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .totalClaimAmount(claimAmount)
                .totalInterest(interestAmount)
                .partialPaymentAmount("3000000")
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The amount already paid exceeds the full claim amount");
        }

        @Test
        void shouldNotReturnError_whenPartialPaymentLessFullClaim() {
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .totalClaimAmount(claimAmount)
                .totalInterest(interestAmount)
                .partialPaymentAmount("3000")
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventRepaymentAndDateValidate {

        private static final String PAGE_ID = "repaymentValidate";

        @Test
        void shouldNotReturnError_whenRepaymentAmountLessThanOrEqualAmountDue() {
            var testDate = LocalDate.now().plusDays(35);
            String due = "1000"; //in pounds
            String suggest = "99999"; // 999 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();
            System.out.println("due" + caseData.getRepaymentDue());
            System.out.println("suggest" + caseData.getRepaymentSuggestion());
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenRepaymentAmountGreaterThanAmountDue() {
            var testDate = LocalDate.now().plusDays(35);
            String due = "1000"; //in pounds
            String suggest = "110000"; // 1100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();
            System.out.println("due" + caseData.getRepaymentDue());
            System.out.println("suggest" + caseData.getRepaymentSuggestion());

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Regular payment cannot exceed the full claim amount");
        }

        @Test
        void shouldNotReturnError_whenDateNotInPastAndEligible() {
            //eligible date is 31 days in the future
            var testDate = LocalDate.now().plusDays(31);
            String due = "1000"; //in pounds
            String suggest = "10000"; // 100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenDateInPastAndNotEligible() {
            //eligible date is 31 days in the future, but as text says "after", we set text to one day previous i.e.
            //If 7th is the eligible date, text will say "after the 6th".
            LocalDate eligibleDate = LocalDate.now().plusDays(30);
            var testDate = LocalDate.now().plusDays(25);
            String due = "1000"; //in pounds
            String suggest = "10000"; // 100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0))
                .isEqualTo("Selected date must be after " + formatLocalDate(
                    eligibleDate,
                    DATE
                ));
        }

        @Nested
        class SubmittedCallback {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
                String cprRequiredInfo = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" " +
                    "target=\"_blank\">Download  default judgment</a> "
                    + "%n The defendant will be served the Default Judgment.";
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader("# Default Judgment Granted ")
                        .confirmationBody(format(cprRequiredInfo))
                        .build());
            }
        }

        @Nested
        class PaymentDateValidationCallback {

            private static final String PAGE_ID = "claimPaymentDate";

            @Test
            void shouldReturnError_whenPastPaymentDate() {

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().minusDays(15))
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors().get(0)).isEqualTo("Payment Date cannot be past date");
            }

            @Test
            void shouldNotReturnError_whenPastPaymentDate() {

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().plusDays(15))
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

        }

        @Nested
        class RepaymentBreakdownCallback {

            private static final String PAGE_ID = "repaymentBreakdown";

            @Test
            void shouldReturnFixedAmount_whenClaimAmountLessthan5000() {
                when(interestCalculator.calculateInterest(any()))
                    .thenReturn(BigDecimal.valueOf(100)
                    );
                when(feesService.getFeeDataByTotalClaimAmount(any()))
                    .thenReturn(Fee.builder()
                                    .calculatedAmountInPence(BigDecimal.valueOf(100))
                                    .version("1")
                                    .code("CODE")
                                    .build());
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().minusDays(15))
                    .partialPaymentAmount("100")
                    .totalClaimAmount(BigDecimal.valueOf(1010))
                    .paymentConfirmationDecisionSpec(YesOrNo.YES)
                    .partialPayment(YesOrNo.YES)

                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                String test = "The judgment will order the defendant to pay £1222.00, including the claim fee and"
                    + " interest, if applicable, as shown:\n"
                    + "### Claim amount \n"
                    + " £1010.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£112.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £1223.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1222.00";

                assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
            }

            @Test
            void shouldReturnFixedAmount_whenClaimAmountLessthan500() {
                when(interestCalculator.calculateInterest(any()))
                    .thenReturn(BigDecimal.valueOf(100)
                    );
                when(feesService.getFeeDataByTotalClaimAmount(any()))
                    .thenReturn(Fee.builder()
                                    .calculatedAmountInPence(BigDecimal.valueOf(100))
                                    .version("1")
                                    .code("CODE")
                                    .build());
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().minusDays(15))
                    .partialPaymentAmount("100")
                    .totalClaimAmount(BigDecimal.valueOf(499))
                    .paymentConfirmationDecisionSpec(YesOrNo.YES)
                    .partialPayment(YesOrNo.YES)

                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                String test = "The judgment will order the defendant to pay £681.00, including the claim fee and"
                    + " interest, if applicable, as shown:\n"
                    + "### Claim amount \n"
                    + " £499.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£82.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £682.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £681.00";

                assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
            }

            @Test
            void shouldReturnFixedAmount_whenClaimAmountLessthan1000AndGreaterThan500() {
                when(interestCalculator.calculateInterest(any()))
                    .thenReturn(BigDecimal.valueOf(100)
                    );
                when(feesService.getFeeDataByTotalClaimAmount(any()))
                    .thenReturn(Fee.builder()
                                    .calculatedAmountInPence(BigDecimal.valueOf(100))
                                    .version("1")
                                    .code("CODE")
                                    .build());
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().minusDays(15))
                    .partialPaymentAmount("100")
                    .totalClaimAmount(BigDecimal.valueOf(999))
                    .paymentConfirmationDecisionSpec(YesOrNo.YES)
                    .partialPayment(YesOrNo.YES)

                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                String test = "The judgment will order the defendant to pay £1201.00, including the claim fee and"
                    + " interest, if applicable, as shown:\n"
                    + "### Claim amount \n"
                    + " £999.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£102.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £1202.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1201.00";
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
            }

            @Test
            void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000() {
                when(interestCalculator.calculateInterest(any()))
                    .thenReturn(BigDecimal.valueOf(0)
                    );
                when(feesService.getFeeDataByTotalClaimAmount(any()))
                    .thenReturn(Fee.builder()
                                    .calculatedAmountInPence(BigDecimal.valueOf(100))
                                    .version("1")
                                    .code("CODE")
                                    .build());
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                    .partialPayment(YesOrNo.YES)
                    .paymentSetDate(LocalDate.now().minusDays(15))
                    .partialPaymentAmount("100")
                    .totalClaimAmount(BigDecimal.valueOf(5001))

                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                String test = "The judgment will order the defendant to pay £5001.00, including the claim fee and"
                    + " interest, if applicable, as shown:\n"
                    + "### Claim amount \n"
                    + " £5001.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £5002.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £5001.00";
                assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
            }
        }
    }
}
