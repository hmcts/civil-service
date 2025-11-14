package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.DefaultJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_GRANTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_LIP_CASE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementSpecHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    DefaultJudgmentOnlineMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
public class DefaultJudgementSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgementSpecHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private RoboticsAddressMapper addressMapper;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            // Given
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedOneDefendant() {
            // Given
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
            CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getDefendantDetailsSpec().getValue().getLabel())
                .isEqualTo(caseData.getRespondent1().getPartyName());
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
        void shouldReturnFixedAmount_whenJudgmentAmountGreaterThan5000AndClaimIssueFixedCostsYesClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5002))
                .fixedCosts(FixedCosts.builder()
                                .claimFixedCosts(YES)
                                .fixedCostAmount("10000")
                                .build())
                .claimFixedCostsOnEntryDJ(YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5132.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5002.00\n"
                + " ### Fixed cost amount \n"
                + "£130.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5133.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5132.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountGreaterThan5000AndClaimIssueFixedCostsYesAndClaimDJFixedCostsNo() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5001))
                .fixedCosts(FixedCosts.builder()
                                .claimFixedCosts(YES)
                                .fixedCostAmount("10000")
                                .build())
                .claimFixedCostsOnEntryDJ(NO)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5101.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5001.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5102.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5101.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountMoreThan25LessThan5000AndClaimIssueFixedCostsYesClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(3001))
                .fixedCosts(FixedCosts.builder()
                    .claimFixedCosts(YES)
                    .fixedCostAmount("10000")
                    .build())
                .claimFixedCostsOnEntryDJ(YES)
                .defendantDetailsSpec(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .label("Test User")
                        .build())
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £3123.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3001.00\n"
                + " ### Fixed cost amount \n"
                + "£122.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3124.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £3123.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountMoreThan25LessThan5000AndClaimIssueFixedCostsYesAndNoClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(3001))
                .fixedCosts(FixedCosts.builder()
                    .claimFixedCosts(YES)
                    .fixedCostAmount("10000")
                    .build())
                .claimFixedCostsOnEntryDJ(NO)
                .defendantDetailsSpec(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .label("Test User")
                        .build())
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £3101.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3001.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3102.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £3101.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountLessThan25AndClaimIssueFixedCostsYes() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("299500")
                .totalClaimAmount(BigDecimal.valueOf(3000))
                .fixedCosts(FixedCosts.builder()
                                .claimFixedCosts(YES)
                                .fixedCostAmount("10000")
                                .build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £106.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3000.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3101.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£2995.00\n"
                + " ## Total still owed \n"
                + " £106.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountLessThan25AndClaimIssueFixedCostsNo() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("299500")
                .totalClaimAmount(BigDecimal.valueOf(3000))
                .fixedCosts(FixedCosts.builder()
                                .claimFixedCosts(NO)
                                .build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £6.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3000.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3001.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£2995.00\n"
                + " ## Total still owed \n"
                + " £6.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1212.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £1010.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£102.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1213.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1212.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);

        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(499))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £671.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £499.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£72.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £672.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £671.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan1000AndGreaterThan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(999))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1191.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £999.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£92.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1192.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1191.00";
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5001))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5001.00, including the claim fee and"
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

        @Test
        void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000And1v2() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5001))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .label("Test User2")
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order the defendants to pay £5001.00, including the claim fee and"
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

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan5000AndLRvLiP() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .claimFee(fee)
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .specRespondent1Represented(NO)
                .respondent1Represented(null)

                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test =
                "The Judgment request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                    + "### Claim amount \n"
                    + " £1010.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£102.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £1213.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1212.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);

        }

        @Test
        void shouldReturnClaimFee_whenHWFRemissionGrantedLRvLiP() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = Fee.builder()
                .calculatedAmountInPence(BigDecimal.valueOf(100))
                .version("1")
                .code("CODE")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .claimFee(fee)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .specRespondent1Represented(NO)
                .respondent1Represented(null)
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .outstandingFeeInPounds(BigDecimal.ZERO)
                                           .build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test =
                "The Judgment request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                    + "### Claim amount \n"
                    + " £1010.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£102.00\n"
                    + "### Claim fee amount \n"
                    + " £0.00\n"
                    + " ## Subtotal \n"
                    + " £1212.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1211.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }
    }

    private static void assertInterestIsPopulated(AboutToStartOrSubmitCallbackResponse response, int val) {
        assertThat(response.getData().get("totalInterest")).isEqualTo(BigDecimal.valueOf(val));
    }

    @Nested
    class MidRepaymentValidate {

        static final String PAGE_ID = "repaymentValidate";

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
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors().get(0))
                .isEqualTo("Selected date must be after " + formatLocalDate(eligibleDate, DATE));
        }

        @Test
        void shouldGetException_whenIsCalledAndTheOverallIsNull() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            assertThrows(
                NullPointerException.class, () -> {
                    handler.handle(params);
                }
            );
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled1v1AndIsJudgmentOnlineLiveDisabled() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(
                DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertInterestIsPopulated(response, 0);
        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v1() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPaymentAmount("10")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(
                DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo(
                "Mr. Sole Trader");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v2NonDivergent() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .partialPaymentAmount("10")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(
                DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Dob").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled1v2Divergent() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .partialPaymentAmount("10")
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(
                DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("REQUESTED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("No");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Dob").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled2v1JOIsNotLive() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(
                DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

        }

        @Test
        void shouldMoveToFinalOrderIssued_whenIsJOOnlineAnd2v1() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build()
                .toBuilder()
                .respondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build())
                .caseNameHmctsInternal("Mr. John Rambo v Dis Guy")
                .caseNamePublic("'John Rambo' v 'Dis Guy'")
                .build();

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YesOrNo.YES)
                .partialPaymentAmount("10")
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseData.getBusinessProcess()).isNotNull();
            assertThat(responseData.getBusinessProcess().getCamundaEvent()).isEqualTo(
                DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(responseData.getJoRepaymentSummaryObject()).doesNotContain("Claim interest amount");
            assertInterestIsPopulated(response, 0);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedOneDefendant() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Default Judgment Granted ")
                    .confirmationBody(format(body))
                    .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedOneDefendantWhen1v2() {
            // Given
            final String header = "# Default judgment requested";
            final String body = "A default judgment has been sent to John Smith. "
                + "The claim will now progress offline (on paper)";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setDefendantDetailsSpec(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .label("John Smith")
                                                            .build())
                                                 .build());

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedBothDefendantWhen1v2() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setDefendantDetailsSpec(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .label("Both")
                                                            .build())
                                                 .build());

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              "# Default Judgment Granted ")
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenLrVLip() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              JUDGMENT_REQUESTED_HEADER)
                                                                          .confirmationBody(String.format(
                                                                              JUDGMENT_REQUESTED_LIP_CASE))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementGrantedResponse_whenisJudgmentLiveTrueAndLrVLip() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              JUDGMENT_GRANTED_HEADER)
                                                                          .confirmationBody(format(body))
                                                                          .build());
        }
    }

    @Test
    void shouldExtendDeadline() {
        // Given
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
            .thenReturn(LocalDateTime.now().plusMonths(36));

        Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
        Party respondent = Party.builder()
            .individualFirstName("Dis")
            .individualLastName("Guy")
            .type(INDIVIDUAL).flags(respondent1Flags).build();

        CaseData caseDataBefore = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1(respondent).build();
        caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build());
        caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
        caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

        when(interestCalculator.calculateInterest(any()))
            .thenReturn(BigDecimal.valueOf(0));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
        caseData.setDefendantDetailsSpec(DynamicList.builder()
                                             .value(DynamicListElement.builder()
                                                        .label("John Smith")
                                                        .build())
                                             .build());

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        Object deadlineValue = response.getData().get("claimDismissedDeadline");
        assertThat(deadlineValue).isNotNull();

        LocalDate expectedDate = LocalDate.now().plusMonths(36);
        LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

        assertThat(actualDate).isEqualTo(expectedDate);
    }
}
